package com.altairclient.mixin;

import com.altairclient.modules.mods.RenderHacks;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps a reference to the game camera so ESP, Tracers and Trajectories use its exact position, rotation and FOV. */
@Mixin(Camera.class)
public abstract class CameraCaptureMixin {

    @Inject(method = "update", at = @At("RETURN"), require = 0)
    private void altair$captureCamera(DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderHacks.camera = (Camera) (Object) this;
    }
}
