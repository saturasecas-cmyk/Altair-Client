package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class FPSDisplay {

    private static final FPSDisplay INSTANCE = new FPSDisplay();

    public static FPSDisplay getInstance() {
        return INSTANCE;
    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;

    // Viene activado por defecto (true) para que se vea nada más entrar al juego
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

        // No dibuja si no hay mundo cargado o si el jugador ocultó el HUD con F1
        if (minecraft == null || minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        int fps = minecraft.getFps();
        String text = "FPS: " + fps;

        int x = 10;
        int y = 10;

        int textWidth = minecraft.font.width(text);

        // 1. Fondo oscuro translúcido
        graphics.fill(
                x - 5,
                y - 4,
                x + textWidth + 8,
                y + 14,
                BACKGROUND
        );

        // 2. Línea decorativa violeta neón (estilo Altair)
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                PURPLE
        );

        // 3. Texto de FPS en blanco
        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                WHITE,
                false
        );
    }

    /**
     * Suscripción al bus de eventos de Forge para pintar el HUD en cada fotograma.
     */
    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            INSTANCE.render(event.getGuiGraphics());
        }
    }
}
