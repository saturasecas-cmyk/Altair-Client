package com.altairclient.mixin;

import com.altairclient.modules.mods.Hacks;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hitboxes: makes other entities easier to hit by growing the area the crosshair checks. */
@Mixin(Entity.class)
public abstract class EntityPickRadiusMixin {

    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$hitboxes(CallbackInfoReturnable<Float> cir) {
        float extra = Hacks.hitboxExpansion();
        if (extra > 0.0F && !((Object) this instanceof LocalPlayer)) {
            cir.setReturnValue(extra);
        }
    }
}
