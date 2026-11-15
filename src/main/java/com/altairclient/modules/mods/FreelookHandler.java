package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Freelook: mientras mantienes la tecla, el ratón mueve solo la cámara y el
 * jugador conserva la rotación con la que empezó.
 *
 * Es puramente visual y del lado del cliente: se devuelve la rotación del jugador
 * a su valor bloqueado antes de cada tick, así que al servidor no le llega ningún
 * giro y no depende de que el servidor lo permita.
 */
public class FreelookHandler {

    private static boolean enabled = true;
    private static boolean keyDown = false;

    private static boolean active = false;
    private static float lockedYaw;
    private static float lockedPitch;
    private static float camYaw;
    private static float camPitch;

    private FreelookHandler() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            keyDown = false;
            active = false;
        }
    }

    public static void toggle() {
        setEnabled(!enabled);
    }

    public static void setKeyDown(boolean value) {
        keyDown = value;
        if (!value) {
            active = false;
        }
    }

    public static boolean isActive() {
        return active;
    }

    /** Desfase de la cámara respecto a la rotación (bloqueada) del jugador. */
    public static float getYawOffset() {
        return camYaw - lockedYaw;
    }

    public static float getPitchOffset() {
        return camPitch - lockedPitch;
    }

    /**
     * Pasa el giro que el ratón haya aplicado al jugador a la cámara y devuelve al
     * jugador a su rotación bloqueada. Se llama cada frame y al inicio de cada tick.
     * Devuelve true si el freelook está activo.
     */
    public static boolean absorb() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || !enabled || !keyDown || mc.gui.screen() != null) {
            active = false;
            return false;
        }

        if (!active) {
            active = true;
            lockedYaw = player.getYRot();
            lockedPitch = player.getXRot();
            camYaw = lockedYaw;
            camPitch = lockedPitch;
            return true;
        }

        float dYaw = player.getYRot() - lockedYaw;
        float dPitch = player.getXRot() - lockedPitch;

        camYaw += dYaw;
        camPitch = Math.max(-90.0F, Math.min(90.0F, camPitch + dPitch));

        player.setYRot(lockedYaw);
        player.setXRot(lockedPitch);
        player.yRotO = lockedYaw;
        player.xRotO = lockedPitch;
        return true;
    }
}
