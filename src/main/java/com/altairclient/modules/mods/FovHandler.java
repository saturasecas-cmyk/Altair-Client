package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

/**
 * Custom FOV y Dynamic FOV.
 *
 * - Custom FOV: usa tu propio campo de visión en lugar del de las opciones de vídeo.
 * - Dynamic FOV: si está activado, el FOV sigue cambiando con el sprint, la velocidad,
 *   etc. Si se desactiva, el FOV se queda fijo.
 */
public final class FovHandler {

    public static final int MIN_FOV = 30;
    public static final int MAX_FOV = 110;

    private static boolean customEnabled = false;
    private static boolean dynamicEnabled = true;
    private static int customFov = 90;

    private FovHandler() {
    }

    public static boolean isCustomEnabled() {
        return customEnabled;
    }

    public static void toggleCustom() {
        customEnabled = !customEnabled;
    }

    public static boolean isDynamicEnabled() {
        return dynamicEnabled;
    }

    public static void toggleDynamic() {
        dynamicEnabled = !dynamicEnabled;
    }

    public static int getCustomFov() {
        return customFov;
    }

    public static void setCustomFov(int value) {
        customFov = Math.max(MIN_FOV, Math.min(MAX_FOV, value));
    }

    /** Recibe el FOV calculado por el juego (en grados) y devuelve el que se debe usar. */
    public static float apply(float fov) {
        if (!customEnabled && dynamicEnabled) {
            return fov;
        }

        float configured = Minecraft.getInstance().options.fov().get();
        float base = customEnabled ? customFov : configured;

        if (dynamicEnabled) {
            // Se conservan los cambios dinámicos, escalados al FOV personalizado.
            return configured > 0 ? fov * base / configured : fov;
        }
        return base;
    }
}
