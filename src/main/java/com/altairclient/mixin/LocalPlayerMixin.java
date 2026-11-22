package com.altairclient.mixin;

import com.altairclient.modules.mods.Hacks;

import net.minecraft.client.player.LocalPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Phase: lets the player walk through blocks while the module is on. */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At("HEAD"), require = 0)
    private void altair$phase(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (Hacks.phaseActive(self)) {
            self.noPhysics = true;
        }
    }
}
