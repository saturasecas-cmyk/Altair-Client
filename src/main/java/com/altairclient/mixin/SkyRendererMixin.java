package com.altairclient.mixin;

import com.altairclient.modules.mods.AltairSky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Altair Sky: cielo azul marino con estrellas siempre visibles y más densas. */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @Unique
    private static boolean altair$drawingExtraStars = false;

    @Shadow
    private void renderStars(float brightness, PoseStack poseStack) {
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
    private void altair$skyState(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
        if (!AltairSky.isEnabled() || !"OVERWORLD".equalsIgnoreCase(String.valueOf(state.skybox))) {
            return;
        }
        state.skyColor = AltairSky.ZENITH;
        state.starBrightness = 1.0F;
        state.sunriseAndSunsetColor = 0;
    }

    /** Con Altair Sky no se dibuja el sol (la luz del día sigue igual). */
    @Inject(method = "renderSun", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$noSun(float angle, PoseStack poseStack, CallbackInfo ci) {
        if (AltairSky.isEnabled()) {
            ci.cancel();
        }
    }

    /** Con Altair Sky no se dibuja la luna (la luz de la noche sigue igual). */
    @Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$noMoon(MoonPhase phase, float angle, PoseStack poseStack, CallbackInfo ci) {
        if (AltairSky.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStars", at = @At("RETURN"), require = 0)
    private void altair$moreStars(float brightness, PoseStack poseStack, CallbackInfo ci) {
        if (!AltairSky.isEnabled() || altair$drawingExtraStars) {
            return;
        }
        int layers = AltairSky.extraStarLayers();
        if (layers == 0) {
            return;
        }
        altair$drawingExtraStars = true;
        try {
            for (int i = 1; i <= layers; i++) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.ZP.rotationDegrees(37.0F * i));
                poseStack.mulPose(Axis.XP.rotationDegrees(53.0F * i));
                this.renderStars(brightness, poseStack);
                poseStack.popPose();
            }
        } finally {
            altair$drawingExtraStars = false;
        }
    }
}
