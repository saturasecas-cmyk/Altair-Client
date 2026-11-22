package com.altairclient.mixin;

import com.altairclient.modules.mods.Hacks;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Blink: holds back movement packets while the module is on. */
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$blink(Packet<?> packet, CallbackInfo ci) {
        if (Hacks.captureBlink(packet)) {
            ci.cancel();
        }
    }
}
