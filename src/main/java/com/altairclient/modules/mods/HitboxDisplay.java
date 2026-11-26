package com.altairclient.modules.mods;

import com.altairclient.AltairClient;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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

    public static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(HitboxDisplay::onRender);
    }

    private static void onRender(net.minecraftforge.client.event.RenderLevelStageEvent event) {
        HitboxDisplay self = INSTANCE;
        if (!self.enabled || event.getStage() != net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        try {
            float partialTick = event.getPartialTick();
            Camera camera = event.getCamera();
            Vec3 cam = camera.getPosition();
            Entity cameraEntity = mc.getCameraEntity();
            boolean hideCameraEntity = !camera.isDetached();
            com.mojang.blaze3d.vertex.PoseStack ps = new com.mojang.blaze3d.vertex.PoseStack();
            ps.mulPose(event.getPoseStack());
            net.minecraft.client.renderer.MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            com.mojang.blaze3d.vertex.VertexConsumer vc = buffers.getBuffer(net.minecraft.client.renderer.RenderType.lines());
            float r = ((PURPLE >> 16) & 255) / 255.0F;
            float g = ((PURPLE >> 8) & 255) / 255.0F;
            float b = (PURPLE & 255) / 255.0F;
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity == cameraEntity && hideCameraEntity) {
                    continue;
                }
                if (mc.player.distanceToSqr(entity) > MAX_DISTANCE_SQR) {
                    continue;
                }
                double dx = Mth.lerp((double) partialTick, entity.xo, entity.getX()) - entity.getX() - cam.x;
                double dy = Mth.lerp((double) partialTick, entity.yo, entity.getY()) - entity.getY() - cam.y;
                double dz = Mth.lerp((double) partialTick, entity.zo, entity.getZ()) - entity.getZ() - cam.z;
                AABB box = entity.getBoundingBox().move(dx, dy, dz);
                net.minecraft.client.renderer.LevelRenderer.renderLineBox(ps, vc, box, r, g, b, 1.0F);
            }
            buffers.endBatch(net.minecraft.client.renderer.RenderType.lines());
        } catch (Throwable t) {
            self.enabled = false;
            AltairClient.LOGGER.error("Hitbox Display disabled because of a rendering error", t);
        }
    }
}
