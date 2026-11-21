package com.altairclient.mixin;

import com.altairclient.modules.mods.FullbrightHandler;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fullbright: el mapa de luz se rellena como si tuvieras visión nocturna al máximo. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapMixin {

    @Inject(method = "extract", at = @At("RETURN"), require = 0)
    private void altair$fullbright(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (FullbrightHandler.isEnabled()) {
            state.needsUpdate = true;
            state.nightVisionEffectIntensity = 1.0F;
            state.nightVisionColor = new Vector3f(1.0F, 1.0F, 1.0F);
            state.darknessEffectScale = 0.0F;
            state.bossOverlayWorldDarkening = 0.0F;
        }
    }
}
