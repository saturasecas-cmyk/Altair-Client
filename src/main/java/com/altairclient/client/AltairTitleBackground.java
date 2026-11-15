package com.altairclient.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Random;

/**
 * Fondo de la pantalla principal: cielo nocturno azul marino con estrellas
 * parpadeantes, estrellas fugaces y nubes de bloques que se desplazan.
 * Se dibuja encima del panorama de vanilla, antes que los botones y el logo.
 */
public final class AltairTitleBackground {

    private static final int STAR_COUNT = 140;
    private static final int CLOUD_COUNT = 9;
    private static final int SHOOTING_STARS = 3;

    // Gradiente del cielo (arriba -> abajo), azul medianoche.
    private static final int SKY_TOP = 0xFF050816;
    private static final int SKY_BOTTOM = 0xFF111A3C;

    private static final long SEED = 0xA17A1AL;
    private static final float[][] STARS = new float[STAR_COUNT][4];   // x, y, fase, tamaño
    private static final float[][] CLOUDS = new float[CLOUD_COUNT][5]; // x, y, ancho, velocidad, capa
    private static final float[][] SHOOTERS = new float[SHOOTING_STARS][3]; // offset, periodo, y

    static {
        Random rng = new Random(SEED);
        for (float[] s : STARS) {
            s[0] = rng.nextFloat();
            s[1] = rng.nextFloat() * 0.85f;
            s[2] = rng.nextFloat() * 6.2831855f;
            s[3] = rng.nextFloat() < 0.08f ? 2f : 1f;
        }
        for (float[] c : CLOUDS) {
            c[0] = rng.nextFloat();
            c[1] = 0.55f + rng.nextFloat() * 0.4f;
            c[2] = 0.18f + rng.nextFloat() * 0.22f;
            c[3] = 0.004f + rng.nextFloat() * 0.006f;
            c[4] = rng.nextInt(2);
        }
        for (int i = 0; i < SHOOTING_STARS; i++) {
            SHOOTERS[i][0] = rng.nextFloat() * 10f;
            SHOOTERS[i][1] = 7f + rng.nextFloat() * 6f;
            SHOOTERS[i][2] = 0.08f + rng.nextFloat() * 0.4f;
        }
    }

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
        float t = (System.currentTimeMillis() % 1_000_000L) / 1000f;

        // Cielo en bandas (gradiente vertical con fill, sin texturas).
        final int bands = 32;
        for (int i = 0; i < bands; i++) {
            int y0 = h * i / bands;
            int y1 = h * (i + 1) / bands;
            g.fill(0, y0, w, y1, lerp(SKY_TOP, SKY_BOTTOM, i / (float) (bands - 1)));
        }

        // Estrellas con parpadeo suave.
        for (float[] s : STARS) {
            int x = (int) (s[0] * w);
            int y = (int) (s[1] * h);
            float tw = 0.55f + 0.45f * (float) Math.sin(t * 1.6f + s[2]);
            int a = (int) (255 * tw);
            int size = (int) s[3];
            g.fill(x, y, x + size, y + size, (a << 24) | 0xDCE6FF);
            if (size == 2) {
                // Destello en cruz para las estrellas grandes.
                int ga = a / 3;
                g.fill(x - 2, y, x + 4, y + 1, (ga << 24) | 0x9FB4FF);
                g.fill(x, y - 2, x + 1, y + 4, (ga << 24) | 0x9FB4FF);
            }
        }

        // Estrellas fugaces con cola diagonal.
        for (float[] sh : SHOOTERS) {
            float phase = ((t + sh[0]) % sh[1]) / 2.2f; // 0..1 durante 2.2 s, luego reposo
            if (phase > 1f) {
                continue;
            }
            float hx = w * (0.95f - 0.8f * phase);
            float hy = h * (sh[2] + 0.35f * phase);
            float fade = (float) Math.sin(phase * Math.PI);
            for (int k = 0; k < 28; k++) {
                int px = (int) (hx + k * 2.2f);
                int py = (int) (hy - k * 1.0f);
                int a = (int) (230 * fade * (1f - k / 28f));
                g.fill(px, py, px + 2, py + 1, (a << 24) | 0x9CC4FF);
            }
            g.fill((int) hx, (int) hy, (int) hx + 2, (int) hy + 2, ((int) (255 * fade) << 24) | 0xFFFFFF);
        }

        // Nubes de bloques, dos capas con distinto tono.
        for (float[] c : CLOUDS) {
            float x = ((c[0] + t * c[3]) % 1.25f) - 0.2f;
            int cw = (int) (c[2] * w);
            int cx = (int) (x * w);
            int cy = (int) (c[1] * h);
            int unit = Math.max(6, h / 40);
            int color = c[4] == 0 ? 0xB0141C42 : 0xB01A2350;
            g.fill(cx, cy, cx + cw, cy + unit, color);
            g.fill(cx + cw / 8, cy - unit, cx + cw * 5 / 8, cy, color);
            g.fill(cx + cw / 4, cy + unit, cx + cw * 7 / 8, cy + unit * 2, color);
        }
    }

    private static int lerp(int c0, int c1, float f) {
        int r = (int) (((c0 >> 16) & 0xFF) + ((((c1 >> 16) & 0xFF) - ((c0 >> 16) & 0xFF)) * f));
        int gr = (int) (((c0 >> 8) & 0xFF) + ((((c1 >> 8) & 0xFF) - ((c0 >> 8) & 0xFF)) * f));
        int b = (int) ((c0 & 0xFF) + (((c1 & 0xFF) - (c0 & 0xFF)) * f));
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }
}
