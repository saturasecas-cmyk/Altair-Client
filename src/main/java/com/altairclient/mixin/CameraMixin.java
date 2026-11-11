package com.altairclient.mixin;

import com.altairclient.modules.mods.ZoomHandler;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float altairclient$applyZoom(float fov) {
        return Minecraft.getInstance().gui.screen() == null
                ? ZoomHandler.applyZoom(fov)
                : fov;
    }
}
