package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.CustomCrosshair;
import com.altairclient.modules.mods.ClockHud;
import com.altairclient.modules.mods.DirectionHud;
import com.altairclient.modules.mods.ServerAddress;
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
import com.altairclient.modules.textures.ExtraTextures;
import com.altairclient.modules.textures.HudTextures;
import com.altairclient.modules.textures.MovementTextures;
import com.altairclient.modules.textures.PvpTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

/** Dibuja los módulos del HUD encima del juego. */
public final class AltairHud {

    private AltairHud() {
    }

    public static void register() {
        HitboxDisplay.register();
    }

    public static void renderOverlay(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (!inWorld() || mc.options.hideGui) {
            return;
        }
        if (CustomCrosshair.isEnabled()) {
            CustomCrosshair.render(graphics);
        }
        PvpTextures.renderFire(graphics);
        MovementTextures.renderSpeedLines(graphics);
        MovementTextures.renderMotionBlur(graphics);
        HudTextures.render(graphics);
        ExtraTextures.render(graphics);
        com.altairclient.modules.mods.DynamicIsland.render(graphics);
        renderAll(graphics);
        com.altairclient.modules.mods.RenderHacks.render(graphics, partialTick);
    }

    /** Altair's HUD and texture overlays only draw while playing in a world, never on menu screens. */
    private static boolean inWorld() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.player != null;
    }

    public static void renderAll(GuiGraphics graphics) {
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
        ClockHud.getInstance().render(graphics);
    }
}
