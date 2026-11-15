package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Cuenta los tótems de la inmortalidad que llevas en el inventario (incluida la mano secundaria). */
public class TotemCounter extends HudElement {

    private static final TotemCounter INSTANCE = new TotemCounter();

    private TotemCounter() {
        super(10, 70);
    }

    public static TotemCounter getInstance() {
        return INSTANCE;
    }

    @Override
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        return (mc != null && mc.font != null ? mc.font.width("T\u00F3tems: 00") : 60) + 13;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    static int count(Minecraft mc, boolean arrows) {
        Inventory inv = mc.player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            boolean match = arrows
                    ? stack.is(Items.ARROW) || stack.is(Items.SPECTRAL_ARROW) || stack.is(Items.TIPPED_ARROW)
                    : stack.is(Items.TOTEM_OF_UNDYING);
            if (match) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    protected void draw(GuiGraphicsExtractor graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        int n = preview ? 3 : count(mc, false);
        String label = "T\u00F3tems: ";
        String value = String.valueOf(n);
        box(graphics, mc.font.width(label + value), getHeight(), GOLD);
        graphics.text(mc.font, label, getX(), getY(), WHITE, false);
        graphics.text(mc.font, value, getX() + mc.font.width(label), getY(), n > 0 ? GREEN : RED, false);
    }
}
