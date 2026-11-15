package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/** Auto Respawn: al morir, pulsa "Reaparecer" por ti. */
public final class AutoRespawn {

    private static boolean enabled = false;

    private AutoRespawn() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    /** Se llama cada tick, incluso con una pantalla abierta (la de muerte lo es). */
    public static void tick(Minecraft mc) {
        if (enabled && mc.player != null && mc.gui.screen() instanceof DeathScreen) {
            mc.player.respawn();
        }
    }
}
