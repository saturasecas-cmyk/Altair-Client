package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.CustomCrosshair;
import com.altairclient.modules.mods.DirectionHud;
import com.altairclient.modules.mods.ServerAddress;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import com.altairclient.modules.mods.ArrowCounter;
import com.altairclient.modules.mods.DamageIndicator;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.ItemNameDisplay;
import com.altairclient.modules.mods.MemoryDisplay;
import com.altairclient.modules.mods.TotemCounter;
import com.altairclient.modules.mods.HealthDisplay;
import com.altairclient.modules.mods.HitboxDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PingDisplay;
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

    private static final Identifier CROSSHAIR_LAYER_ID =
            Identifier.fromNamespaceAndPath(AltairClient.MOD_ID, "crosshair");

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

        // La mira de Altair sustituye a la de vanilla mientras está activada.
        event.getLayeredDraw().addConditionTo(ForgeLayeredDraw.CROSSHAIR, () -> !CustomCrosshair.isEnabled());
        event.getLayeredDraw().addWithCondition(
                CROSSHAIR_LAYER_ID,
                (graphics, deltaTracker) -> CustomCrosshair.render(graphics),
                () -> CustomCrosshair.isEnabled() && !Minecraft.getInstance().gui.hud.isHidden()
        );

        // La capa del HUD se oculta con F1, igual que el HUD de vanilla.
        event.getLayeredDraw().addWithCondition(
                LAYER_ID,
                (graphics, deltaTracker) -> renderAll(graphics),
                () -> !Minecraft.getInstance().gui.hud.isHidden()
        );
    }

    public static void renderAll(GuiGraphicsExtractor graphics) {
        CPSCounter.getInstance().poll();

        FPSDisplay.getInstance().render(graphics);
        PingDisplay.getInstance().render(graphics);
        HealthDisplay.getInstance().render(graphics);
        TotemCounter.getInstance().render(graphics);
        ArrowCounter.getInstance().render(graphics);
        MemoryDisplay.getInstance().render(graphics);
        ItemNameDisplay.getInstance().render(graphics);
        DamageIndicator.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
        ServerAddress.getInstance().render(graphics);
        DirectionHud.getInstance().render(graphics);
    }
}
