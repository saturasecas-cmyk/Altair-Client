package com.altairclient.mixin;

import com.altairclient.client.AltairHud;
import com.altairclient.modules.mods.CustomCrosshair;
import com.altairclient.modules.textures.ExtraTextures;
import com.altairclient.modules.textures.HudTextures;
import com.altairclient.modules.textures.PvpTextures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 1.21.1 has no Forge overlay events, so Altair hooks the vanilla HUD directly. */
@Mixin(Gui.class)
public abstract class GuiHookMixin {

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void altair$render(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        AltairHud.renderOverlay(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$crosshair(CallbackInfo ci) {
        if (CustomCrosshair.isEnabled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$hotbar(CallbackInfo ci) {
        if (HudTextures.hideVanillaHotbar()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$xp(CallbackInfo ci) {
        if (HudTextures.hideVanillaXpBar()) {
            ci.cancel();
        }
    }

    @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$scoreboard(CallbackInfo ci) {
        if (HudTextures.hideScoreboard()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCameraOverlays", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$camera(CallbackInfo ci) {
        if (PvpTextures.hideCameraOverlay()) {
            ci.cancel();
        }
    }
}
