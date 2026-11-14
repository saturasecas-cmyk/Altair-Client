
package com.altairclient.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Random;

public final class AltairTitleScreen {

    private static final long PERIOD_MS = 11500L;
    private static final long FLIGHT_MS = 1250L;

    private AltairTitleScreen() {}

    public static void register() {
        ScreenEvent.Render.Post.BUS.addListener(
                AltairTitleScreen::renderShootingStar
        );
    }

    private static void renderShootingStar(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }

        long now = System.currentTimeMillis();
        long cycle = now % PERIOD_MS;

        if (cycle >= FLIGHT_MS) {
            return;
        }

        long cycleNumber = now / PERIOD_MS;
        Random random = new Random(0xA17A1L + cycleNumber * 7919L);

        int width = screen.width;
        int height = screen.height;

        boolean leftSide = random.nextBoolean();

        int startX = leftSide
                ? random.nextInt(Math.max(1, width / 7))
                : width - random.nextInt(Math.max(1, width / 7));

        int startY = height / 8
                + random.nextInt(Math.max(1, height / 4));

        int travelX = Math.max(35, width / 9);
        int travelY = Math.max(18, height / 14);

        float progress = cycle / (float) FLIGHT_MS;
        int direction = leftSide ? 1 : -1;

        int x = startX + (int) (direction * travelX * progress);
        int y = startY + (int) (travelY * progress);

        GuiGraphicsExtractor graphics = event.getGuiGraphics();

        // Estela pixelada
        for (int i = 8; i >= 1; i--) {
            int px = x - direction * i * 3;
            int py = y - i * 2;
            int alpha = Math.max(25, 190 - i * 19);

            int color = (alpha << 24) | 0x00DCEAFF;

            graphics.fill(px, py, px + 2, py + 2, color);
        }

        // Cabeza luminosa
        graphics.fill(x, y, x + 3, y + 3, 0xFFFFFFFF);
    }
}
