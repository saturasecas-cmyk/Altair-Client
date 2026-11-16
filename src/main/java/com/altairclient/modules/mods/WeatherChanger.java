package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.event.TickEvent;

/** Weather Changer: cambia el clima solo en tu cliente (quitar lluvia o nieve, o forzar lluvia y tormenta). */
public final class WeatherChanger {

    public static final String[] MODE_NAMES = {"Despejado", "Lluvia", "Tormenta"};

    private static boolean enabled = false;
    private static boolean registered = false;
    private static int mode = 0;

    private WeatherChanger() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            TickEvent.ClientTickEvent.Post.BUS.addListener(WeatherChanger::onTick);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int v) {
        mode = Math.max(0, Math.min(MODE_NAMES.length - 1, v));
    }

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        if (!enabled) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        float rain = mode == 0 ? 0.0F : 1.0F;
        float thunder = mode == 2 ? 1.0F : 0.0F;
        level.oRainLevel = rain;
        level.rainLevel = rain;
        level.oThunderLevel = thunder;
        level.thunderLevel = thunder;
    }
}
