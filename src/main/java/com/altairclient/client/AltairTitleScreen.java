
package com.altairclient.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Random;

public final class AltairTitleScreen {

    private static final int SKY_TOP = 0xFF071126;
    private static final int SKY_BOTTOM = 0xFF101D38;
    private static final long STAR_SEED = 0xA17A1L;

    private AltairTitleScreen() {}

    public static void register() {
        ScreenEvent.Render.Background.BUS.addListener(
                AltairTitleScreen::renderBackground);
        ScreenEvent.Render.Post.BUS.addListener(
                AltairTitleScreen::renderEditionLabel);
    }

    private static void renderBackground(ScreenEvent.Render.Background event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) return;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int w = screen.width;
        int h = screen.height;

        // Cielo nocturno pixelado
        int bands = 12;

        for (int i = 0; i < bands; i++) {
            int y0 = h * i / bands;
            int y1 = h * (i + 1) / bands;
            float t = i / (float) (bands - 1);

            graphics.fill(0, y0, w, y1,
                    blend(SKY_TOP, SKY_BOTTOM, t));
        }

        // Estrellas
        Random random = new Random(STAR_SEED);
        int count = Math.max(100, (w * h) / 5200);

        long now = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            int x = random.nextInt(Math.max(1, w));
            int y = random.nextInt(Math.max(1, h));

            int size = random.nextInt(10) == 0 ? 2 : 1;

            int color = random.nextInt(5) == 0
                    ? 0xFFD4E4FF
                    : 0xFF9EAFD0;

            graphics.fill(x, y, x + size, y + size, color);
        }

        // Flor de lis central
        drawFleurDeLis(graphics, w / 2, h / 2 - 4);

        // Estrella fugaz
        long cycle = now % 17000L;

        if (cycle < 1250L) {
            float progress = cycle / 1250.0f;

            int sx = (int) (w * 0.78 - progress * w * 0.48);
            int sy = (int) (h * 0.16 + progress * h * 0.30);

            for (int i = 0; i < 9; i++) {
                int px = sx + i * 3;
                int py = sy - i * 2;
                int alpha = Math.max(35, 220 - i * 22);

                graphics.fill(px, py, px + 2, py + 2,
                        (alpha << 24) | 0x00DDEBFF);
            }

            graphics.fill(sx, sy, sx + 3, sy + 3, 0xFFFFFFFF);
        }
    }

    // Texto ALTAIR EDITION
    private static void renderEditionLabel(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) return;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();

        String label = "ALTAIR EDITION";

        int textWidth = Minecraft.getInstance().font.width(label);
        int centerX = screen.width / 2;
        int y = screen.height / 4 + 42;

        graphics.fill(
                centerX - textWidth / 2 - 3,
                y - 2,
                centerX + textWidth / 2 + 3,
                y + 10,
                0xD0071126
        );

        graphics.text(
                Minecraft.getInstance().font,
                Component.literal(label),
                centerX - textWidth / 2,
                y,
                0xFFE6EDF9,
                true
        );
    }

    // Flor de lis construida con píxeles
    private static void drawFleurDeLis(
            GuiGraphicsExtractor g, int cx, int cy) {

        int c = 0x779CB8D8;

        g.fill(cx - 1, cy - 15, cx + 2, cy + 13, c);
        g.fill(cx - 5, cy - 12, cx + 5, cy - 9, c);

        g.fill(cx - 9, cy - 8, cx - 5, cy - 5, c);
        g.fill(cx + 5, cy - 8, cx + 9, cy - 5, c);

        g.fill(cx - 12, cy - 4, cx - 8, cy + 1, c);
        g.fill(cx + 8, cy - 4, cx + 12, cy + 1, c);

        g.fill(cx - 7, cy + 3, cx - 3, cy + 7, c);
        g.fill(cx + 3, cy + 3, cx + 7, cy + 7, c);

        g.fill(cx - 5, cy + 10, cx + 5, cy + 13, c);
        g.fill(cx - 8, cy + 14, cx + 8, cy + 16, c);
    }

    private static int blend(int a, int b, float t) {
        int aa = (a >>> 24) & 255;
        int ar = (a >>> 16) & 255;
        int ag = (a >>> 8) & 255;
        int ab = a & 255;

        int br = (b >>> 16) & 255;
        int bg = (b >>> 8) & 255;
        int bb = b & 255;

        int r = Math.round(ar + (br - ar) * t);
        int g = Math.round(ag + (bg - ag) * t);
        int bl = Math.round(ab + (bb - ab) * t);

        return (aa << 24) | (r << 16) | (g << 8) | bl;
    }
}
