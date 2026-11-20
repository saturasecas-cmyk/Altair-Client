package com.altairclient.mixin;

import com.altairclient.modules.mods.ClearWater;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clear Water: bajo el agua se ve mucho más lejos. */
@Mixin(WaterFogEnvironment.class)
public abstract class WaterFogMixin {

    @Inject(method = "setupFog", at = @At("RETURN"), require = 0)
    private void altair$clearWater(FogData data, Camera camera, ClientLevel level, float renderDistance,
                                   DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClearWater.isEnabled()) {
            data.environmentalStart = -8.0F;
            data.environmentalEnd = Math.max(data.environmentalEnd, Math.min(renderDistance, 128.0F));
        }
    }
}
