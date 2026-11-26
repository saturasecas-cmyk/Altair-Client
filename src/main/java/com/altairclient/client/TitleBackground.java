package com.altairclient.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.Random;

/**
 * Fondo del menú principal: cielo nocturno azul marino y morado, muchas estrellas que parpadean,
 * estrellas fugaces y una silueta de terreno con árboles. Se dibuja entero con código (sin imágenes).
 */
public final class TitleBackground {

    private static final int STARS = 300;
    private static final int BIG_STARS = 12;
    private static final int[] STAR_COLORS = {0xFFFFFFFF, 0xFFCFE0FF, 0xFFD8C8FF, 0xFFBFD8FF};

    private static final float[] SX = new float[STARS];
    private static final float[] SY = new float[STARS];
    private static final float[] SPHASE = new float[STARS];
    private static final float[] SSPEED = new float[STARS];
    private static final int[] SCOLOR = new int[STARS];
    private static final float[] BX = new float[BIG_STARS];
    private static final float[] BY = new float[BIG_STARS];
    private static final float[] BPHASE = new float[BIG_STARS];

    private static final ResourceLocation LOGO = ResourceLocation.fromNamespaceAndPath("altairclient", "textures/gui/title/altair_logo.png");
    private static final int LOGO_TEX_W = 768;
    private static final int LOGO_TEX_H = 172;
    private static final int LOGO_DRAW_W = 232;

    // Campo de estrellas de las pantallas Opciones / Un jugador / Multijugador
    private static final int FIELD_STARS = 700;
    private static final float[] FX = new float[FIELD_STARS];
    private static final float[] FY = new float[FIELD_STARS];
    private static final float[] FPHASE = new float[FIELD_STARS];
    private static final float[] FSPEED = new float[FIELD_STARS];
    private static final float[] FBRIGHT = new float[FIELD_STARS];
    private static final int[] FLAYER = new int[FIELD_STARS];
    private static final int[] FCOLOR = new int[FIELD_STARS];
    /** Velocidad de deriva (px por segundo) de cada capa: las cercanas se mueven más rápido. */
    private static final float[] LAYER_SPEED = {2.5F, 5.0F, 9.0F};

    private static boolean registered = false;

    static {
        Random r = new Random(26_2_0L);
        for (int i = 0; i < STARS; i++) {
            SX[i] = r.nextFloat();
            SY[i] = r.nextFloat() * 0.85F;
            SPHASE[i] = r.nextFloat() * 6.2831F;
            SSPEED[i] = 0.6F + r.nextFloat() * 2.0F;
            SCOLOR[i] = STAR_COLORS[r.nextInt(STAR_COLORS.length)];
        }
        for (int i = 0; i < BIG_STARS; i++) {
            BX[i] = r.nextFloat();
            BY[i] = r.nextFloat() * 0.6F;
            BPHASE[i] = r.nextFloat() * 6.2831F;
        }
    }

    static {
        Random r = new Random(7_1_8L);
        for (int i = 0; i < FIELD_STARS; i++) {
            float x = r.nextFloat();
            float y = r.nextFloat();
            if (r.nextFloat() < 0.4F) {
                // Una franja diagonal más densa, como la Vía Láctea de la foto
                x = r.nextFloat();
                y = Math.max(0.0F, Math.min(1.0F, 0.1F + 0.6F * x + (float) r.nextGaussian() * 0.07F));
            }
            FX[i] = x;
            FY[i] = y;
            FPHASE[i] = r.nextFloat() * 6.2831F;
            FSPEED[i] = 0.5F + r.nextFloat() * 1.8F;
            float roll = r.nextFloat();
            FLAYER[i] = roll < 0.55F ? 0 : (roll < 0.87F ? 1 : 2);
            FBRIGHT[i] = 0.3F + r.nextFloat() * 0.7F;
            FCOLOR[i] = STAR_COLORS[r.nextInt(STAR_COLORS.length)];
        }
    }

    private TitleBackground() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(TitleBackground::onBackground);
        }
    }

    /** Lo llama TitleScreenMixin: dibuja el fondo de Altair en el título. Devuelve false si está desactivado. */
    public static boolean drawForTitle(GuiGraphics g, int w, int h) {
        if (!AltairConfig.isTitleBackground()) {
            return false;
        }
        titleDrawnFrame = System.nanoTime();
        draw(g, w, h);
        return true;
    }

    private static long titleDrawnFrame = 0L;

    private static void onBackground(ScreenEvent.BackgroundRendered event) {
        if (!AltairConfig.isTitleBackground()) {
            return;
        }
        Screen screen = event.getScreen();
        if (screen instanceof TitleScreen) {
            if (System.nanoTime() - titleDrawnFrame < 100_000_000L) {
                return; // ya dibujado por TitleScreenMixin
            }
            draw(event.getGuiGraphics(), screen.width, screen.height);
        } else if (Minecraft.getInstance().level == null
                && (screen instanceof OptionsScreen || screen instanceof SelectWorldScreen
                || screen instanceof JoinMultiplayerScreen)) {
            drawStarfield(event.getGuiGraphics(), screen.width, screen.height, 1.0F);
        }
    }

    /** Cielo de foto nocturna: azul marino, neblina azulada abajo a la derecha y estrellas que derivan despacio. */
    public static void drawStarfield(GuiGraphics g, int w, int h, float alpha) {
        float t = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;

        final int cell = 12;
        for (int y = 0; y < h; y += cell) {
            float v = y / (float) h;
            for (int x = 0; x < w; x += cell) {
                float u = x / (float) w;
                float d = u * 0.55F + v * 0.75F;
                int base = Anim.mix(0xFF020517, 0xFF0A1850, Math.max(0.0F, Math.min(1.0F, d / 0.8F)));
                float haze = (d - 0.72F) / 0.45F + 0.05F * (float) Math.sin(t * 0.25F + u * 2.5F);
                haze = Math.max(0.0F, Math.min(1.0F, haze));
                haze *= haze;
                g.fill(x, y, x + cell, y + cell, Anim.scaleAlpha(haze > 0.01F ? Anim.mix(base, 0xFF4B4C86, haze * 0.9F) : base, alpha));
            }
        }

        for (int i = 0; i < FIELD_STARS; i += 2) {
            float speed = LAYER_SPEED[FLAYER[i]];
            float px = FX[i] * w - t * speed;
            float py = FY[i] * h + t * speed * 0.25F;
            int x = Math.floorMod((int) px, w);
            int y = Math.floorMod((int) py, h);
            float tw = 0.65F + 0.35F * (float) Math.sin(t * FSPEED[i] + FPHASE[i]);
            int size = FLAYER[i] == 2 && FBRIGHT[i] > 0.7F ? 2 : 1;
            g.fill(x, y, x + size, y + size, Anim.scaleAlpha(FCOLOR[i], FBRIGHT[i] * tw * alpha));
        }

        drawShootingStars(g, w, h, t, alpha);
    }

    /** Dibuja el logo Altair Edition en lugar del de Minecraft. Devuelve false si está desactivado (se usa el de vanilla). */
    public static boolean drawLogo(GuiGraphics g, int screenWidth, float alpha, int y) {
        if (!AltairConfig.isTitleBackground()) {
            return false;
        }
        int drawH = Math.round(LOGO_DRAW_W * LOGO_TEX_H / (float) LOGO_TEX_W);
        int color = (Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) << 24) | 0xFFFFFF;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        g.setColor(1.0F, 1.0F, 1.0F, alpha);
        g.blit(LOGO, screenWidth / 2 - LOGO_DRAW_W / 2, y, LOGO_DRAW_W, drawH, 0.0F, 0.0F,
                LOGO_TEX_W, LOGO_TEX_H, LOGO_TEX_W, LOGO_TEX_H);
        g.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    private static int hash(long v) {
        long x = v * 0x9E3779B97F4A7C15L;
        x ^= x >>> 29;
        x *= 0xBF58476D1CE4E5B9L;
        x ^= x >>> 32;
        return (int) (x & 0x7FFFFFFF);
    }

    private static void draw(GuiGraphics g, int w, int h) {
        long ms = System.currentTimeMillis();
        float t = (ms % 3_600_000L) / 1000.0F;

        // Cielo: azul marino arriba, azul intenso en medio y morado cerca del horizonte
        final int top = 0xFF040821;
        final int mid = 0xFF0B1244;
        final int low = 0xFF2B1A5E;
        for (int y = 0; y < h; y += 8) {
            float f = y / (float) h;
            int c = f < 0.6F ? Anim.mix(top, mid, f / 0.6F) : Anim.mix(mid, low, (f - 0.6F) / 0.4F);
            g.fill(0, y, w, Math.min(h, y + 8), c);
        }

        // Estrellas pequeñas con parpadeo
        for (int i = 0; i < STARS; i += 2) {
            float tw = 0.5F + 0.5F * (float) Math.sin(t * SSPEED[i] + SPHASE[i]);
            int x = (int) (SX[i] * w);
            int y = (int) (SY[i] * h);
            int size = (i % 9 == 0) ? 2 : 1;
            g.fill(x, y, x + size, y + size, Anim.scaleAlpha(SCOLOR[i], 0.25F + 0.75F * tw));
        }

        // Estrellas grandes en cruz
        for (int i = 0; i < BIG_STARS; i++) {
            float tw = 0.55F + 0.45F * (float) Math.sin(t * 1.3F + BPHASE[i]);
            int x = (int) (BX[i] * w);
            int y = (int) (BY[i] * h);
            int c = Anim.scaleAlpha(0xFFFFFFFF, tw);
            int arm = Anim.scaleAlpha(0xFFBFD8FF, tw * 0.7F);
            g.fill(x - 1, y - 1, x + 1, y + 1, c);
            g.fill(x - 4, y, x + 4, y + 1, arm);
            g.fill(x, y - 4, x + 1, y + 4, arm);
        }

        drawShootingStars(g, w, h, t, 1.0F);
        drawTerrain(g, w, h);
    }

    private static void drawShootingStars(GuiGraphics g, int w, int h, float t, float alphaScale) {
        final float dur = 1.3F;
        for (int slot = 0; slot < 3; slot++) {
            float period = 7.0F + slot * 2.3F;
            float shifted = t + slot * 3.1F;
            float local = shifted % period;
            if (local >= dur) {
                continue;
            }
            long cycle = (long) (shifted / period);
            int hs = hash(cycle * 31L + slot);
            float startX = w * (0.45F + 0.55F * ((hs & 0xFFFF) / 65535.0F));
            float startY = h * (0.03F + 0.30F * (((hs >> 8) & 0xFFFF) / 65535.0F));
            float p = local / dur;
            float fade = (float) Math.sin(Math.PI * p);
            float travel = w * 0.5F * p;
            float dx = -1.0F;
            float dy = 0.42F;
            for (int i = 0; i < 22; i++) {
                float back = i * 2.5F;
                int x = (int) (startX + dx * (travel - back));
                int y = (int) (startY + dy * (travel - back));
                float a = fade * (1.0F - i / 22.0F) * alphaScale;
                int size = i < 3 ? 2 : 1;
                g.fill(x, y, x + size, y + size, Anim.scaleAlpha(i < 3 ? 0xFFFFFFFF : 0xFFBFD8FF, a));
            }
        }
    }

    private static void drawTerrain(GuiGraphics g, int w, int h) {
        final int cell = 8;
        // Colinas del fondo, con árboles
        for (int x = 0; x < w; x += cell) {
            float hy = h * (0.80F + 0.04F * (float) Math.sin(x * 0.011F) + 0.025F * (float) Math.sin(x * 0.027F + 1.3F));
            int top = Math.round(hy / cell) * cell;
            g.fill(x, top, x + cell, h, 0xFF080C26);
            if (hash(x / cell) % 13 == 0) {
                g.fill(x + 1, top - 7, x + 3, top, 0xFF05081C);
                g.fill(x - 3, top - 15, x + 7, top - 6, 0xFF05081C);
                g.fill(x - 1, top - 19, x + 5, top - 14, 0xFF05081C);
            }
        }
        // Primer plano, más oscuro
        for (int x = 0; x < w; x += cell) {
            float hy = h * (0.91F + 0.03F * (float) Math.sin(x * 0.017F + 0.5F) + 0.015F * (float) Math.sin(x * 0.05F));
            int top = Math.round(hy / cell) * cell;
            g.fill(x, top, x + cell, h, 0xFF03050E);
        }
    }
}
