package me.namila.project.text_render.service;

import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.util.ProgressTracker;
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
            return;
        }

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
    }

    private void executeJob(RenderJob job, RendererService renderer, 
                           Semaphore semaphore, ProgressTracker progressTracker) {
        try {
            semaphore.acquire();
            try {
                renderer.render(job);
                progressTracker.increment();
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("Job interrupted for: %s%n", job.text());
        } catch (Exception e) {
            System.err.printf("Failed to render job for '%s': %s%n", job.text(), e.getMessage());
        }
    }

    private void awaitCompletion(ExecutorService executor) {
        try {
            boolean completed = executor.awaitTermination(DEFAULT_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            if (!completed) {
                System.err.println("Warning: Some tasks did not complete within the timeout period");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
