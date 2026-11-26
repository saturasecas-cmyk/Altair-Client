package com.altairclient.modules.textures;

import com.altairclient.client.Anim;
import com.altairclient.client.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Texturas que se dibujan en el HUD: hotbar mínima, resaltado del objeto seleccionado, barra de XP simple
 * y barra de progreso al romper bloques. Las versiones de vanilla se ocultan desde {@code AltairHud}.
 */
public final class HudTextures {

    private static final TextureModules.Entry SIMPLE_XP = TextureModules.byId(TextureModules.PVP + "/simple-xp-bar");
    private static final TextureModules.Entry MIN_HOTBAR = TextureModules.byId(TextureModules.PVP + "/minimal-hotbar");
    private static final TextureModules.Entry HIGHLIGHT = TextureModules.byId(TextureModules.EXTRA + "/hotbar-highlight");
    private static final TextureModules.Entry BREAK_BAR = TextureModules.byId(TextureModules.BREAKING + "/breaking-progress-bar");

    private HudTextures() {
    }

    // Condiciones para ocultar las capas de vanilla (se usan desde AltairHud)
    public static boolean hideVanillaHotbar() {
        return MIN_HOTBAR.isEnabled();
    }

    public static boolean hideVanillaXpBar() {
        return SIMPLE_XP.isEnabled();
    }

    public static boolean hideScoreboard() {
        return com.altairclient.modules.mods.HudHider.isScoreboardHidden();
    }

    public static boolean hideBossBar() {
        return com.altairclient.modules.mods.HudHider.isBossBarHidden();
    }

    /** Dibuja todo lo de este apartado en el HUD. */
    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.options.hideGui) {
            return;
        }
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        if (MIN_HOTBAR.isEnabled()) {
            renderMinimalHotbar(graphics, mc, p, w, h);
        } else if (HIGHLIGHT.isEnabled()) {
            outline(graphics, w / 2 - 91 + p.getInventory().getSelectedSlot() * 20, h - 22, 22, 22,
                    Palette.color(HIGHLIGHT.getColorIndex()));
        }
        if (SIMPLE_XP.isEnabled()) {
            renderSimpleXp(graphics, mc, p, w, h);
        }
        if (BREAK_BAR.isEnabled()) {
            renderBreakBar(graphics, mc, w, h);
        }
    }

    private static void renderMinimalHotbar(GuiGraphics graphics, Minecraft mc, LocalPlayer p, int w, int h) {
        int x0 = w / 2 - 91;
        int y0 = h - 22;
        graphics.fill(x0, y0, x0 + 182, y0 + 22, 0x990A0E1C);
        Inventory inv = p.getInventory();
        int selected = inv.getSelectedSlot();
        int color = Palette.color(MIN_HOTBAR.getColorIndex());
        for (int i = 0; i < 9; i++) {
            if (i == selected) {
                graphics.fill(x0 + i * 20 + 1, y0 + 1, x0 + i * 20 + 21, y0 + 21, Anim.scaleAlpha(color, 0.28F));
            }
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                int ix = x0 + 3 + i * 20;
                int iy = y0 + 3;
                graphics.renderItem(stack, ix, iy);
                graphics.renderItemDecorations(mc.font, stack, ix, iy);
            }
        }
        outline(graphics, x0 + selected * 20, y0, 22, 22, color);
    }

    private static void renderSimpleXp(GuiGraphics graphics, Minecraft mc, LocalPlayer p, int w, int h) {
        int x0 = w / 2 - 91;
        int y = h - 27;
        int color = Palette.color(SIMPLE_XP.getColorIndex());
        graphics.fill(x0, y, x0 + 182, y + 2, 0x88000000);
        int filled = Math.round(182 * Math.max(0.0F, Math.min(1.0F, p.experienceProgress)));
        if (filled > 0) {
            graphics.fill(x0, y, x0 + filled, y + 2, color);
        }
        if (p.experienceLevel > 0) {
            String level = String.valueOf(p.experienceLevel);
            graphics.drawString(mc.font, level, w / 2 - mc.font.width(level) / 2, y - 10, color, true);
        }
    }

    private static void renderBreakBar(GuiGraphics graphics, Minecraft mc, int w, int h) {
        MultiPlayerGameMode gm = mc.gameMode;
        if (gm == null || !gm.isDestroying()) {
            return;
        }
        int stage = Math.max(-1, Math.min(9, gm.getDestroyStage()));
        float fraction = (stage + 1) / 10.0F;
        int bw = 44;
        int x = w / 2 - bw / 2;
        int y = h / 2 + 14;
        int color = Palette.color(BREAK_BAR.getColorIndex());
        graphics.fill(x - 1, y - 1, x + bw + 1, y + 4, 0xAA05070F);
        graphics.fill(x, y, x + Math.round(bw * fraction), y + 3, color);
    }

    private static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }
}
