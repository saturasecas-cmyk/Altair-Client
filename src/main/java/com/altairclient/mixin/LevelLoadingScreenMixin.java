package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.LoadingScreenArt;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Entrar a un mundo: la pantalla de carga de Altair tapa la de vanilla (mapa de chunks y barra). */
@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {

    @Shadow
    private float smoothedProgress;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("RETURN"), require = 0)
    private void altair$worldLoading(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!AltairConfig.isTitleBackground()) {
            return;
        }
        var window = net.minecraft.client.Minecraft.getInstance().getWindow();
        LoadingScreenArt.draw(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(), this.smoothedProgress, 1.0F);
    }
}
