package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Cuenta las flechas (normales, espectrales y con efecto) que llevas en el inventario. */
public class ArrowCounter extends HudElement {

    private static final ArrowCounter INSTANCE = new ArrowCounter();

    private ArrowCounter() {
        super(10, 90);
    }

    public static ArrowCounter getInstance() {
        return INSTANCE;
    }

    @Override
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.font != null ? mc.font.width("Flechas: 0000") : 70) + 13;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    protected void draw(GuiGraphicsExtractor graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        int n = preview ? 64 : TotemCounter.count(mc, true);
        String label = "Flechas: ";
        String value = String.valueOf(n);
        box(graphics, mc.font.width(label + value), getHeight(), CYAN);
        graphics.text(mc.font, label, getX(), getY(), WHITE, false);
        graphics.text(mc.font, value, getX() + mc.font.width(label), getY(), n > 0 ? GREEN : RED, false);
    }
}
