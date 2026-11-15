package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

/**
 * No Hurt Camera: la cámara no se sacude al recibir daño. Se anula el contador de
 * "golpe recibido" de tu jugador en el cliente, que es lo que mueve la cámara.
 */
public final class NoHurtCamera {

    private static boolean enabled = false;

    private NoHurtCamera() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void apply(Minecraft mc) {
        if (enabled && mc.player != null) {
            mc.player.hurtTime = 0;
        }
    }
}
