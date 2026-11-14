package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.HitboxDisplay;
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

    private static final Identifier HITBOX_LAYER_ID =
            Identifier.fromNamespaceAndPath(AltairClient.MOD_ID, "hitboxes");

    private AltairHud() {
    }

    public static void register() {
        AddGuiOverlayLayersEvent.BUS.addListener(AltairHud::onAddLayers);
    }

    private static void onAddLayers(AddGuiOverlayLayersEvent event) {
        // Las hitboxes se dibujan en el mundo, no en el HUD, así que esta capa no se oculta con F1.
        event.getLayeredDraw().addWithCondition(
                HITBOX_LAYER_ID,
                (graphics, deltaTracker) -> HitboxDisplay.getInstance().emit(deltaTracker),
                () -> true
        );

        // La capa del HUD se oculta con F1, igual que el HUD de vanilla.
        event.getLayeredDraw().addWithCondition(
                LAYER_ID,
                (graphics, deltaTracker) -> renderAll(graphics),
                () -> Minecraft.getInstance().level != null && !Minecraft.getInstance().gui.hud.isHidden()
        );
    }

    public static void renderAll(GuiGraphicsExtractor graphics) {
        CPSCounter.getInstance().poll();

        FPSDisplay.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
    }
}
