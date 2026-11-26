package com.altairclient.modules.mods;

import com.altairclient.client.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/** Custom Crosshair: mira personalizada con modelos pensados para PvP. */
public final class CustomCrosshair {

    public static final String[] STYLE_NAMES = {
            "Dot", "Cross", "Gapped cross", "Circle", "Circle + dot", "T", "X", "Corners"
    };

    public static final int MIN_SIZE = 2;
    public static final int MAX_SIZE = 10;

    private static boolean enabled = false;
    private static int style = 2;
    private static int colorIndex = 0;
    private static int size = 5;

    private CustomCrosshair() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static int getStyle() {
        return style;
    }

    public static void setStyle(int v) {
        style = Math.max(0, Math.min(STYLE_NAMES.length - 1, v));
    }

    public static int getColorIndex() {
        return colorIndex;
    }

    public static void setColorIndex(int v) {
        colorIndex = Math.max(0, Math.min(Palette.MAX_INDEX, v));
    }

    public static int getSize() {
        return size;
    }

    public static void setSize(int v) {
        size = Math.max(MIN_SIZE, Math.min(MAX_SIZE, v));
    }

    /** Dibuja la mira en el centro de la pantalla. */
    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.getWindow() == null) {
            return;
        }
        drawAt(graphics, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2);
    }

    /** Dibuja la mira centrada en (cx, cy). También sirve para la vista previa. */
    public static void drawAt(GuiGraphics graphics, int cx, int cy) {
        List<int[]> rects = shape(cx, cy);
        // Contorno oscuro de 1 px para que se vea sobre cualquier fondo, y después el color.
        for (int[] r : rects) {
            graphics.fill(r[0] - 1, r[1] - 1, r[2] + 1, r[3] + 1, 0xAA000000);
        }
        int color = Palette.color(colorIndex);
        for (int[] r : rects) {
            graphics.fill(r[0], r[1], r[2], r[3], color);
        }
    }

    private static List<int[]> shape(int cx, int cy) {
        List<int[]> out = new ArrayList<>();
        int s = size;
        int gap = Math.max(2, s / 2);
        switch (style) {
            case 0 -> dot(out, cx, cy);
            case 1 -> {
                out.add(new int[]{cx - s, cy, cx + s + 1, cy + 1});
                out.add(new int[]{cx, cy - s, cx + 1, cy + s + 1});
            }
            case 2 -> {
                out.add(new int[]{cx - gap - s, cy, cx - gap, cy + 1});
                out.add(new int[]{cx + gap + 1, cy, cx + gap + s + 1, cy + 1});
                out.add(new int[]{cx, cy - gap - s, cx + 1, cy - gap});
                out.add(new int[]{cx, cy + gap + 1, cx + 1, cy + gap + s + 1});
            }
            case 3, 4 -> {
                int r = s + 1;
                int steps = Math.max(16, r * 8);
                int lastX = Integer.MIN_VALUE;
                int lastY = Integer.MIN_VALUE;
                for (int i = 0; i < steps; i++) {
                    double a = i * Math.PI * 2.0 / steps;
                    int px = cx + (int) Math.round(Math.cos(a) * r);
                    int py = cy + (int) Math.round(Math.sin(a) * r);
                    if (px != lastX || py != lastY) {
                        out.add(new int[]{px, py, px + 1, py + 1});
                        lastX = px;
                        lastY = py;
                    }
                }
                if (style == 4) {
                    dot(out, cx, cy);
                }
            }
            case 5 -> {
                out.add(new int[]{cx - s, cy, cx + s + 1, cy + 1});
                out.add(new int[]{cx, cy, cx + 1, cy + s + 1});
            }
            case 6 -> {
                for (int i = gap; i <= gap + s - 1; i++) {
                    out.add(new int[]{cx - i, cy - i, cx - i + 1, cy - i + 1});
                    out.add(new int[]{cx + i, cy - i, cx + i + 1, cy - i + 1});
                    out.add(new int[]{cx - i, cy + i, cx - i + 1, cy + i + 1});
                    out.add(new int[]{cx + i, cy + i, cx + i + 1, cy + i + 1});
                }
            }
            default -> {
                int d = s + 1;
                int a = Math.max(2, s / 2 + 1);
                for (int sx = -1; sx <= 1; sx += 2) {
                    for (int sy = -1; sy <= 1; sy += 2) {
                        int x0 = cx + sx * d;
                        int y0 = cy + sy * d;
                        int x1 = x0 - sx * a;
                        int y1 = y0 - sy * a;
                        out.add(new int[]{Math.min(x0, x1), y0, Math.max(x0, x1) + 1, y0 + 1});
                        out.add(new int[]{x0, Math.min(y0, y1), x0 + 1, Math.max(y0, y1) + 1});
                    }
                }
            }
        }
        return out;
    }

    private static void dot(List<int[]> out, int cx, int cy) {
        out.add(new int[]{cx - 1, cy - 1, cx + 1, cy + 1});
    }
}
