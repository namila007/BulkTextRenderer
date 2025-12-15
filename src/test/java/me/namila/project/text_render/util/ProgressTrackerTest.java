package me.namila.project.text_render.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProgressTrackerTest {

    @Test
    void shouldInitializeWithTotal() {
        // Given & When
        ProgressTracker tracker = new ProgressTracker(100);

        // Then
        assertThat(tracker.getTotal()).isEqualTo(100);
        assertThat(tracker.getCompleted()).isEqualTo(0);
    }

    @Test
    void shouldIncrementCompleted() {
        // Given
        ProgressTracker tracker = new ProgressTracker(10);

        // When
        tracker.increment();
        tracker.increment();
        tracker.increment();

        // Then
        assertThat(tracker.getCompleted()).isEqualTo(3);
    }

    @Test
    void shouldBeThreadSafe() throws Exception {
        // Given
        ProgressTracker tracker = new ProgressTracker(1000);
        int numThreads = 100;
        int incrementsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // When - 100 threads each incrementing 10 times
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    tracker.increment();
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then - should have exactly 1000 increments
        assertThat(tracker.getCompleted()).isEqualTo(1000);
    }

    @Test
    void shouldCalculateProgressPercentage() {
        // Given
        ProgressTracker tracker = new ProgressTracker(4);

        // When & Then
        assertThat(tracker.getProgressPercentage()).isEqualTo(0);
        
        tracker.increment();
        assertThat(tracker.getProgressPercentage()).isEqualTo(25);
        
        tracker.increment();
        assertThat(tracker.getProgressPercentage()).isEqualTo(50);
        
        tracker.increment();
        tracker.increment();
        assertThat(tracker.getProgressPercentage()).isEqualTo(100);
    }

    @Test
    void shouldHandleZeroTotal() {
        // Given
        ProgressTracker tracker = new ProgressTracker(0);

        // Then
        assertThat(tracker.getProgressPercentage()).isEqualTo(100);
    }
}
