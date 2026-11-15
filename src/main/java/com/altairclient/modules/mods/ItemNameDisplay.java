package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Item Name: muestra el nombre del objeto que llevas en la mano, con el color de su rareza. */
public class ItemNameDisplay extends HudElement {

    private static final ItemNameDisplay INSTANCE = new ItemNameDisplay();

    private ItemNameDisplay() {
        super(10, 130);
    }

    public static ItemNameDisplay getInstance() {
        return INSTANCE;
    }

    private Component currentName(Minecraft mc, boolean preview) {
        if (preview) {
            return Component.literal("Espada de diamante");
        }
        ItemStack stack = mc.player.getMainHandItem();
        return stack.isEmpty() ? null : stack.getHoverName();
    }

    @Override
    public int getWidth() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.font != null) {
            return mc.font.width("Espada de diamante") + 13;
        }
        return 110;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        // Con la mano vacía no se dibuja nada.
        if (isEnabled() && mc != null && mc.player != null && mc.player.getMainHandItem().isEmpty()) {
            return;
        }
        super.render(graphics);
    }

    @Override
    protected void draw(GuiGraphicsExtractor graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        Component name = currentName(mc, preview);
        if (name == null) {
            return;
        }
        box(graphics, mc.font.width(name), getHeight(), GOLD);
        graphics.text(mc.font, name, getX(), getY(), WHITE, false);
    }
}
