package com.altairclient.modules.mods;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class HitboxDisplay {

    private static final HitboxDisplay INSTANCE = new HitboxDisplay();

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

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!INSTANCE.enabled) {
            return;
        }

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick();

        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == null) {
                continue;
            }

            if (entity == mc.player && mc.options.getCameraType().isFirstPerson()) {
                continue;
            }

            AABB box = entity.getBoundingBox();

            // Paleta cósmica Altair
            float r, g, b, a;
            if (entity instanceof ItemEntity) {
                r = 0.384F; g = 0.784F; b = 1.0F; a = 0.95F;
            } else if (entity instanceof Player) {
                r = 0.608F; g = 0.482F; b = 1.0F; a = 1.0F;
            } else if (entity instanceof Monster) {
                r = 0.88F; g = 0.35F; b = 0.98F; a = 1.0F;
            } else {
                r = 0.72F; g = 0.88F; b = 1.0F; a = 0.85F;
            }

            // Caja de colisión
            LevelRenderer.renderLineBox(poseStack, buffer, box, r, g, b, a);

            // Vector de mirada y altura de ojos para entidades vivas
            if (entity instanceof LivingEntity living) {
                Vec3 eyePos = living.getEyePosition(partialTick);
                Vec3 viewVec = living.getViewVector(partialTick);
                Vec3 lookTarget = eyePos.add(viewVec.scale(2.0));

                double eyeY = eyePos.y;
                LevelRenderer.renderLineBox(
                        poseStack, buffer,
                        box.minX, eyeY, box.minZ,
                        box.maxX, eyeY, box.maxZ,
                        1.0F, 0.32F, 0.45F, 1.0F
                );

                LevelRenderer.renderLineBox(
                        poseStack, buffer,
                        eyePos.x, eyePos.y, eyePos.z,
                        lookTarget.x, lookTarget.y, lookTarget.z,
                        0.384F, 0.85F, 1.0F, 1.0F
                );

                LevelRenderer.renderLineBox(
                        poseStack, buffer,
                        lookTarget.x - 0.025, lookTarget.y - 0.025, lookTarget.z - 0.025,
                        lookTarget.x + 0.025, lookTarget.y + 0.025, lookTarget.z + 0.025,
                        1.0F, 1.0F, 1.0F, 1.0F
                );
            }
        }

        poseStack.popPose();
        mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
    }
}
