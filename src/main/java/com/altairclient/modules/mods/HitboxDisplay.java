package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
        if (mc == null) {
            return;
        }

        // Intento 1: EntityRenderDispatcher (métodos o campos de hitboxes)
        try {
            Object dispatcher = mc.getEntityRenderDispatcher();
            if (dispatcher != null) {
                for (Method method : dispatcher.getClass().getMethods()) {
                    String name = method.getName().toLowerCase();
                    if (name.contains("hitbox") && method.getParameterCount() == 1 && method.getParameterTypes()[0] == boolean.class) {
                        method.setAccessible(true);
                        method.invoke(dispatcher, enabled);
                        return;
                    }
                }

                for (Field field : dispatcher.getClass().getDeclaredFields()) {
                    String name = field.getName().toLowerCase();
                    if (field.getType() == boolean.class && name.contains("hitbox")) {
                        field.setAccessible(true);
                        field.setBoolean(dispatcher, enabled);
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // Intento 2: DebugEntries de la nueva arquitectura de depuración
        try {
            for (Field field : mc.getClass().getDeclaredFields()) {
                if (field.getName().toLowerCase().contains("debugentri")) {
                    field.setAccessible(true);
                    Object debugEntries = field.get(mc);
                    if (debugEntries != null) {
                        for (Method method : debugEntries.getClass().getMethods()) {
                            if (method.getName().equals("toggleStatus") && method.getParameterCount() == 1) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        if (!INSTANCE.enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }

        try {
            Object dispatcher = mc.getEntityRenderDispatcher();
            if (dispatcher != null) {
                for (Field field : dispatcher.getClass().getDeclaredFields()) {
                    String name = field.getName().toLowerCase();
                    if (field.getType() == boolean.class && name.contains("hitbox")) {
                        field.setAccessible(true);
                        if (!field.getBoolean(dispatcher)) {
                            field.setBoolean(dispatcher, true);
                        }
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
