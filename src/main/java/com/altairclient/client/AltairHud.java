package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.WorldInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;

/** Dibuja los módulos del HUD encima del juego. */
public final class AltairHud {

    private static final Identifier LAYER_ID =
            Identifier.fromNamespaceAndPath(AltairClient.MOD_ID, "hud");

    private AltairHud() {
    }

    public static void register() {
        AddGuiOverlayLayersEvent.BUS.addListener(AltairHud::onAddLayers);
    }

    private static void onAddLayers(AddGuiOverlayLayersEvent event) {
        // La capa se oculta con F1, igual que el HUD de vanilla.
        event.getLayeredDraw().addWithCondition(
                LAYER_ID,
                (graphics, deltaTracker) -> renderAll(graphics),
                () -> !Minecraft.getInstance().gui.hud.isHidden()
        );
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
