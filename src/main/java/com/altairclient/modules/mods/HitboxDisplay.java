package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public class HitboxDisplay {

    private static final HitboxDisplay INSTANCE = new HitboxDisplay();

    private boolean enabled = false;

    private HitboxDisplay() {
    }

    public static HitboxDisplay getInstance() {
        return INSTANCE;
    }

    public void toggle() {
        enabled = !enabled;
        applyState();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        applyState();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void applyState() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getEntityRenderDispatcher() != null) {
            mc.getEntityRenderDispatcher().setRenderHitBoxes(enabled);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        if (!INSTANCE.enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getEntityRenderDispatcher() != null) {
            if (!mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                mc.getEntityRenderDispatcher().setRenderHitBoxes(true);
            }
        }
    }
}
