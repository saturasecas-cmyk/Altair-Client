
package com.altairclient;

import com.altairclient.client.AltairKeyHandler;
import com.altairclient.client.AltairHudEditorScreen;
import com.altairclient.client.AltairScreen;
import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.WorldInfo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class AltairClient implements ClientModInitializer {
    public static final String MOD_ID = "altairclient";

    @Override
    public void onInitializeClient() {
        AltairKeyHandler.register();
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "hud"),
                (graphics, deltaTracker) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.player == null
                            || minecraft.gui.screen() instanceof AltairScreen
                            || minecraft.gui.screen() instanceof AltairHudEditorScreen) {
                        return;
                    }

                    FPSDisplay.getInstance().render(graphics);
                    CPSCounter.getInstance().render(graphics);
                    ArmorHUD.getInstance().render(graphics);
                    Keystrokes.getInstance().render(graphics);
                    PotionStatus.getInstance().render(graphics);
                    WorldInfo.getInstance().render(graphics);
                }
        );
    }
}
