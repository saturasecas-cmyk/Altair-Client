package com.altairclient.modules.mods;

import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;

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
            ViewportEvent.RenderFog.BUS.addListener(ClearWater::onFog);
            RenderBlockScreenEffectEvent.BUS.addListener(ClearWater::onOverlay);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    /** Aleja la niebla del agua. Devuelve false para no cancelar el evento. */
    private static boolean onFog(ViewportEvent.RenderFog event) {
        if (enabled && String.valueOf(event.getType()).contains("WATER")) {
            event.setFarPlaneDistance(512.0F);
            event.setNearPlaneDistance(-8.0F);
        }
        return false;
    }

    /** Cancela el filtro azul de estar sumergido. */
    private static boolean onOverlay(RenderBlockScreenEffectEvent event) {
        return enabled && String.valueOf(event.getOverlayType()).contains("WATER");
    }
}
