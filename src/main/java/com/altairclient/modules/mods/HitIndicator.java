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
public class HitIndicator {

    private static final HitIndicator INSTANCE = new HitIndicator();

    private boolean enabled = false;
    private long lastHitTime = 0;

    private HitIndicator() {
    }

    public static HitIndicator getInstance() {
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

    public void registerHit() {
        this.lastHitTime = System.currentTimeMillis();
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.getWindow() == null) {
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - lastHitTime;
        if (elapsed > 280) {
            return;
        }

        float alpha = 1.0f - ((float) elapsed / 280.0f);
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));

        int purpleMarker = (a << 24) | 0x9B7BFF;
        int whiteCore = (a << 24) | 0xFFFFFF;

        int cx = mc.getWindow().getGuiScaledWidth() / 2;
        int cy = mc.getWindow().getGuiScaledHeight() / 2;

        // Diagonal Superior-Izquierda
        graphics.fill(cx - 7, cy - 7, cx - 3, cy - 6, purpleMarker);
        graphics.fill(cx - 7, cy - 6, cx - 6, cy - 3, purpleMarker);
        graphics.fill(cx - 5, cy - 5, cx - 4, cy - 4, whiteCore);

        // Diagonal Superior-Derecha
        graphics.fill(cx + 4, cy - 7, cx + 8, cy - 6, purpleMarker);
        graphics.fill(cx + 7, cy - 6, cx + 8, cy - 3, purpleMarker);
        graphics.fill(cx + 5, cy - 5, cx + 6, cy - 4, whiteCore);

        // Diagonal Inferior-Izquierda
        graphics.fill(cx - 7, cy + 7, cx - 3, cy + 8, purpleMarker);
        graphics.fill(cx - 7, cy + 4, cx - 6, cy + 7, purpleMarker);
        graphics.fill(cx - 5, cy + 5, cx - 4, cy + 6, whiteCore);

        // Diagonal Inferior-Derecha
        graphics.fill(cx + 4, cy + 7, cx + 8, cy + 8, purpleMarker);
        graphics.fill(cx + 7, cy + 4, cx + 8, cy + 7, purpleMarker);
        graphics.fill(cx + 5, cy + 5, cx + 6, cy + 6, whiteCore);
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
