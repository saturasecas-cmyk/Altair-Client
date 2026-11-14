package com.altairclient.modules.mods;

import com.altairclient.AltairClient;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * Dibuja las cajas de colisión de las entidades en color morado Altair.
 *
 * En Minecraft 26.x las hitboxes de vanilla (F3 + B) se dibujan con el sistema de "gizmos" y con
 * colores fijos, así que aquí se emiten gizmos propios cada frame. Se respeta la profundidad (no se
 * ven a través de bloques), igual que las hitboxes de vanilla.
 */
public class HitboxDisplay {

    private static final HitboxDisplay INSTANCE = new HitboxDisplay();

    /** Morado del cliente (ARGB). */
    private static final int PURPLE = 0xFF9B7BFF;
    private static final GizmoStyle STYLE = GizmoStyle.stroke(PURPLE);

    private static final double MAX_DISTANCE = 128.0D;
    private static final double MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE;

    private boolean enabled = false;

    private HitboxDisplay() {
    }

    public static HitboxDisplay getInstance() {
        return INSTANCE;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Se llama una vez por frame desde una capa del HUD. */
    public void emit(DeltaTracker deltaTracker) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            return;
        }

        try {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            Camera camera = mc.gameRenderer.mainCamera();
            Entity cameraEntity = camera.entity();
            boolean hideCameraEntity = !camera.isDetached();

            // Instala el colector de gizmos del frame; se restaura el anterior al cerrar.
            try (Gizmos.TemporaryCollection ignored = mc.levelRenderer.collectPerFrameRenderThreadGizmos()) {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity == cameraEntity && hideCameraEntity) {
                        continue;
                    }
                    if (mc.player.distanceToSqr(entity) > MAX_DISTANCE_SQR) {
                        continue;
                    }

                    double dx = Mth.lerp((double) partialTick, entity.xo, entity.getX()) - entity.getX();
                    double dy = Mth.lerp((double) partialTick, entity.yo, entity.getY()) - entity.getY();
                    double dz = Mth.lerp((double) partialTick, entity.zo, entity.getZ()) - entity.getZ();

                    AABB box = entity.getBoundingBox().move(dx, dy, dz);
                    Gizmos.cuboid(box, STYLE);
                }
            }
        } catch (Throwable t) {
            // Evita bloquear el juego si cambia la API interna: se desactiva y se avisa una vez.
            enabled = false;
            AltairClient.LOGGER.error("Hitbox Display desactivado por un error al dibujar", t);
        }
    }
}
