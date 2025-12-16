package me.namila.project.text_render.service;

import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.util.ProgressTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Service for executing render jobs in parallel using Java 21+ virtual threads.
 * Uses a semaphore to limit concurrent tasks when maxParallelism is specified.
 */
@Service
public class ParallelExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(ParallelExecutorService.class);
    private static final long DEFAULT_TIMEOUT_MINUTES = 60;

    /**
     * Executes all render jobs in parallel.
     *
     * @param jobs           the list of render jobs to execute
     * @param renderer       the renderer service to use
     * @param maxParallelism the maximum number of concurrent tasks
     * @param progressTracker the progress tracker for monitoring completion
     */
    public void executeAll(List<RenderJob> jobs, RendererService renderer, 
                          int maxParallelism, ProgressTracker progressTracker) {
        if (jobs.isEmpty()) {
            logger.debug("No jobs to execute, returning");
            return;
        }

        logger.info("Starting parallel execution of {} jobs with {} threads", jobs.size(), maxParallelism);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        Semaphore semaphore = new Semaphore(maxParallelism);

        try {
            for (RenderJob job : jobs) {
                executor.submit(() -> executeJob(job, renderer, semaphore, progressTracker));
            }
        } finally {
            executor.shutdown();
            awaitCompletion(executor);
        }
        logger.info("All jobs completed");
    }

    private void executeJob(RenderJob job, RendererService renderer, 
                           Semaphore semaphore, ProgressTracker progressTracker) {
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
        } catch (Exception e) {
            logger.error("Failed to render job for '{}': {}", job.text(), e.getMessage());
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
