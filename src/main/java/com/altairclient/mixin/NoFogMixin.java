package com.altairclient.mixin;

import com.altairclient.modules.mods.RenderHacks;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NoRender > Fog: pushes the fog far away. */
@Mixin({AtmosphericFogEnvironment.class, LavaFogEnvironment.class, WaterFogEnvironment.class})
public abstract class NoFogMixin {

    @Inject(method = "setupFog", at = @At("RETURN"), require = 0)
    private void altair$noFog(FogData data, Camera camera, ClientLevel level, float renderDistance,
                              DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RenderHacks.NO_FOG.enabled) {
            data.environmentalStart = 100000.0F;
            data.environmentalEnd = 100001.0F;
            data.renderDistanceStart = 100000.0F;
            data.renderDistanceEnd = 100001.0F;
        }
    }
}
