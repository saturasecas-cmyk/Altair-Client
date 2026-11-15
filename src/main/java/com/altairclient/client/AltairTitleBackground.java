package com.altairclient.client;

import com.altairclient.AltairClient;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.ScreenEvent;

/**
 * Fondo de la pantalla principal: cielo nocturno con estrellas fugaces y nubes
 * (textura propia del mod). Se dibuja a pantalla completa, recortada para cubrir
 * toda la ventana sin deformarse, encima del panorama de vanilla y antes que
 * los botones y el logo.
 */
public final class AltairTitleBackground {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(AltairClient.MOD_ID, "textures/gui/title_background.png");

    private static final int TEX_W = 1800;
    private static final int TEX_H = 874;

    private AltairTitleBackground() {
    }

    public static void register() {
        ScreenEvent.BackgroundRendered.BUS.addListener(AltairTitleBackground::onBackground);
    }

    private static void onBackground(ScreenEvent.BackgroundRendered event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }
        draw(event.getGuiGraphics(), screen.width, screen.height);
    }

    private static void draw(GuiGraphicsExtractor g, int w, int h) {
        // "Cover": se recorta la textura por el centro para llenar la pantalla sin estirarla.
        int regionW = TEX_W;
        int regionH = TEX_H;
        if ((long) w * TEX_H > (long) h * TEX_W) {
            regionH = Math.max(1, (int) ((long) TEX_W * h / w));
        } else {
            regionW = Math.max(1, (int) ((long) TEX_H * w / h));
        }
        int u = (TEX_W - regionW) / 2;
        int v = (TEX_H - regionH) / 2;

        g.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, u, v, w, h, regionW, regionH, TEX_W, TEX_H);
    }
}
