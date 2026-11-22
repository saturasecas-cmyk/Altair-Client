package com.altairclient.mixin;

import com.altairclient.modules.mods.RenderHacks;

import net.minecraft.world.entity.Entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Chams: players and mobs get an outline you can see through walls. */
@Mixin(Entity.class)
public abstract class EntityGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$chams(CallbackInfoReturnable<Boolean> cir) {
        if (RenderHacks.glows((Entity) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
