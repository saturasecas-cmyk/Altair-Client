package com.altairclient.mixin;

import com.altairclient.client.TitleBackground;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LogoRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sustituye el logo de Minecraft del menú principal por el de Altair Edition. */
@Mixin(LogoRenderer.class)
public abstract class LogoRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IF)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$logo3(GuiGraphicsExtractor graphics, int width, float alpha, CallbackInfo ci) {
        if (TitleBackground.drawLogo(graphics, width, alpha, LogoRenderer.DEFAULT_HEIGHT_OFFSET)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IFI)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$logo4(GuiGraphicsExtractor graphics, int width, float alpha, int heightOffset, CallbackInfo ci) {
        if (TitleBackground.drawLogo(graphics, width, alpha, heightOffset)) {
            ci.cancel();
        }
    }
}
