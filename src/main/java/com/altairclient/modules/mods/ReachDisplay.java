package com.altairclient.modules.mods;

import com.altairclient.client.AltairScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class ReachDisplay {

    private static final ReachDisplay INSTANCE = new ReachDisplay();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int CYAN = 0xFF62C8FF;

    private boolean enabled = false;
    private int x = 10;
    private int y = 70;

    private double lastReach = 0.0;
    private long lastReachTime = 0;

    private ReachDisplay() {
    }

    public static ReachDisplay getInstance() {
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
            return minecraft.font.width("Reach: 0.00m") + 13;
        }
        return 75;
    }

    public int getHeight() {
        return 18;
    }

    public void setReach(double reach) {
        this.lastReach = reach;
        this.lastReachTime = System.currentTimeMillis();
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        String text;
        long now = System.currentTimeMillis();
        if (now - lastReachTime < 4000 && lastReach > 0.0) {
            text = String.format(Locale.US, "Reach: %.2fm", lastReach);
        } else {
            text = "Reach: 0.00m";
        }

        renderContent(graphics, text);
    }

    public void renderPreview(GuiGraphicsExtractor graphics) {
        renderContent(graphics, "Reach: 2.87m");
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

        // Línea decorativa azul/cian
        graphics.fill(
                x - 5,
                y - 4,
                x - 2,
                y + 14,
                CYAN
        );

        // Texto Reach
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
            Entity target = event.getTarget();
            Vec3 eye = mc.player.getEyePosition();
            AABB box = target.getBoundingBox();

            double cx = Math.max(box.minX, Math.min(eye.x, box.maxX));
            double cy = Math.max(box.minY, Math.min(eye.y, box.maxY));
            double cz = Math.max(box.minZ, Math.min(eye.z, box.maxZ));

            double dist = eye.distanceTo(new Vec3(cx, cy, cz));
            INSTANCE.setReach(dist);
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
