package com.altairclient.modules.mods;

import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;

/**
 * Clear Water: bajo el agua se ve lejos y sin el filtro azul de la pantalla.
 * No cambia el aspecto de la superficie del agua (eso lo decide el paquete de texturas).
 */
public final class ClearWater {

    private static boolean enabled = false;
    private static boolean registered = false;

    private ClearWater() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ClearWater::onOverlay);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    /** Cancela el filtro azul de estar sumergido. */
    private static void onOverlay(RenderBlockScreenEffectEvent event) {
        if (enabled && String.valueOf(event.getOverlayType()).contains("WATER")) {
            event.setCanceled(true);
        }
    }
}
