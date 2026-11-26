package com.altairclient.client;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * Pantalla de carga de Altair: cielo estrellado, el emblema azul brillante que da una vuelta cada 4 segundos
 * y una barra fina de morado a celeste. Se usa en la carga del juego y al entrar a un mundo.
 */
public final class LoadingScreenArt {

    private static final ResourceLocation EMBLEM = ResourceLocation.fromNamespaceAndPath("altairclient", "textures/gui/loading/emblem.png");
    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath("altairclient", "textures/gui/loading/emblem_glow.png");
    private static final int EMBLEM_W = 408;
    private static final int EMBLEM_H = 488;
    private static final int GLOW_W = 520;
    private static final int GLOW_H = 600;

    private LoadingScreenArt() {
    }

    private static float ease(float x) {
        x = Math.max(0.0F, Math.min(1.0F, x));
        return x * x * x * (x * (x * 6.0F - 15.0F) + 10.0F);
    }

    /** @param progress 0..1  @param alpha 0..1 (para el fundido de salida) */
    public static void draw(GuiGraphics g, int w, int h, float progress, float alpha) {
        TitleBackground.drawStarfield(g, w, h, alpha);

        float t = (System.currentTimeMillis() % 3_600_000L) / 1000.0F;

        // Una vuelta rápida sobre su eje al inicio de cada ciclo de 4 s, como en el diseño
        float phase = (t % 4.0F) / 4.0F;
        float angle = phase < 0.09F ? ease(phase / 0.09F) * 360.0F : 0.0F;
        float squash = (float) Math.abs(Math.cos(Math.toRadians(angle)));

        int emblemHeight = Math.round(Math.min(h * 0.30F, 96.0F));
        float k = emblemHeight / (float) EMBLEM_H;
        int cx = w / 2;
        int cy = h / 2 - 12;

        // Brillo que respira cada 2,4 s
        float pulse = 0.55F + 0.45F * (float) Math.sin(t * Math.PI * 2.0 / 2.4);
        drawTexture(g, GLOW, cx, cy, GLOW_W, GLOW_H, k, squash, alpha * pulse);
        drawTexture(g, EMBLEM, cx, cy, EMBLEM_W, EMBLEM_H, k, squash, alpha);

        // Barra de carga: morado -> azul -> celeste
        int barW = Math.round(Math.min(w * 0.56F, 160.0F));
        int barX = cx - barW / 2;
        int barY = cy + emblemHeight / 2 + 22;
        g.fill(barX, barY, barX + barW, barY + 2, Anim.scaleAlpha(0x14FFFFFF | 0x12000000, alpha));
        int filled = Math.round(barW * Math.max(0.0F, Math.min(1.0F, progress)));
        for (int x = 0; x < filled; x++) {
            float f = x / (float) Math.max(1, barW - 1);
            int c = f < 0.5F ? Anim.mix(0xFF8A2BE2, 0xFF6F6BFF, f / 0.5F) : Anim.mix(0xFF6F6BFF, 0xFF7FD4FF, (f - 0.5F) / 0.5F);
            g.fill(barX + x, barY, barX + x + 1, barY + 2, Anim.scaleAlpha(c, alpha));
        }
    }

    private static void drawTexture(GuiGraphics g, ResourceLocation texture, int cx, int cy,
                                    int texW, int texH, float k, float squash, float alpha) {
        int dw = Math.round(texW * k * squash);
        int dh = Math.round(texH * k);
        if (dw < 1 || dh < 1 || alpha <= 0.0F) {
            return;
        }
        int color = (Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) << 24) | 0xFFFFFF;
        g.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, cx - dw / 2, cy - dh / 2, 0.0F, 0.0F, dw, dh,
                texW, texH, texW, texH, color);
    }
}
