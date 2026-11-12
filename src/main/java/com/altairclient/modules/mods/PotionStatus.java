package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;

public class PotionStatus {

    private static final PotionStatus INSTANCE = new PotionStatus();

    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFF929DBD;

    private boolean enabled = false;
    private int x = -1;
    private int y = -1;

    private PotionStatus() {
    }

    public static PotionStatus getInstance() {
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
        if (x < 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                return mc.getWindow().getGuiScaledWidth() - getWidth() - 10;
            }
            return 250;
        }
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        if (y < 0) {
            return 80;
        }
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return 120;
    }

    public int getHeight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            int count = mc.player.getActiveEffects().size();
            if (count > 0) {
                return count * 16 + 6;
            }
        }
        return 22;
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (effects.isEmpty()) {
            return;
        }

        int curX = getX();
        int curY = getY();
        int boxW = getWidth();
        int rowHeight = 16;
        int boxH = effects.size() * rowHeight + 6;

        // Fondo y acento cósmico
        graphics.fill(curX - 4, curY - 3, curX + boxW, curY + boxH - 3, BACKGROUND);
        graphics.fill(curX + boxW - 2, curY - 3, curX + boxW, curY + boxH - 3, PURPLE);

        int lineY = curY;
        for (MobEffectInstance effect : effects) {
            String name = Component.translatable(effect.getDescriptionId()).getString();
            int amp = effect.getAmplifier();
            if (amp > 0) {
                name += " " + (amp + 1);
            }

            int durationSecs = effect.getDuration() / 20;
            String time = String.format("%02d:%02d", durationSecs / 60, durationSecs % 60);

            // Nombre del efecto
            graphics.text(mc.font, name, curX + 2, lineY + 1, WHITE, false);

            // Tiempo restante alineado a la derecha
            int timeW = mc.font.width(time);
            graphics.text(mc.font, time, curX + boxW - timeW - 5, lineY + 1, MUTED, false);

            lineY += rowHeight;
        }
    }

}
