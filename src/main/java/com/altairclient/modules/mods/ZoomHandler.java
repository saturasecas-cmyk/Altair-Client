
package com.altairclient.modules.mods;

public class ZoomHandler {

    private static final float ZOOM_FACTOR = 0.25F;

    private static boolean enabled = true;
    private static boolean zoomKeyDown = false;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;

        if (!enabled) {
            zoomKeyDown = false;
        }
    }

    public static void setZoomKeyDown(boolean value) {
        zoomKeyDown = value;
    }

    public static boolean isZoomKeyDown() {
        return zoomKeyDown;
    }

    public static float applyZoom(float fov) {
        return enabled && zoomKeyDown ? fov * ZOOM_FACTOR : fov;
    }
}
