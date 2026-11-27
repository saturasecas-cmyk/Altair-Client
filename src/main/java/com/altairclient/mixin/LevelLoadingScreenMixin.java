package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.LoadingScreenArt;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Entrar a un mundo: la pantalla de carga de Altair tapa la de vanilla. */
@Mixin(LevelLoadingScreen.class)
public abstract class LevelLoadingScreenMixin {

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void altair$worldLoading(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!AltairConfig.isTitleBackground()) {
            return;
        }
        var window = net.minecraft.client.Minecraft.getInstance().getWindow();
        float progress = (float) ((System.currentTimeMillis() % 3000L) / 3000.0);
        LoadingScreenArt.draw(graphics, window.getGuiScaledWidth(), window.getGuiScaledHeight(), progress, 1.0F);
    }
}
