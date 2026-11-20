package com.altairclient.mixin;

import com.altairclient.client.TitleBackground;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** La pantalla de título dibuja su propio fondo, así que se sustituye el panorama por el cielo de Altair. */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Inject(method = "extractBackground", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$background(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (TitleBackground.drawForTitle(g, self.width, self.height)) {
            ci.cancel();
        }
    }
}
