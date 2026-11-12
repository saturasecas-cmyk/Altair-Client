package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.WorldInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Dibuja los módulos del HUD encima del juego. */
public final class AltairHud {

    private AltairHud() {
    }

    public static void register() {
        // TODO: enganchar renderAll a la capa de HUD de Forge 26.2.
    }

    public static void renderAll(GuiGraphicsExtractor graphics) {
        FPSDisplay.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
    }
}
