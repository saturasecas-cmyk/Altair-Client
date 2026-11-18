package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.LoadingScreenArt;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Carga del juego: la pantalla de Altair se dibuja encima de la de Mojang (que sigue haciendo su trabajo debajo). */
@Mixin(LoadingOverlay.class)
public abstract class LoadingOverlayMixin {

    @Shadow
    private float currentProgress;

    @Shadow
    private long fadeOutStart;

    /** Momento (reloj normal) en que vimos empezar el fundido de salida. */
    @Unique
    private long altair$fadeSeenAt = 0L;

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
            at = @At("RETURN"), require = 0)
    private void altair$loadingScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!AltairConfig.isTitleBackground()) {
            return;
        }
        float alpha = 1.0F;
        float progress = this.currentProgress;
        if (this.fadeOutStart > 0L) {
            if (this.altair$fadeSeenAt == 0L) {
                this.altair$fadeSeenAt = System.currentTimeMillis();
            }
            alpha = 1.0F - Math.max(0.0F, Math.min(1.0F, (System.currentTimeMillis() - this.altair$fadeSeenAt) / (float) LoadingOverlay.FADE_OUT_TIME));
            progress = 1.0F;
        }
        if (alpha <= 0.0F) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LoadingScreenArt.draw(graphics, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), progress, alpha);
    }
}
