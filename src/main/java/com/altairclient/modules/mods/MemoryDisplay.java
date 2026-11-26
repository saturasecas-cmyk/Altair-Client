package com.altairclient.modules.mods;

import com.altairclient.client.Anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Memory Display: RAM usada por el juego, como porcentaje, como gráfico de los últimos
 * segundos o las dos cosas. El modo se elige en la tarjeta del menú de módulos.
 */
public class MemoryDisplay extends HudElement {

    public static final String[] MODE_NAMES = {"Percent and graph", "Graph only", "Percent only"};

    private static final MemoryDisplay INSTANCE = new MemoryDisplay();

    private static final int SAMPLES = 40;
    private static final int COLUMN_W = 2;
    private static final int GRAPH_H = 14;
    private static final long SAMPLE_MS = 500L;

    private final int[] history = new int[SAMPLES];
    private final float[] shown = new float[SAMPLES];
    private long lastSample = 0L;
    private int mode = 0;

    private MemoryDisplay() {
        super(10, 110);
    }

    public static MemoryDisplay getInstance() {
        return INSTANCE;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int value) {
        mode = Math.floorMod(value, MODE_NAMES.length);
    }

    private boolean showText() {
        return mode == 0 || mode == 2;
    }

    private boolean showGraph() {
        return mode == 0 || mode == 1;
    }

    @Override
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        int w = 0;
        if (showText()) {
            w += (mc != null && mc.font != null ? mc.font.width("RAM: 100%") : 50);
        }
        if (showGraph()) {
            w += (showText() ? 6 : 0) + SAMPLES * COLUMN_W;
        }
        return w + 13;
    }

    @Override
    public int getHeight() {
        return showGraph() ? GRAPH_H + 4 : 18;
    }

    private static int usedPercent() {
        Runtime rt = Runtime.getRuntime();
        long used = rt.totalMemory() - rt.freeMemory();
        return (int) Math.min(100, Math.max(0, used * 100L / rt.maxMemory()));
    }

    private void sample() {
        long now = System.currentTimeMillis();
        if (now - lastSample >= SAMPLE_MS) {
            lastSample = now;
            System.arraycopy(history, 1, history, 0, SAMPLES - 1);
            history[SAMPLES - 1] = usedPercent();
        }
    }

    private final Anim.Counter percentAnim = new Anim.Counter(5.0F);

    private static int colorFor(int percent) {
        return percent < 60 ? GREEN : (percent < 85 ? YELLOW : RED);
    }

    @Override
    protected void draw(GuiGraphics graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        int[] data = history;
        int percent;
        if (preview) {
            data = new int[SAMPLES];
            for (int i = 0; i < SAMPLES; i++) {
                data[i] = 45 + (int) (20 * Math.sin(i / 4.0));
            }
            percent = 52;
        } else {
            sample();
            percent = usedPercent();
        }
        float smoothPercent = percentAnim.update(percent, 8.0F);
        percent = Math.round(smoothPercent);

        int x = getX();
        int y = getY();
        int contentW = getWidth() - 13;
        box(graphics, contentW, getHeight(), CYAN);

        int gx = x;
        if (showText()) {
            String label = "RAM: ";
            String value = percent + "%";
            int textY = showGraph() ? y + (GRAPH_H - 8) / 2 : y;
            graphics.drawString(mc.font, label, x, textY, WHITE, false);
            graphics.drawString(mc.font, value, x + mc.font.width(label), textY, percentAnim.color(colorFor(percent)), false);
            gx = x + mc.font.width("RAM: 100%") + 6;
        }

        if (showGraph()) {
            int baseY = y + GRAPH_H;
            long now = System.currentTimeMillis();
            // Desplazamiento suave entre muestras: el gráfico se desliza en vez de dar saltos.
            float slide = preview ? 0.0F : Math.min(1.0F, (now - lastSample) / (float) SAMPLE_MS);
            graphics.fill(gx - 1, y - 1, gx + SAMPLES * COLUMN_W + 1, baseY + 1, 0x66000000);
            graphics.fill(gx - 1, baseY, gx + SAMPLES * COLUMN_W + 1, baseY + 1, 0x3362C8FF);
            for (int i = 0; i < SAMPLES; i++) {
                // La altura mostrada persigue a la real (animación de subida y bajada)
                shown[i] += (data[i] - shown[i]) * 0.2F;
                int cx = gx + i * COLUMN_W - Math.round(slide * COLUMN_W);
                if (cx < gx - COLUMN_W) {
                    continue;
                }
                int left = Math.max(cx, gx);
                int h = Math.max(1, Math.round(shown[i] * GRAPH_H / 100.0F));
                int rgb = colorFor(Math.round(shown[i])) & 0x00FFFFFF;
                int fade = 70 + 150 * i / SAMPLES;
                graphics.fill(left, baseY - h, cx + COLUMN_W, baseY, (fade << 24) | rgb);
                graphics.fill(left, baseY - h, cx + COLUMN_W, baseY - h + 1, 0xFF000000 | rgb);
            }
            // Punto que late al final de la línea
            float pulse = 0.5F + 0.5F * (float) Math.sin(now / 220.0);
            int lastH = Math.max(1, Math.round(shown[SAMPLES - 1] * GRAPH_H / 100.0F));
            int dotX = gx + SAMPLES * COLUMN_W - 2;
            int dotA = 120 + Math.round(135 * pulse);
            graphics.fill(dotX - 1, baseY - lastH - 1, dotX + 2, baseY - lastH + 2, (dotA << 24) | 0xFFFFFF);
        }
    }
}
