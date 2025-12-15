package me.namila.project.text_render.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe progress tracker for monitoring task completion.
 */
public class ProgressTracker {

    private final int total;
    private final AtomicInteger completed;

    public ProgressTracker(int total) {
        this.total = total;
        this.completed = new AtomicInteger(0);
    }

    /**
     * Increments the completed count by one.
     * This method is thread-safe.
     */
    public void increment() {
        int current = completed.incrementAndGet();
        printProgress(current);
    }

    /**
     * Returns the number of completed tasks.
     */
    public int getCompleted() {
        return completed.get();
    }

    /**
     * Returns the total number of tasks.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Returns the progress as a percentage (0-100).
     */
    public int getProgressPercentage() {
        if (total == 0) {
            return 100;
        }
        return (int) ((completed.get() * 100L) / total);
    }

    private void printProgress(int current) {
        int percentage = (int) ((current * 100L) / Math.max(1, total));
        System.out.printf("\rProgress: %d/%d (%d%%)%s", current, total, percentage, current == total ? "\n" : "");
    }
}
