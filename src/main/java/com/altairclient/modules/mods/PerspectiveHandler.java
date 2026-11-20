package com.altairclient.modules.mods;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

/**
 * Perspective Mod: mientras mantienes la tecla, la cámara pasa a tercera persona y el
 * ratón la mueve alrededor de tu jugador sin girarlo. Al soltar, vuelve todo a como estaba.
 *
 * Es visual y del lado del cliente: la rotación del jugador se devuelve a su valor antes
 * de cada tick, así que al servidor no le llega ningún giro.
 */
public final class PerspectiveHandler {

    private static boolean enabled = true;
    private static boolean keyDown = false;

    private static boolean active = false;
    private static CameraType savedType = null;

    private static float lockedYaw;
    private static float lockedPitch;
    private static float camYaw;
    private static float camPitch;

    private PerspectiveHandler() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static void setKeyDown(boolean value) {
        keyDown = value;
    }

    /** true cuando CameraMixin ya está moviendo la cámara (entonces no se usa el evento de cámara). */
    private static boolean cameraMixinActive = false;
    private static boolean rotationSwapped = false;

    public static boolean isCameraMixinActive() {
        return cameraMixinActive;
    }

    /**
     * Lo llama CameraMixin justo antes de calcular la posición de la cámara: absorbe el giro del ratón
     * y deja puesta en el jugador la rotación de la cámara orbital para que la cámara orbite de verdad.
     */
    public static void beginCamera(Entity cameraEntity) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || cameraEntity != player) {
            return;
        }
        cameraMixinActive = true;
        if (!absorb()) {
            return;
        }
        player.setYRot(camYaw);
        player.setXRot(camPitch);
        player.yRotO = camYaw;
        player.xRotO = camPitch;
        rotationSwapped = true;
    }

    /** Devuelve al jugador su rotación real tras calcular la cámara. */
    public static void endCamera(Entity cameraEntity) {
        if (!rotationSwapped) {
            return;
        }
        rotationSwapped = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.setYRot(lockedYaw);
        player.setXRot(lockedPitch);
        player.yRotO = lockedYaw;
        player.xRotO = lockedPitch;
    }

    public static float getYawOffset() {
        return camYaw - lockedYaw;
    }

    public static float getPitchOffset() {
        return camPitch - lockedPitch;
    }

    /**
     * Pasa el giro del ratón a la cámara y devuelve al jugador a su rotación bloqueada.
     * Se llama cada frame y al inicio de cada tick. Devuelve true mientras está aplicándose.
     */
    public static boolean absorb() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        boolean wanted = player != null && enabled && keyDown
                && mc.gui.screen() == null && !FreecamHandler.isActive();

        if (!wanted) {
            if (active) {
                active = false;
                if (savedType != null) {
                    mc.options.setCameraType(savedType);
                    savedType = null;
                }
            }
            return false;
        }

        if (!active) {
            active = true;
            lockedYaw = player.getYRot();
            lockedPitch = player.getXRot();
            camYaw = lockedYaw;
            camPitch = lockedPitch;

            savedType = mc.options.getCameraType();
            if (savedType == CameraType.FIRST_PERSON) {
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else {
                savedType = null; // ya estaba en tercera persona: no se cambia al soltar
            }
            return true;
        }

        camYaw += player.getYRot() - lockedYaw;
        camPitch = Math.max(-90.0F, Math.min(90.0F, camPitch + (player.getXRot() - lockedPitch)));

        player.setYRot(lockedYaw);
        player.setXRot(lockedPitch);
        player.yRotO = lockedYaw;
        player.xRotO = lockedPitch;
        return true;
    }
}
