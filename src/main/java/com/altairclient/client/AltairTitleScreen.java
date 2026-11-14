
package com.altairclient.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Random;

public final class AltairTitleScreen {

    private static final long STAR_SEED = 0xA17A1L;

    private AltairTitleScreen() {}

    public static void register() {
        ScreenEvent.Render.Post.BUS.addListener(
                AltairTitleScreen::renderDecoration
        );
    }

    private static void renderDecoration(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        int width = screen.width;
        int height = screen.height;
        int centerX = width / 2;

        // Oscurecimiento del cielo
        graphics.fill(0, 0, width, height / 3, 0x3A071126);
        graphics.fill(0, height * 4 / 5, width, height, 0x30071126);
        graphics.fill(0, height / 3, width / 5, height * 4 / 5, 0x26071126);
        graphics.fill(width * 4 / 5, height / 3, width, height * 4 / 5, 0x26071126);

        // Estrellas
        Random random = new Random(STAR_SEED);
        int starCount = Math.max(120, (width * height) / 3000);

        for (int i = 0; i < starCount; i++) {
            int x = random.nextInt(Math.max(1, width));
            int y = random.nextInt(Math.max(1, height));

            // Evita dibujar estrellas encima de los botones.
            if (x > centerX - 155 && x < centerX + 155
                    && y > height * 2 / 5 && y < height * 4 / 5) {
                continue;
            }

            int size = random.nextInt(12) == 0 ? 2 : 1;
            int color = random.nextInt(5) == 0
                    ? 0xFFE0E9FF
                    : 0xFF9EAFD0;

            graphics.fill(x, y, x + size, y + size, color);
        }

        // Flor de lis central
        drawFleurDeLis(graphics, centerX, height / 2 - 88);

        // Estrella fugaz animada
        long cycle = System.currentTimeMillis() % 19000L;

        if (cycle < 1150L) {
            float progress = cycle / 1150.0f;

            int sx = (int) (width * 0.82 - progress * width * 0.48);
            int sy = (int) (height * 0.12 + progress * height * 0.25);

            for (int i = 8; i >= 1; i--) {
                int px = sx + i * 3;
                int py = sy - i * 2;
                int alpha = Math.max(35, 210 - i * 20);

                graphics.fill(px, py, px + 2, py + 2,
                        (alpha << 24) | 0x00DCEAFF);
            }

            graphics.fill(sx, sy, sx + 3, sy + 3, 0xFFFFFFFF);
        }

        // Título Altair Edition
        drawEditionLabel(graphics, screen, height);
    }

    private static void drawEditionLabel(
            GuiGraphicsExtractor graphics,
            TitleScreen screen,
            int height) {

        String label = "ALTAIR EDITION";

        int textWidth = Minecraft.getInstance().font.width(label);
        int centerX = screen.width / 2;
        int y = height / 4 + 42;

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

    private static void drawFleurDeLis(
            GuiGraphicsExtractor graphics,
            int cx,
            int cy) {

        int color = 0x779CB8D8;

        // Forma pixelada de la flor de lis
        graphics.fill(cx - 1, cy - 15, cx + 2, cy + 13, color);
        graphics.fill(cx - 5, cy - 12, cx + 5, cy - 9, color);

        graphics.fill(cx - 9, cy - 8, cx - 5, cy - 5, color);
        graphics.fill(cx + 5, cy - 8, cx + 9, cy - 5, color);

        graphics.fill(cx - 12, cy - 4, cx - 8, cy + 1, color);
        graphics.fill(cx + 8, cy - 4, cx + 12, cy + 1, color);

        graphics.fill(cx - 7, cy + 3, cx - 3, cy + 7, color);
        graphics.fill(cx + 3, cy + 3, cx + 7, cy + 7, color);

        graphics.fill(cx - 5, cy + 10, cx + 5, cy + 13, color);
        graphics.fill(cx - 8, cy + 14, cx + 8, cy + 16, color);

        // Estrellas de la constelación
        graphics.fill(cx - 19, cy - 11, cx - 17, cy - 9, 0xAAE0E9FF);
        graphics.fill(cx + 17, cy - 11, cx + 19, cy - 9, 0xAAE0E9FF);
        graphics.fill(cx - 17, cy + 17, cx - 15, cy + 19, 0xAAE0E9FF);
        graphics.fill(cx + 15, cy + 17, cx + 17, cy + 19, 0xAAE0E9FF);
    }
}
