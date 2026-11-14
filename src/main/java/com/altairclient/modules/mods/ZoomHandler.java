package com.altairclient.modules.mods;

public class ZoomHandler {

    /** Con la tecla pulsada el FOV se reduce a este factor (0.25 = x4 de zoom). */
    private static final float ZOOM_FACTOR = 0.25F;

    /** Velocidad de la transición; más alto = más rápido. */
    private static final float SMOOTH_SPEED = 14.0F;

    private static boolean enabled = true;
    private static boolean zoomKeyDown = false;

    private static float currentFactor = 1.0F;
    private static long lastNanos = 0L;

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

    /** Recibe el FOV en grados y devuelve el FOV con el zoom (suavizado) aplicado. */
    public static float applyZoom(float fov) {
        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0.016F : Math.min((now - lastNanos) / 1.0E9F, 0.1F);
        lastNanos = now;

        float target = enabled && zoomKeyDown ? ZOOM_FACTOR : 1.0F;
        float alpha = 1.0F - (float) Math.exp(-dt * SMOOTH_SPEED);
        currentFactor += (target - currentFactor) * alpha;

        if (Math.abs(target - currentFactor) < 0.001F) {
            currentFactor = target;
        }

        return fov * currentFactor;
    }
}
