package com.altairclient.modules.mods;

import com.altairclient.client.Anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class CPSCounter {

    private static final CPSCounter INSTANCE = new CPSCounter();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;

    private boolean enabled = false;
    private int x = 10;
    private int y = 30;

    private final List<Long> leftClicks = new ArrayList<>();
    private final List<Long> rightClicks = new ArrayList<>();

    private boolean leftWasDown = false;
    private boolean rightWasDown = false;

    private CPSCounter() {
    }

    public static CPSCounter getInstance() {
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

    public int getWidth() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.font != null) {
            return minecraft.font.width("CPS: 00 | 00") + 13;
        }
        return 75;
    }

    public int getHeight() {
        return 18;
    }

    public void addLeftClick() {
        leftClicks.add(System.currentTimeMillis());
    }

    public void addRightClick() {
        rightClicks.add(System.currentTimeMillis());
    }

    /**
     * Detecta pulsaciones nuevas de los botones del ratón (flanco de subida). Es seguro llamarlo
     * varias veces por frame; solo cuenta cuando el botón pasa de suelto a pulsado.
     */
    public void poll() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.mouseHandler == null) {
            return;
        }

        boolean left = minecraft.mouseHandler.isLeftPressed();
        boolean right = minecraft.mouseHandler.isRightPressed();

        if (left && !leftWasDown) {
            addLeftClick();
        }
        if (right && !rightWasDown) {
            addRightClick();
        }

        leftWasDown = left;
        rightWasDown = right;
    }

    public int getLeftCPS() {
        long now = System.currentTimeMillis();
        leftClicks.removeIf(time -> now - time > 1000);
        return leftClicks.size();
    }

    public int getRightCPS() {
        long now = System.currentTimeMillis();
        rightClicks.removeIf(time -> now - time > 1000);
        return rightClicks.size();
    }

    private final Anim.Counter leftAnim = new Anim.Counter(2.0F);
    private final Anim.Counter rightAnim = new Anim.Counter(2.0F);
    private final Anim.Smooth widthAnim = new Anim.Smooth();

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        int lCps = Math.round(leftAnim.update(getLeftCPS(), 16.0F));
        int rCps = Math.round(rightAnim.update(getRightCPS(), 16.0F));

        String text = "CPS: " + lCps + " | " + rCps;

        int textWidth = Math.round(widthAnim.update(minecraft.font.width(text), 20.0F));
        int textColor = leftAnim.flash() >= rightAnim.flash() ? leftAnim.color(WHITE) : rightAnim.color(WHITE);

        // Fondo del indicador
        graphics.fill(
                x - 5,
                y - 4,
                x + textWidth + 8,
                y + 14,
                BACKGROUND
        );

        // Línea decorativa morada
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                PURPLE
        );

        // Texto CPS
        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                textColor,
                false
        );
    }

}
