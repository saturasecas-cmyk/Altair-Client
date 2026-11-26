package com.altairclient.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;

/** Fondo de boton con bordes redondeados en azul oscuro del cliente. */
public final class RoundedButtons {

    private static final int FILL = 0xFF171D3D;
    private static final int FILL_HOVER = 0xFF2A2F66;
    private static final int FILL_OFF = 0xFF11152B;
    private static final int EDGE = 0xFF2E3A78;
    private static final int EDGE_HOVER = 0xFF8B7CFF;
    private static final int EDGE_OFF = 0xFF1C2347;

    private RoundedButtons() {
    }

    public static void draw(GuiGraphics g, AbstractWidget w) {
        int x = w.getX();
        int y = w.getY();
        int width = w.getWidth();
        int height = w.getHeight();
        boolean hot = w.isHoveredOrFocused() && w.active;
        int edge = !w.active ? EDGE_OFF : hot ? EDGE_HOVER : EDGE;
        int fill = !w.active ? FILL_OFF : hot ? FILL_HOVER : FILL;
        float a = w.alpha;
        if (a < 1.0F) {
            edge = Anim.scaleAlpha(edge, a);
            fill = Anim.scaleAlpha(fill, a);
        }
        round(g, x, y, x + width, y + height, edge);
        round(g, x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    /** Rectangulo con las esquinas recortadas en escalera (radio ~3 px). */
    private static void round(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int h = y2 - y1;
        int w = x2 - x1;
        if (h < 6 || w < 8) {
            g.fill(x1, y1, x2, y2, color);
            return;
        }
        g.fill(x1 + 3, y1, x2 - 3, y1 + 1, color);
        g.fill(x1 + 1, y1 + 1, x2 - 1, y1 + 2, color);
        g.fill(x1 + 1, y1 + 2, x2 - 1, y2 - 2, color);
        g.fill(x1, y1 + 2, x2, y2 - 2, color);
        g.fill(x1 + 1, y2 - 2, x2 - 1, y2 - 1, color);
        g.fill(x1 + 3, y2 - 1, x2 - 3, y2, color);
    }
}
