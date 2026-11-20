package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Base de los módulos de HUD: posición movible en el editor, activar/desactivar y vista previa. */
public abstract class HudElement {

    protected static final int WHITE = 0xFFFFFFFF;
    protected static final int MUTED = 0xFF929DBD;
    protected static final int BACKGROUND = 0xAA10152A;
    protected static final int CYAN = 0xFF62C8FF;
    protected static final int GREEN = 0xFF5DFFA8;
    protected static final int YELLOW = 0xFFFFD75D;
    protected static final int RED = 0xFFFF5C77;
    protected static final int GOLD = 0xFFFFC857;

    private boolean enabled = false;
    private int x;
    private int y;

    protected HudElement(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    /** Dibuja el módulo. En la vista previa (editor del HUD) se usan datos de ejemplo. */
    protected abstract void draw(GuiGraphicsExtractor graphics, boolean preview);

    public void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!enabled || mc == null || mc.player == null || mc.font == null) {
            return;
        }
        draw(graphics, false);
    }

    public void renderPreview(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }
        draw(graphics, true);
    }

    /** Fondo con la barra de color a la izquierda, como el resto de módulos del HUD. */
    protected void box(GuiGraphicsExtractor graphics, int width, int height, int accent) {
        graphics.fill(x - 5, y - 4, x + width + 8, y + height - 4, BACKGROUND);
        graphics.fill(x - 5, y - 4, x - 2, y + height - 4, accent);
    }

    /** Solo el fondo, sin la barra de color. */
    protected void plainBox(GuiGraphicsExtractor graphics, int width, int height) {
        graphics.fill(x - 5, y - 4, x + width + 8, y + height - 4, BACKGROUND);
    }
}
