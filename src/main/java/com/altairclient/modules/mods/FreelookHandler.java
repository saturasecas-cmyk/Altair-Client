package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Freelook: con el módulo activado, el ratón mueve solo la cámara y el jugador
 * conserva la rotación con la que empezó. Se activa y desactiva desde el menú de
 * módulos o con la tecla "Freelook" de los controles de Minecraft (ambos cambian
 * el mismo estado).
 *
 * Es puramente visual y del lado del cliente: se devuelve la rotación del jugador
 * a su valor bloqueado antes de cada tick, así que al servidor no le llega ningún
 * giro y no depende de que el servidor lo permita.
 */
public class FreelookHandler {

    private static boolean enabled = false;
    private static boolean initialized = false;

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
        // Al activar se empieza desde donde mira el jugador; al desactivar la cámara vuelve a él.
        initialized = false;
    }

    public static void toggle() {
        setEnabled(!enabled);
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
     * Devuelve true si el freelook está aplicándose ahora mismo.
     */
    public static boolean absorb() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || !enabled) {
            return false;
        }

        // Con un menú abierto no se aplica, pero se conserva la posición de la cámara.
        if (mc.gui.screen() != null) {
            return false;
        }

        if (!initialized) {
            initialized = true;
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
