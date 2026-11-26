package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.LoadingScreenArt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Entrar a un mundo: la pantalla de carga de Altair tapa la de vanilla. */
@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {

    @Shadow
    @Final
    private StoringChunkProgressListener progressListener;

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void altair$worldLoading(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!AltairConfig.isTitleBackground()) {
            return;
        }
        var window = net.minecraft.client.Minecraft.getInstance().getWindow();
        float progress = Math.max(0, Math.min(100, this.progressListener.getProgress())) / 100.0F;
        LoadingScreenArt.draw(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(), progress, 1.0F);
    }
}
