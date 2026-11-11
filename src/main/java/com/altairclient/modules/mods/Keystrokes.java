package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public class Keystrokes {

    private static final Keystrokes INSTANCE = new Keystrokes();

    private static final int BG_NORMAL = 0xAA10152A;
    private static final int BG_PRESSED = 0xCC9B7BFF;
    private static final int BORDER_NORMAL = 0xFF303957;
    private static final int BORDER_PRESSED = 0xFFFFFFFF;
    private static final int TEXT_NORMAL = 0xFF929DBD;
    private static final int TEXT_PRESSED = 0xFFFFFFFF;

    private boolean enabled = false;
    private int x = -1; // -1 indica posición automática inicial en el lado derecho
    private int y = -1;

    private Keystrokes() {
    }

    public static Keystrokes getInstance() {
        return INSTANCE;
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
        if (x < 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                return mc.getWindow().getGuiScaledWidth() - getWidth() - 10;
            }
            return 300;
        }
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        if (y < 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                return mc.getWindow().getGuiScaledHeight() - getHeight() - 15;
            }
            return 150;
        }
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return 66; // 3 teclas de 20px + dos márgenes de 3px
    }

    public int getHeight() {
        return 74; // W (20) + ASD (20) + LMB/RMB (20) + Espacio (10) + gaps
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        int curX = getX();
        int curY = getY();

        boolean wPressed = mc.options.keyUp.isDown();
        boolean aPressed = mc.options.keyLeft.isDown();
        boolean sPressed = mc.options.keyDown.isDown();
        boolean dPressed = mc.options.keyRight.isDown();
        boolean lmbPressed = mc.options.keyAttack.isDown();
        boolean rmbPressed = mc.options.keyUse.isDown();
        boolean spacePressed = mc.options.keyJump.isDown();

        // Tecla W
        drawKey(graphics, mc, "W", curX + 23, curY, 20, 18, wPressed);

        // Teclas A, S, D
        drawKey(graphics, mc, "A", curX, curY + 20, 20, 18, aPressed);
        drawKey(graphics, mc, "S", curX + 23, curY + 20, 20, 18, sPressed);
        drawKey(graphics, mc, "D", curX + 46, curY + 20, 20, 18, dPressed);

        // LMB y RMB
        drawKey(graphics, mc, "LMB", curX, curY + 40, 31, 18, lmbPressed);
        drawKey(graphics, mc, "RMB", curX + 35, curY + 40, 31, 18, rmbPressed);

        // Tecla Espacio
        drawKey(graphics, mc, "\u2014\u2014\u2014", curX, curY + 60, 66, 12, spacePressed);
    }

    private void drawKey(
            GuiGraphicsExtractor graphics,
            Minecraft mc,
            String text,
            int kx,
            int ky,
            int kw,
            int kh,
            boolean pressed
    ) {
        int bg = pressed ? BG_PRESSED : BG_NORMAL;
        int border = pressed ? BORDER_PRESSED : BORDER_NORMAL;
        int textColor = pressed ? TEXT_PRESSED : TEXT_NORMAL;

        // Fondo
        graphics.fill(kx, ky, kx + kw, ky + kh, bg);

        // Borde fino
        graphics.fill(kx, ky, kx + kw, ky + 1, border);
        graphics.fill(kx, ky + kh - 1, kx + kw, ky + kh, border);
        graphics.fill(kx, ky + 1, kx + 1, ky + kh, border);   // ← CORREGIDO: borde izquierdo
        graphics.fill(kx + kw - 1, ky, kx + kw, ky + kh, border);

        // Texto centrado
        int tw = mc.font.width(text);
        int tx = kx + (kw - tw) / 2;
        int ty = ky + (kh - 8) / 2;
        graphics.text(mc.font, text, tx, ty, textColor, false);
    }

}
