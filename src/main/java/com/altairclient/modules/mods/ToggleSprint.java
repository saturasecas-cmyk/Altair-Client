package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

/** Toggle Sprint: corres automáticamente mientras avanzas. */
public final class ToggleSprint {

    private static boolean enabled = false;
    private static boolean forced = false;

    private ToggleSprint() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            release(Minecraft.getInstance());
        }
    }

    /** Se llama antes de cada tick del cliente, antes de que el jugador lea los controles. */
    public static void tick(Minecraft mc) {
        if (!enabled || mc.player == null || mc.screen != null || FreecamHandler.isActive()) {
            release(mc);
            return;
        }

        if (mc.options.keyUp.isDown()) {
            mc.options.keySprint.setDown(true);
            forced = true;
        } else {
            release(mc);
        }
    }

    private static void release(Minecraft mc) {
        if (forced) {
            mc.options.keySprint.setDown(false);
            forced = false;
        }
    }
}
