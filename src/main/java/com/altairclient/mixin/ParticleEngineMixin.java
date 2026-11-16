package com.altairclient.mixin;

import com.altairclient.modules.textures.PvpTextures;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Partículas: quita parte de ellas y tiñe las que quedan con la paleta de Altair. */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$dropParticle(ParticleOptions options, double x, double y, double z,
                                     double xa, double ya, double za, CallbackInfoReturnable<Particle> cir) {
        if (PvpTextures.shouldDropParticle(options)) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "createParticle", at = @At("RETURN"), require = 0)
    private void altair$tintParticle(ParticleOptions options, double x, double y, double z,
                                     double xa, double ya, double za, CallbackInfoReturnable<Particle> cir) {
        Particle particle = cir.getReturnValue();
        if (particle != null) {
            PvpTextures.tintParticle(particle, options);
        }
    }
}
