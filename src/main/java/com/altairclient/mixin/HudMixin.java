package com.altairclient.mixin;

import com.altairclient.modules.mods.CustomCrosshair;
import com.altairclient.modules.textures.ExtraTextures;
import com.altairclient.modules.textures.HudTextures;
import com.altairclient.modules.textures.PvpTextures;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Oculta partes del HUD de vanilla cuando Altair dibuja la suya (mira, calabaza, marcador, jefe, lista). */
@Mixin(Hud.class)
public abstract class HudMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$crosshair(GuiGraphicsExtractor g, DeltaTracker dt, CallbackInfo ci) {
        if (CustomCrosshair.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$overlay(GuiGraphicsExtractor g, Identifier texture, float alpha, CallbackInfo ci) {
        if (texture != null && texture.getPath().contains("pumpkin") && PvpTextures.hideCameraOverlay()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$scoreboard(GuiGraphicsExtractor g, DeltaTracker dt, CallbackInfo ci) {
        if (HudTextures.hideScoreboard()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractBossOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$boss(GuiGraphicsExtractor g, DeltaTracker dt, CallbackInfo ci) {
        if (HudTextures.hideBossBar()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTabList", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$tab(GuiGraphicsExtractor g, DeltaTracker dt, CallbackInfo ci) {
        if (ExtraTextures.hideVanillaTablist()) {
            ci.cancel();
        }
    }
}
