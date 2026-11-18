package com.altairclient.mixin;

import com.altairclient.modules.mods.PerspectiveHandler;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Freelook / Perspective Mod: mientras se calcula la posición de la cámara, el jugador tiene la rotación
 * de la cámara orbital (así la cámara da la vuelta de 360 grados alrededor de él y puede verle la cara).
 * Al terminar, el jugador recupera su rotación real.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private Entity entity;

    @Inject(method = "alignWithEntity", at = @At("HEAD"), require = 0)
    private void altair$orbitStart(float partialTick, CallbackInfo ci) {
        PerspectiveHandler.beginCamera(this.entity);
    }

    @Inject(method = "alignWithEntity", at = @At("RETURN"), require = 0)
    private void altair$orbitEnd(float partialTick, CallbackInfo ci) {
        PerspectiveHandler.endCamera(this.entity);
    }
}
