package com.altairclient.mixin;

import com.altairclient.modules.textures.PvpTextures;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Minimal Totem: la animación del tótem al revivir sale más pequeña. */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "renderItemActivationAnimation", at = @At("HEAD"), require = 0)
    private void altair$shrinkTotem(PoseStack poseStack, float partialTick, SubmitNodeCollector collector, CallbackInfo ci) {
        float scale = PvpTextures.totemScale();
        if (scale != 1.0F) {
            poseStack.scale(scale, scale, scale);
        }
    }
}
