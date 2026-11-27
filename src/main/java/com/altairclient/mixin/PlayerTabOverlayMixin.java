package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.AltairUsers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Pone una flor de lis delante del nick de quien lleva Altair Client en la lista TAB. */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

    @Inject(method = "getNameForDisplay(Lnet/minecraft/client/multiplayer/PlayerInfo;)Lnet/minecraft/network/chat/Component;",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void altair$fleur(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        if (!AltairConfig.isTabFleur() || info == null || info.getProfile() == null) {
            return;
        }
        if (AltairUsers.isUser(info.getProfile().id())) {
            Component mark = Component.literal("⚜ ").withStyle(ChatFormatting.LIGHT_PURPLE);
            cir.setReturnValue(Component.empty().append(mark).append(cir.getReturnValue()));
        }
    }
}
