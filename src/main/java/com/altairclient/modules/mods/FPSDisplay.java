package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class FPSDisplay {

    private static final FPSDisplay INSTANCE = new FPSDisplay();

    public static FPSDisplay getInstance() {
        return INSTANCE;
    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xCC0E1326;
    private static final int BORDER = 0xFF242E54;
    private static final int PURPLE = 0xFFA580FF;

    private boolean enabled = true;

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        int fps = minecraft.getFps();
        String text = "FPS: " + fps;

        int x = 12;
        int y = 12;
        int textWidth = minecraft.font.width(text);

        // Borde exterior refinado
        graphics.fill(x - 6, y - 5, x + textWidth + 9, y + 15, BORDER);
        // Fondo cósmico translúcido
        graphics.fill(x - 5, y - 4, x + textWidth + 8, y + 14, BACKGROUND);
        // Barra violeta neón a la izquierda
        graphics.fill(x - 5, y - 4, x - 2, y + 14, PURPLE);
        // Texto de FPS
        graphics.text(minecraft.font, text, x + 2, y, WHITE, false);
    }

    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post event) {
            // Se dibuja en el HUD junto a la barra de objetos
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
                INSTANCE.render(event.getGuiGraphics());
            }
        }
    }
}
