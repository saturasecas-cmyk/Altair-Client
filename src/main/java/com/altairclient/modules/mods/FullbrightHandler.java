package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

/**
 * Fullbright: sube el brillo del juego al máximo para ver bien en cuevas y de noche.
 * Es un ajuste visual del cliente (el mismo que el brillo de las opciones de vídeo),
 * no añade efectos de poción ni envía nada al servidor.
 */
public final class FullbrightHandler {

    private static final double FULL = 15.0D;

    private static boolean enabled = false;
    private static Double savedGamma = null;
    private static boolean hookAdded = false;

    private FullbrightHandler() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            restore(Minecraft.getInstance());
        }
    }

    /** Se llama cada tick: mantiene el brillo al máximo mientras esté activo. */
    public static void tick(Minecraft mc) {
        if (!enabled || mc.options == null) {
            return;
        }
        if (savedGamma == null) {
            savedGamma = mc.options.gamma().get();
            addShutdownHook(mc);
        }
        if (mc.options.gamma().get() < FULL) {
            mc.options.gamma().set(FULL);
        }
    }

    private static void restore(Minecraft mc) {
        if (savedGamma != null && mc != null && mc.options != null) {
            mc.options.gamma().set(savedGamma);
        }
        savedGamma = null;
    }

    /** Si cierras el juego con Fullbright activo, se devuelve el brillo original para que no se quede guardado al máximo. */
    private static void addShutdownHook(Minecraft mc) {
        if (hookAdded) {
            return;
        }
        hookAdded = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (savedGamma != null) {
                    mc.options.gamma().set(savedGamma);
                    mc.options.save();
                }
            } catch (Throwable ignored) {
            }
        }, "Altair-FullbrightRestore"));
    }
}
