package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

/** Toggle Sneak: te mantienes agachado automáticamente hasta que lo desactives. */
public final class ToggleSneak {

    private static boolean enabled = false;
    private static boolean forced = false;

    private ToggleSneak() {
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
        // Con un menú abierto o en Freecam (donde Mayús baja la cámara) no se fuerza.
        if (!enabled || mc.player == null || mc.screen != null || FreecamHandler.isActive()) {
            release(mc);
            return;
        }

        mc.options.keyShift.setDown(true);
        forced = true;
    }

    private static void release(Minecraft mc) {
        if (forced) {
            mc.options.keyShift.setDown(false);
            forced = false;
        }
    }
}
