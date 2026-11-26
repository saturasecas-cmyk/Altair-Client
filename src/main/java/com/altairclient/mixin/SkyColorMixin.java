package com.altairclient.mixin;

import com.altairclient.modules.mods.AltairSky;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Altair Sky: cielo azul marino en el Overworld. */
@Mixin(ClientLevel.class)
public abstract class SkyColorMixin {

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true, require = 0)
    private void altair$sky(Vec3 pos, float partialTick, CallbackInfoReturnable<Vec3> cir) {
        if (AltairSky.isEnabled() && ((ClientLevel) (Object) this).dimension() == Level.OVERWORLD) {
            int c = AltairSky.ZENITH;
            cir.setReturnValue(new Vec3(((c >> 16) & 255) / 255.0D, ((c >> 8) & 255) / 255.0D, (c & 255) / 255.0D));
        }
    }
}
