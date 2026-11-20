package com.altairclient.mixin;

import com.altairclient.modules.textures.PvpTextures;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clear Lava: la niebla de la lava deja ver mucho más lejos. */
@Mixin(LavaFogEnvironment.class)
public abstract class LavaFogMixin {

    @Inject(method = "setupFog", at = @At("RETURN"), require = 0)
    private void altair$clearLava(FogData data, Camera camera, ClientLevel level, float renderDistance,
                                  DeltaTracker deltaTracker, CallbackInfo ci) {
        if (PvpTextures.isClearLava()) {
            data.environmentalStart = -8.0F;
            data.environmentalEnd = Math.max(data.environmentalEnd, 48.0F);
        }
    }
}
