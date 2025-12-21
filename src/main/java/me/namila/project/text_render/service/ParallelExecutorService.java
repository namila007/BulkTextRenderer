package me.namila.project.text_render.service;

import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.util.ProgressTracker;
import me.namila.project.text_render.util.VirtualThreadExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing render jobs using adaptive threading strategy.
 * <p>
 * Threading Strategy:
 * - For small batches (< SEQUENTIAL_THRESHOLD jobs): Uses main thread for sequential execution.
 *   This is more efficient for small workloads and avoids virtual thread overhead.
 * - For larger batches: Uses Java 21+ virtual threads with custom exception handling
 *   and a semaphore to control parallelism.
 * <p>
 * All threads have a custom {@link VirtualThreadExceptionHandler} attached to ensure
 * uncaught exceptions are properly logged, which is critical for GraalVM native-image
 * environments where default exception handling may not work correctly.
 */
@Service
public class ParallelExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ParallelExecutorService.class);
    private static final long DEFAULT_TIMEOUT_MINUTES = 60;
    
    /**
     * Default threshold for switching from sequential to parallel execution.
     * Jobs below this count will be processed sequentially on the main thread.
     */
    public static final int DEFAULT_SEQUENTIAL_THRESHOLD = 10;

    /**
     * Executes all render jobs using adaptive threading strategy.
     * <p>
     * For job counts below the threshold, executes sequentially on the main thread.
     * For larger job counts, uses virtual threads with the specified parallelism.
     *
     * @param jobs            the list of render jobs to execute
     * @param renderer        the renderer service to use
     * @param maxParallelism  the maximum number of concurrent tasks
     * @param progressTracker the progress tracker for monitoring completion
     */
    public void executeAll(List<RenderJob> jobs, RendererService renderer,
                           int maxParallelism, ProgressTracker progressTracker) {
        executeAll(jobs, renderer, maxParallelism, progressTracker, DEFAULT_SEQUENTIAL_THRESHOLD);
    }

    /**
     * Executes all render jobs with configurable sequential threshold.
     *
     * @param jobs               the list of render jobs to execute
     * @param renderer           the renderer service to use
     * @param maxParallelism     the maximum number of concurrent tasks (for parallel mode)
     * @param progressTracker    the progress tracker for monitoring completion
     * @param sequentialThreshold jobs below this count are processed sequentially
     */
    public void executeAll(List<RenderJob> jobs, RendererService renderer,
                           int maxParallelism, ProgressTracker progressTracker,
                           int sequentialThreshold) {
        if (jobs.isEmpty()) {
            logger.debug("No jobs to execute, returning");
            return;
        }

        if (jobs.size() < sequentialThreshold) {
            logger.info("Processing {} jobs sequentially (below threshold of {})", 
                       jobs.size(), sequentialThreshold);
            executeSequentially(jobs, renderer, progressTracker);
        } else {
            logger.info("Processing {} jobs in parallel with {} threads", 
                       jobs.size(), maxParallelism);
            executeInParallel(jobs, renderer, maxParallelism, progressTracker);
        }

        logger.info("All jobs completed");
    }

    /**
     * Executes jobs sequentially on the main thread.
     * More efficient for small batches, avoiding thread creation overhead.
     */
    private void executeSequentially(List<RenderJob> jobs, RendererService renderer,
                                     ProgressTracker progressTracker) {
        List<String> failedJobs = new ArrayList<>();
        
        for (RenderJob job : jobs) {
            try {
                logger.debug("Rendering job for text: {}", job.text());
                renderer.render(job);
                progressTracker.increment();
                logger.debug("Successfully rendered job for text: {}", job.text());
            } catch (Throwable e) {
                failedJobs.add(job.text());
                handleJobError(job, e);
            }
        }
        
        reportFailures(failedJobs);
    }

    /**
     * Executes jobs in parallel using virtual threads with custom exception handling.
     */
    private void executeInParallel(List<RenderJob> jobs, RendererService renderer,
                                   int maxParallelism, ProgressTracker progressTracker) {
        // Create thread factory with custom exception handler for visibility in native-image
        ThreadFactory virtualThreadFactory = createVirtualThreadFactory();
        ExecutorService executor = Executors.newThreadPerTaskExecutor(virtualThreadFactory);
        
        Semaphore semaphore = new Semaphore(maxParallelism);
        List<String> failedJobs = java.util.Collections.synchronizedList(new ArrayList<>());

        try {
            for (RenderJob job : jobs) {
                executor.submit(() -> executeJobWithSemaphore(job, renderer, semaphore, 
                                                              progressTracker, failedJobs));
            }
        } finally {
            executor.shutdown();
            awaitCompletion(executor);
        }
        
        reportFailures(failedJobs);
    }

    /**
     * Creates a virtual thread factory with custom exception handling.
     * The exception handler ensures errors are visible in GraalVM native-image.
     */
    private ThreadFactory createVirtualThreadFactory() {
        return VirtualThreadExceptionHandler.createVirtualThreadBuilder("render-worker-")
            .factory();
    }

    /**
     * Executes a single job with semaphore-based concurrency control.
     */
    private void executeJobWithSemaphore(RenderJob job, RendererService renderer,
                                         Semaphore semaphore, ProgressTracker progressTracker,
                                         List<String> failedJobs) {
        try {
            semaphore.acquire();
            try {
                logger.debug("Rendering job for text: {}", job.text());
                renderer.render(job);
                progressTracker.increment();
                logger.debug("Successfully rendered job for text: {}", job.text());
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Job interrupted for: {}", job.text());
            failedJobs.add(job.text());
        } catch (Throwable e) {
            failedJobs.add(job.text());
            handleJobError(job, e);
        }
    }

    /**
     * Handles job execution errors with comprehensive logging.
     * Logs to both SLF4J and stderr for maximum visibility in native-image.
     */
    private void handleJobError(RenderJob job, Throwable e) {
        String errorMsg = String.format("Failed to render '%s': %s: %s",
            job.text(), e.getClass().getSimpleName(), e.getMessage());
        
        logger.error(errorMsg, e);
        
        // Also print to stderr for native-image environments
        System.err.println("ERROR: " + errorMsg);
        e.printStackTrace(System.err);
    }

    /**
     * Reports summary of failed jobs if any.
     */
    private void reportFailures(List<String> failedJobs) {
        if (!failedJobs.isEmpty()) {
            logger.warn("{} job(s) failed during rendering: {}", 
                       failedJobs.size(), String.join(", ", failedJobs));
            System.err.printf("WARNING: %d job(s) failed. See errors above.%n", failedJobs.size());
        }
    }

    private void awaitCompletion(ExecutorService executor) {
        try {
            boolean completed = executor.awaitTermination(DEFAULT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!completed) {
                logger.warn("Some tasks did not complete within the timeout period");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
