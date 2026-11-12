package com.altairclient.mixin;

import com.altairclient.modules.mods.ZoomHandler;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void altairclient$applyZoom(CallbackInfoReturnable<Float> cir) {
        if (Minecraft.getInstance().gui.screen() == null) {
            cir.setReturnValue(ZoomHandler.applyZoom(cir.getReturnValueF()));
        }
    }
}
