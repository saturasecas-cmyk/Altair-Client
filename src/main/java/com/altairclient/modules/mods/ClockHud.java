package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Clock: shows your computer's local time. Movable in the HUD Editor. */
public class ClockHud extends HudElement {

    private static final ClockHud INSTANCE = new ClockHud();
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ClockHud() {
        super(10, 150);
    }

    public static ClockHud getInstance() {
        return INSTANCE;
    }

    @Override
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.font != null ? mc.font.width("⏰ 00:00:00") : 60;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    protected void draw(GuiGraphicsExtractor graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        box(graphics, getWidth(), getHeight(), CYAN);
        graphics.text(mc.font, "⏰ ", getX(), getY(), MUTED, false);
        graphics.text(mc.font, LocalTime.now().format(FORMAT), getX() + mc.font.width("⏰ "), getY(), WHITE, false);
    }
}
