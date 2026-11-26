package com.altairclient.modules.mods;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;

/**
 * Freecam: tu jugador se queda quieto (como en supervivencia) y la cámara vuela
 * libre como en modo espectador.
 *
 * Todo es del lado del cliente: la cámara es una entidad auxiliar que nunca se
 * añade al mundo, y los controles del jugador real se anulan mientras dura, así
 * que al servidor no le llega ningún movimiento ni giro.
 *
 * Controles: W/A/S/D para volar, Espacio para subir, Mayús para bajar, Ctrl para ir rápido.
 */
public class FreecamHandler {

    private static final double SPEED = 0.55D;       // bloques por tick
    private static final double SPRINT_FACTOR = 2.5D;

    private static boolean active = false;
    private static Marker camera = null;
    private static Input savedInput = null;
    private static CameraType savedCameraType = null;

    private static float lockedYaw;
    private static float lockedPitch;
    private static float camYaw;
    private static float camPitch;

    private FreecamHandler() {
    }

    public static boolean isActive() {
        return active;
    }

    public static void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public static void enable() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (active || player == null || mc.level == null) {
            return;
        }

        lockedYaw = player.getYRot();
        lockedPitch = player.getXRot();
        camYaw = lockedYaw;
        camPitch = lockedPitch;

        Vec3 eye = player.getEyePosition();
        // Entidad auxiliar (nunca se añade al mundo). La cámara se coloca a la altura de los ojos,
        // así que se resta su altura de ojos para que coincida con la posición del jugador.
        camera = new Marker(player.getType(), mc.level);
        double feetY = eye.y - camera.getEyeHeight();
        placeCamera(eye.x, feetY, eye.z);
        camera.xo = camera.xOld = eye.x;
        camera.yo = camera.yOld = feetY;
        camera.zo = camera.zOld = eye.z;

        // Los controles del jugador real se anulan: se queda quieto.
        savedInput = player.input;
        player.input = new Input();

        savedCameraType = mc.options.getCameraType();
        mc.options.setCameraType(CameraType.FIRST_PERSON);

        mc.setCameraEntity(camera);
        active = true;
    }

    public static void disable() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player != null) {
            if (savedInput != null) {
                player.input = savedInput;
            }
            mc.setCameraEntity(player);
        }
        if (savedCameraType != null) {
            mc.options.setCameraType(savedCameraType);
        }

        reset();
    }

    private static void reset() {
        active = false;
        camera = null;
        savedInput = null;
        savedCameraType = null;
    }

    private static void placeCamera(double x, double y, double z) {
        camera.setPos(x, y, z);
        camera.setYRot(camYaw);
        camera.setXRot(camPitch);
        camera.yRotO = camYaw;
        camera.xRotO = camPitch;
    }

    /** Mundo o jugador distintos al de cuando se activó (muerte, cambio de dimensión, desconexión). */
    private static boolean isStale(Minecraft mc) {
        return mc.player == null || mc.level == null || camera == null || camera.level() != mc.level;
    }

    /**
     * Pasa el giro del ratón a la cámara y devuelve al jugador a su rotación bloqueada.
     * Se llama cada frame y al inicio de cada tick. Devuelve true si el freecam está activo.
     */
    public static boolean absorbRotation() {
        Minecraft mc = Minecraft.getInstance();
        if (!active) {
            return false;
        }
        if (isStale(mc)) {
            reset();
            return false;
        }
        if (mc.screen != null) {
            return true;
        }

        LocalPlayer player = mc.player;
        camYaw += player.getYRot() - lockedYaw;
        camPitch = Math.max(-90.0F, Math.min(90.0F, camPitch + (player.getXRot() - lockedPitch)));

        player.setYRot(lockedYaw);
        player.setXRot(lockedPitch);
        player.yRotO = lockedYaw;
        player.xRotO = lockedPitch;

        camera.setYRot(camYaw);
        camera.setXRot(camPitch);
        camera.yRotO = camYaw;
        camera.xRotO = camPitch;
        return true;
    }

    public static float getCameraYaw() {
        return camYaw;
    }

    public static float getCameraPitch() {
        return camPitch;
    }

    /** Mueve la cámara según las teclas de movimiento. Se llama una vez por tick. */
    public static void tickMovement() {
        Minecraft mc = Minecraft.getInstance();
        if (!active || isStale(mc) || mc.screen != null) {
            return;
        }

        double forward = 0;
        double strafe = 0;
        double vertical = 0;
        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;
        if (mc.options.keyJump.isDown()) vertical += 1;
        if (mc.options.keyShift.isDown()) vertical -= 1;

        double speed = SPEED * (mc.options.keySprint.isDown() ? SPRINT_FACTOR : 1.0D);

        double yawRad = Math.toRadians(camYaw);
        double sin = Math.sin(yawRad);
        double cos = Math.cos(yawRad);

        // En Minecraft, yaw 0 mira hacia +Z y el yaw positivo gira hacia -X.
        double dx = (-sin * forward + cos * strafe);
        double dz = (cos * forward + sin * strafe);
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 1.0E-6) {
            dx = dx / len * speed;
            dz = dz / len * speed;
        } else {
            dx = 0;
            dz = 0;
        }
        double dy = vertical * speed;

        camera.xo = camera.xOld = camera.getX();
        camera.yo = camera.yOld = camera.getY();
        camera.zo = camera.zOld = camera.getZ();
        camera.setPos(camera.getX() + dx, camera.getY() + dy, camera.getZ() + dz);
    }
}
