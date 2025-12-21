package me.namila.project.text_render.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Custom uncaught exception handler for virtual threads.
 * Ensures that exceptions thrown in virtual threads are properly logged and visible,
 * which is especially important in GraalVM native-image environments where
 * default exception handling may not display errors correctly.
 */
public class VirtualThreadExceptionHandler implements UncaughtExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadExceptionHandler.class);
    
    private static final VirtualThreadExceptionHandler INSTANCE = new VirtualThreadExceptionHandler();

    /**
     * Returns the singleton instance of this handler.
     */
    public static VirtualThreadExceptionHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String threadName = thread.getName();
        String errorMessage = String.format(
            "Uncaught exception in thread '%s' (virtual=%s): %s",
            threadName,
            thread.isVirtual(),
            throwable.getMessage()
        );
        
        // Log to both logger and stderr for maximum visibility
        logger.error(errorMessage, throwable);
        
        // Also print to stderr for native-image environments where logging may not work
        System.err.println("ERROR: " + errorMessage);
        throwable.printStackTrace(System.err);
    }

    /**
     * Creates a ThreadFactory that produces virtual threads with this exception handler attached.
     *
     * @param namePrefix prefix for thread names
     * @return a ThreadFactory for virtual threads with exception handling
     */
    public static Thread.Builder.OfVirtual createVirtualThreadBuilder(String namePrefix) {
        return Thread.ofVirtual()
            .name(namePrefix, 0)
            .uncaughtExceptionHandler(INSTANCE);
    }
}
