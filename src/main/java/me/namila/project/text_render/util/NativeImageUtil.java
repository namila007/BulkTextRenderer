package me.namila.project.text_render.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for detecting GraalVM native-image runtime environment
 * and checking for AWT availability.
 */
public final class NativeImageUtil {

    private static final Logger logger = LoggerFactory.getLogger(NativeImageUtil.class);
    private static final Boolean NATIVE_IMAGE_RUNTIME;
    private static final Boolean AWT_AVAILABLE;

    static {
        NATIVE_IMAGE_RUNTIME = detectNativeImageRuntime();
        AWT_AVAILABLE = checkAwtAvailability();
        
        logger.debug("Running in native-image: {}, AWT available: {}", NATIVE_IMAGE_RUNTIME, AWT_AVAILABLE);
    }

    private NativeImageUtil() {
        // Utility class
    }

    /**
     * Detect if we're running inside a GraalVM native-image.
     * 
     * @return true if running as native-image, false if running on JVM
     */
    private static boolean detectNativeImageRuntime() {
        // Method 1: Check for GraalVM's ImageInfo class
        try {
            Class<?> imageInfoClass = Class.forName("org.graalvm.nativeimage.ImageInfo");
            Boolean inImage = (Boolean) imageInfoClass.getMethod("inImageCode").invoke(null);
            return Boolean.TRUE.equals(inImage);
        } catch (Exception e) {
            // Not running in native-image or ImageInfo not available
        }
        
        // Method 2: Check system property set by native-image
        String vendor = System.getProperty("java.vendor", "");
        String vmName = System.getProperty("java.vm.name", "");
        
        return vendor.contains("GraalVM") && vmName.contains("Substrate VM");
    }

    /**
     * Check if AWT is available in the current runtime.
     * On native-image macOS, AWT native libraries are not bundled.
     * 
     * @return true if AWT is available, false otherwise
     */
    private static boolean checkAwtAvailability() {
        if (!NATIVE_IMAGE_RUNTIME) {
            // On JVM, AWT is always available
            return true;
        }
        
        // In native-image, try to load AWT class and check for native library
        try {
            // This will trigger UnsatisfiedLinkError on macOS native-image
            // if AWT native libraries are not available
            Class.forName("java.awt.Toolkit");
            
            // On macOS native-image, even loading Toolkit class can fail
            // because it tries to load native 'awt' library in static initializer
            return true;
        } catch (UnsatisfiedLinkError e) {
            logger.warn("AWT native libraries not available: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.warn("Error checking AWT availability: {}", e.getMessage());
            return false;
        }
    }

    /**
     * @return true if running in a GraalVM native-image
     */
    public static boolean isNativeImage() {
        return NATIVE_IMAGE_RUNTIME;
    }

    /**
     * @return true if AWT (Abstract Window Toolkit) is available
     */
    public static boolean isAwtAvailable() {
        return AWT_AVAILABLE;
    }

    /**
     * Checks if the current runtime supports image formats that require AWT.
     * PNG and JPEG rendering in this application uses Java AWT (BufferedImage, Graphics2D, ImageIO).
     * 
     * @throws UnsupportedOperationException if running in native-image without AWT support
     */
    public static void requireAwtSupport() {
        if (NATIVE_IMAGE_RUNTIME && !AWT_AVAILABLE) {
            throw new UnsupportedOperationException(
                "PNG/JPEG rendering is not supported in GraalVM native-image on macOS. " +
                "AWT native libraries (libawt.dylib) are not bundled. " +
                "Workaround: Use JAR mode (java -jar bulkTextRenderer.jar) for PNG/JPEG, " +
                "or use native-image for PDF-only workflows. " +
                "See: https://github.com/oracle/graal/issues/4124"
            );
        }
    }

    /**
     * Get a descriptive string about the current runtime.
     * Useful for debugging and logging.
     * 
     * @return description of the runtime environment
     */
    public static String getRuntimeDescription() {
        if (NATIVE_IMAGE_RUNTIME) {
            return String.format("GraalVM Native Image (AWT: %s)", AWT_AVAILABLE ? "available" : "unavailable");
        } else {
            return String.format("JVM (%s %s)", 
                System.getProperty("java.vendor"), 
                System.getProperty("java.version"));
        }
    }
}
