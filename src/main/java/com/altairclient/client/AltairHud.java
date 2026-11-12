package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.WorldInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraftforge.client.event.RenderGuiEvent;

/** Dibuja los módulos del HUD encima del juego. */
public final class AltairHud {

    private AltairHud() {
    }

    public static void register() {
        RenderGuiEvent.Post.BUS.addListener(AltairHud::onRenderGui);
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        GuiGraphicsExtractor graphics = event.getGuiGraphics();

        FPSDisplay.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
    }
}
