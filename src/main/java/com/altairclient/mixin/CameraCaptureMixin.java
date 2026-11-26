package com.altairclient.mixin;

import com.altairclient.modules.mods.RenderHacks;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps a reference to the game camera so ESP, Tracers and Trajectories use its exact position, rotation and FOV. */
@Mixin(Camera.class)
public abstract class CameraCaptureMixin {

    @Inject(method = "setup", at = @At("RETURN"), require = 0)
    private void altair$captureCamera(BlockGetter level, Entity entity, boolean detached, boolean mirrored, float partialTick, CallbackInfo ci) {
        RenderHacks.camera = (Camera) (Object) this;
    }
}
