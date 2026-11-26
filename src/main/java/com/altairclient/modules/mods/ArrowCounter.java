package com.altairclient.modules.mods;

import com.altairclient.client.Anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

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
        return (mc != null && mc.font != null ? mc.font.width("Arrows: 0000") : 70) + 13;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    private final Anim.Counter counter = new Anim.Counter(1.0F);
    private final Anim.Smooth widthAnim = new Anim.Smooth();

    @Override
    protected void draw(GuiGraphics graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        int n = preview ? 64 : TotemCounter.count(mc, true);
        String label = "Arrows: ";
        int shown = Math.round(counter.update(n, 14.0F));
        String value = String.valueOf(shown);
        box(graphics, Math.round(widthAnim.update(mc.font.width(label + value), 20.0F)), getHeight(), CYAN);
        graphics.drawString(mc.font, label, getX(), getY(), WHITE, false);
        graphics.drawString(mc.font, value, getX() + mc.font.width(label), getY(), counter.color(n > 0 ? GREEN : RED), false);
    }
}
