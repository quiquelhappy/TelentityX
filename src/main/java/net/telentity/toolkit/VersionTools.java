package net.telentity.toolkit;

public class VersionTools {

    /** Returns true when the Canvas API is present on the classpath. */
    public static boolean isCanvas() {
        try {
            Class.forName("io.canvasmc.canvas.event.EntityPostTeleportAsyncEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
