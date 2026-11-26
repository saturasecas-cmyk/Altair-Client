package com.altairclient.mixin;

import com.altairclient.modules.mods.AltairSky;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Altair Sky: estrellas siempre visibles (solo en el cliente). */
@Mixin(Level.class)
public abstract class StarBrightnessMixin {

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true, require = 0)
    private void altair$stars(float partialTick, CallbackInfoReturnable<Float> cir) {
        Object self = this;
        if (self instanceof ClientLevel level && AltairSky.isEnabled() && level.dimension() == Level.OVERWORLD) {
            cir.setReturnValue(1.0F);
        }
    }
}
