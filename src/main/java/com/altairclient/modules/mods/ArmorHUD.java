package com.altairclient.modules.mods;

import com.altairclient.client.AltairScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ArmorHUD {

    private static final ArmorHUD INSTANCE = new ArmorHUD();

    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREEN = 0xFF55FF55;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int RED = 0xFFFF5555;

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };

    private boolean enabled = false;
    private int x = 10;
    private int y = 52;

    private ArmorHUD() {
    }

    public static ArmorHUD getInstance() {
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
        return 77;
    }

    public int getHeight() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.player != null) {
            int count = 0;
            for (EquipmentSlot slot : SLOTS) {
                if (!minecraft.player.getItemBySlot(slot).isEmpty()) {
                    count++;
                }
            }
            if (count > 0) {
                return count * 18 + 6;
            }
        }
        return 78;
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;

        // Comprobar si tiene al menos una pieza equipada
        boolean hasAnyArmor = false;
        for (EquipmentSlot slot : SLOTS) {
            if (!player.getItemBySlot(slot).isEmpty()) {
                hasAnyArmor = true;
                break;
            }
        }

        if (!hasAnyArmor) {
            return;
        }

        int rowHeight = 18;
        int boxWidth = 72;
        int count = 0;

        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                count++;
            }
        }

        int boxHeight = count * rowHeight + 6;

        // Fondo y borde cósmico
        graphics.fill(x - 5, y - 4, x + boxWidth, y + boxHeight - 4, BACKGROUND);
        graphics.fill(x - 5, y - 4, x - 2, y + boxHeight - 4, PURPLE);

        int currentY = y;
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            // Renderizar el icono del item
            graphics.item(stack, x, currentY);
            graphics.itemDecorations(minecraft.font, stack, x, currentY);

            // Texto de durabilidad
            if (stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int damage = stack.getDamageValue();
                int durability = maxDamage - damage;
                int percent = (durability * 100) / maxDamage;

                int color = percent > 50 ? GREEN : (percent > 25 ? YELLOW : RED);
                String durText = durability + "";

                graphics.text(
                        minecraft.font,
                        durText,
                        x + 20,
                        currentY + 4,
                        color,
                        false
                );
            } else {
                graphics.text(
                        minecraft.font,
                        "100%",
                        x + 20,
                        currentY + 4,
                        WHITE,
                        false
                );
            }

            currentY += rowHeight;
        }
    }

    @SubscribeEvent
    public static void onRenderChatOverlay(CustomizeGuiOverlayEvent.Chat event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.screen() == null) {
            INSTANCE.render(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof AltairScreen)) {
            INSTANCE.render(event.getGuiGraphics());
        }
    }
}
