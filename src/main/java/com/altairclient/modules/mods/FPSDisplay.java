package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FPSDisplay {

    private static final FPSDisplay INSTANCE = new FPSDisplay();

    public static FPSDisplay getInstance() {
        return INSTANCE;
    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;

    private boolean enabled = true;

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft == null || minecraft.player == null) {
            return;
        }

        int fps = minecraft.getFps();
        String text = "FPS: " + fps;

        int x = 10;
        int y = 10;

        int textWidth = minecraft.font.width(text);

        // 1. Fondo oscuro
        graphics.fill(
                x - 5,
                y - 4,
                x + textWidth + 8,
                y + 14,
                BACKGROUND
        );

        // 2. Barra violeta neón
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                PURPLE
        );

        // 3. Texto FPS en blanco
        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                WHITE,
                false
        );
    }
}
