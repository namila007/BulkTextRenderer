package me.namila.project.text_render.service;

import me.namila.project.text_render.model.Alignment;
import me.namila.project.text_render.model.RenderJob;
import me.namila.project.text_render.model.TextConfig;
import me.namila.project.text_render.util.ProgressTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ParallelExecutorServiceTest {

    private ParallelExecutorService parallelExecutorService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parallelExecutorService = new ParallelExecutorService();
    }

    @Test
    void shouldExecuteJobsInParallel() throws Exception {
        // Given
        List<Long> threadIds = new CopyOnWriteArrayList<>();
        CountDownLatch latch = new CountDownLatch(5);
        
        RendererService renderer = job -> {
            threadIds.add(Thread.currentThread().threadId());
            Thread.sleep(50); // Simulate work
            latch.countDown();
        };

        List<RenderJob> jobs = createTestJobs(5);
        ProgressTracker tracker = new ProgressTracker(5);

        // When - use threshold of 0 to force parallel execution
        long startTime = System.currentTimeMillis();
        parallelExecutorService.executeAll(jobs, renderer, 5, tracker, 0);
        latch.await(5, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        // Then - parallel execution should be faster than sequential (5 * 50ms = 250ms)
        assertThat(duration).isLessThan(200);
        assertThat(tracker.getCompleted()).isEqualTo(5);
    }

    @Test
    void shouldRespectMaxThreadLimit() throws Exception {
        // Given
        AtomicInteger concurrentTasks = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        
        RendererService renderer = job -> {
            int current = concurrentTasks.incrementAndGet();
            maxConcurrent.updateAndGet(max -> Math.max(max, current));
            Thread.sleep(100); // Simulate work
            concurrentTasks.decrementAndGet();
        };

        List<RenderJob> jobs = createTestJobs(10);
        ProgressTracker tracker = new ProgressTracker(10);

        // When - limit to 2 parallel threads
        parallelExecutorService.executeAll(jobs, renderer, 2, tracker);

        // Then - max concurrent should not exceed 2
        assertThat(maxConcurrent.get()).isLessThanOrEqualTo(2);
        assertThat(tracker.getCompleted()).isEqualTo(10);
    }

    @Test
    void shouldReportProgressAccurately() throws Exception {
        // Given
        List<Integer> progressSnapshots = Collections.synchronizedList(new ArrayList<>());
        
        RendererService renderer = job -> {
            Thread.sleep(10);
        };

        List<RenderJob> jobs = createTestJobs(5);
        ProgressTracker tracker = new ProgressTracker(5);

        // When
        parallelExecutorService.executeAll(jobs, renderer, 5, tracker);

        // Then
        assertThat(tracker.getCompleted()).isEqualTo(5);
        assertThat(tracker.getTotal()).isEqualTo(5);
    }

    @Test
    void shouldUseVirtualThreads() throws Exception {
        // Given
        List<Boolean> isVirtual = new CopyOnWriteArrayList<>();
        
        RendererService renderer = job -> {
            isVirtual.add(Thread.currentThread().isVirtual());
        };

        List<RenderJob> jobs = createTestJobs(3);
        ProgressTracker tracker = new ProgressTracker(3);

        // When - use threshold of 0 to force parallel execution with virtual threads
        parallelExecutorService.executeAll(jobs, renderer, 3, tracker, 0);

        // Then - all tasks should run on virtual threads
        assertThat(isVirtual).hasSize(3);
        assertThat(isVirtual).allMatch(v -> v);
    }

    @Test
    void shouldHandleFailedJobsGracefully() throws Exception {
        // Given
        AtomicInteger successCount = new AtomicInteger(0);
        
        RendererService renderer = job -> {
            if (job.text().contains("fail")) {
                throw new RuntimeException("Simulated failure");
            }
            successCount.incrementAndGet();
        };

        List<RenderJob> jobs = new ArrayList<>();
        jobs.add(createJob("success1"));
        jobs.add(createJob("fail-this"));
        jobs.add(createJob("success2"));
        jobs.add(createJob("fail-again"));
        jobs.add(createJob("success3"));

        ProgressTracker tracker = new ProgressTracker(5);

        // When - should not throw exception
        assertThatCode(() -> 
            parallelExecutorService.executeAll(jobs, renderer, 5, tracker)
        ).doesNotThrowAnyException();

        // Then - successful jobs should complete
        assertThat(successCount.get()).isEqualTo(3);
    }

    @Test
    void shouldHandleEmptyJobList() throws Exception {
        // Given
        List<RenderJob> jobs = Collections.emptyList();
        ProgressTracker tracker = new ProgressTracker(0);
        RendererService renderer = job -> {};

        // When & Then - should not throw
        assertThatCode(() -> 
            parallelExecutorService.executeAll(jobs, renderer, 4, tracker)
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldExecuteSequentiallyForSmallBatches() throws Exception {
        // Given - track execution thread
        List<Boolean> isMainThread = new CopyOnWriteArrayList<>();
        Thread mainThread = Thread.currentThread();
        
        RendererService renderer = job -> {
            isMainThread.add(Thread.currentThread() == mainThread);
        };

        List<RenderJob> jobs = createTestJobs(3); // Below default threshold of 10
        ProgressTracker tracker = new ProgressTracker(3);

        // When - use default threshold (should run sequentially)
        parallelExecutorService.executeAll(jobs, renderer, 3, tracker);

        // Then - all tasks should run on main thread
        assertThat(isMainThread).hasSize(3);
        assertThat(isMainThread).allMatch(v -> v);
        assertThat(tracker.getCompleted()).isEqualTo(3);
    }

    @Test
    void shouldUseCustomSequentialThreshold() throws Exception {
        // Given
        List<Boolean> isVirtual = new CopyOnWriteArrayList<>();
        
        RendererService renderer = job -> {
            isVirtual.add(Thread.currentThread().isVirtual());
        };

        List<RenderJob> jobs = createTestJobs(5);
        ProgressTracker tracker = new ProgressTracker(5);

        // When - set threshold to 3, so 5 jobs should use parallel execution
        parallelExecutorService.executeAll(jobs, renderer, 5, tracker, 3);

        // Then - should run on virtual threads (parallel mode)
        assertThat(isVirtual).hasSize(5);
        assertThat(isVirtual).allMatch(v -> v);
    }

    private List<RenderJob> createTestJobs(int count) {
        List<RenderJob> jobs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            jobs.add(createJob("Test " + i));
        }
        return jobs;
    }

    private RenderJob createJob(String text) {
        TextConfig config = new TextConfig(100, 200, Alignment.CENTER);
        return new RenderJob(
            text,
            config,
            tempDir.resolve("template.pdf"),
            tempDir.resolve("output_" + text.hashCode() + ".pdf")
        );
    }
}
