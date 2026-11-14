package com.altairclient.modules.mods;

import com.altairclient.client.AltairScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ComboCounter {

    private static final ComboCounter INSTANCE = new ComboCounter();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;

    private boolean enabled = false;
    private int x = 10;
    private int y = 50;

    private int combo = 0;
    private long lastHitTime = 0;

    private ComboCounter() {
    }

    public static ComboCounter getInstance() {
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
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.font != null) {
            return minecraft.font.width("Combo: 00") + 13;
        }
        return 75;
    }

    public int getHeight() {
        return 18;
    }

    public void registerHit() {
        long now = System.currentTimeMillis();
        if (now - lastHitTime > 2500) {
            combo = 1;
        } else {
            combo++;
        }
        lastHitTime = now;
    }

    public int getCombo() {
        long now = System.currentTimeMillis();
        if (now - lastHitTime > 2500) {
            combo = 0;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && mc.player.hurtTime > 0) {
            combo = 0;
        }
        return combo;
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }
        renderContent(graphics, "Combo: " + getCombo());
    }

    public void renderPreview(GuiGraphicsExtractor graphics) {
        renderContent(graphics, "Combo: 3");
    }

    private void renderContent(GuiGraphicsExtractor graphics, String text) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.font == null) {
            return;
        }

        int textWidth = minecraft.font.width(text);

        // Fondo del indicador
        graphics.fill(
                x - 5,
                y - 4,
                x + textWidth + 8,
                y + 14,
                BACKGROUND
        );

        // Línea decorativa cósmica
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                PURPLE
        );

        // Texto del Combo
        graphics.text(
                minecraft.font,
                text,
                x,
                y,
                WHITE,
                false
        );
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && event.getEntity() == mc.player && event.getTarget() != null) {
            INSTANCE.registerHit();
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
