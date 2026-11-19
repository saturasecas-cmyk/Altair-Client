package com.altairclient.modules.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Extra Visual Effects (los que quedaban) y Low Armor Overlay:
 * nombres minimalistas, tablist compacta, chat sin fondo, inventario más limpio,
 * fundido al abrir pantallas y tiras de armadura discretas.
 */
public final class ExtraTextures {

    private static final TextureModules.Entry NAMETAG = TextureModules.byId(TextureModules.EXTRA + "/minimal-nametag");
    private static final TextureModules.Entry TABLIST = TextureModules.byId(TextureModules.EXTRA + "/minimal-tablist");
    private static final TextureModules.Entry CHAT = TextureModules.byId(TextureModules.EXTRA + "/clean-chat");
    private static final TextureModules.Entry INVENTORY = TextureModules.byId(TextureModules.EXTRA + "/clean-inventory");
    private static final TextureModules.Entry TRANSITIONS = TextureModules.byId(TextureModules.EXTRA + "/smooth-gui-transitions");
    private static final TextureModules.Entry ARMOR = TextureModules.byId(TextureModules.PVP + "/low-armor-overlay");

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static boolean registered = false;
    private static Double savedChatBackground = null;
    private static Integer savedBlur = null;
    private static long screenOpenedAt = 0L;

    private ExtraTextures() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        RenderNameTagEvent.BUS.addListener(ExtraTextures::onNameTag);
        ScreenEvent.RenderInventoryMobEffects.BUS.addListener(ExtraTextures::onInventoryEffects);
        ScreenEvent.Opening.BUS.addListener(ExtraTextures::onScreenOpening);
        ScreenEvent.Render.Post.BUS.addListener(ExtraTextures::onScreenRendered);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ExtraTextures::onTick);
    }

    // ==================== Minimal Nametag ====================

    private static void onNameTag(RenderNameTagEvent event) {
        if (!NAMETAG.isEnabled()) {
            return;
        }
        Component original = event.getOriginalContent();
        if (original != null) {
            // Solo el nombre en texto plano, sin colores de equipo ni adornos, y un poco más pequeño
            event.setContent(Component.literal(original.getString()));
        }
        event.getPoseStack().scale(0.8F, 0.8F, 0.8F);
    }

    // ==================== Clean Inventory ====================

    /** Oculta el panel de efectos de poción del inventario. */
    private static boolean onInventoryEffects(ScreenEvent.RenderInventoryMobEffects event) {
        return INVENTORY.isEnabled();
    }

    // ==================== Chat sin fondo / inventario sin desenfoque ====================

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        OptionInstance<Double> bg = mc.options.textBackgroundOpacity();
        if (CHAT.isEnabled()) {
            if (savedChatBackground == null) {
                savedChatBackground = bg.get();
            }
            if (bg.get() != 0.0) {
                bg.set(0.0);
            }
        } else if (savedChatBackground != null) {
            bg.set(savedChatBackground);
            savedChatBackground = null;
        }

        OptionInstance<Integer> blur = mc.options.menuBackgroundBlurriness();
        if (com.altairclient.client.AltairScreen.blurActive) {
            // el menu de Altair mantiene su propio desenfoque
        } else if (INVENTORY.isEnabled()) {
            if (savedBlur == null) {
                savedBlur = blur.get();
            }
            if (blur.get() != 0) {
                blur.set(0);
            }
        } else if (savedBlur != null) {
            blur.set(savedBlur);
            savedBlur = null;
        }
    }

    // ==================== Smooth GUI Transitions ====================

    private static boolean onScreenOpening(ScreenEvent.Opening event) {
        screenOpenedAt = System.currentTimeMillis();
        return false;
    }

    private static void onScreenRendered(ScreenEvent.Render.Post event) {
        if (!TRANSITIONS.isEnabled()) {
            return;
        }
        long elapsed = System.currentTimeMillis() - screenOpenedAt;
        final long duration = 200L;
        if (elapsed < 0 || elapsed >= duration) {
            return;
        }
        float fade = 1.0F - elapsed / (float) duration;
        int alpha = Math.round(170.0F * fade * fade);
        if (alpha > 0) {
            event.getGuiGraphics().fill(0, 0, event.getScreen().width, event.getScreen().height, alpha << 24);
        }
    }

    // ==================== Minimal Tablist ====================

    public static boolean hideVanillaTablist() {
        return TABLIST.isEnabled();
    }

    private static void renderTablist(GuiGraphicsExtractor graphics, Minecraft mc, int w) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null || !mc.options.keyPlayerList.isDown()) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (PlayerInfo info : connection.getListedOnlinePlayers()) {
            Component display = info.getTabListDisplayName();
            names.add(display != null ? display.getString() : info.getProfile().name());
        }
        if (names.isEmpty()) {
            return;
        }
        names.sort(Comparator.comparing(String::toLowerCase));
        Font font = mc.font;
        final int perColumn = 20;
        int columns = (names.size() + perColumn - 1) / perColumn;
        int colWidth = 0;
        for (String n : names) {
            colWidth = Math.max(colWidth, font.width(n));
        }
        colWidth += 10;
        int rows = Math.min(perColumn, names.size());
        int panelW = colWidth * columns + 6;
        int panelH = rows * 10 + 6;
        int x0 = w / 2 - panelW / 2;
        int y0 = 10;
        graphics.fill(x0, y0, x0 + panelW, y0 + panelH, 0xAA0A0E1C);
        for (int i = 0; i < names.size(); i++) {
            int col = i / perColumn;
            int row = i % perColumn;
            graphics.text(font, names.get(i), x0 + 5 + col * colWidth, y0 + 4 + row * 10, 0xFFFFFFFF, true);
        }
    }

    // ==================== Low Armor Overlay ====================

    /** Cuatro barritas discretas (casco, peto, pantalones, botas) con la durabilidad de cada pieza. */
    private static void renderArmor(GuiGraphicsExtractor graphics, Minecraft mc, int w, int h) {
        boolean any = false;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            any |= !mc.player.getItemBySlot(slot).isEmpty();
        }
        if (!any) {
            return;
        }
        int x = w / 2 + 125;
        int y = h - 21;
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(ARMOR_SLOTS[i]);
            int by = y + i * 4;
            graphics.fill(x - 1, by - 1, x + 21, by + 2, 0x88000000);
            if (stack.isEmpty()) {
                continue;
            }
            float fraction = 1.0F;
            if (stack.isDamageableItem() && stack.getMaxDamage() > 0) {
                fraction = 1.0F - stack.getDamageValue() / (float) stack.getMaxDamage();
            }
            fraction = Math.max(0.0F, Math.min(1.0F, fraction));
            int red = Math.round(255 * Math.min(1.0F, (1.0F - fraction) * 2.0F));
            int green = Math.round(255 * Math.min(1.0F, fraction * 2.0F));
            int color = 0xFF000000 | (red << 16) | (green << 8) | 0x30;
            graphics.fill(x, by, x + Math.max(1, Math.round(20 * fraction)), by + 1, color);
        }
    }

    // ==================== HUD ====================

    public static void render(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.hud.isHidden()) {
            return;
        }
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        if (ARMOR.isEnabled()) {
            renderArmor(graphics, mc, w, h);
        }
        if (TABLIST.isEnabled()) {
            renderTablist(graphics, mc, w);
        }
    }
}
