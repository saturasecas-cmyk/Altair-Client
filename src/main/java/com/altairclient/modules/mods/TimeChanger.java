package com.altairclient.modules.mods;

import net.minecraft.client.ClientClockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockNetworkState;
import net.minecraft.world.clock.WorldClock;
import net.minecraftforge.event.TickEvent;

import java.util.Map;
import java.util.Optional;

/**
 * Time Changer: fija la hora del día solo en tu cliente. No cambia la hora del servidor
 * ni afecta a nadie más; al desactivarlo, el servidor vuelve a mandar la hora real.
 */
public final class TimeChanger {

    public static final String[] MODE_NAMES = {"Amanecer", "Día", "Atardecer", "Noche"};
    private static final long[] TICKS = {23000L, 6000L, 12500L, 18000L};
    private static final long DAY = 24000L;

    private static boolean enabled = false;
    private static boolean registered = false;
    private static int mode = 1;

    private TimeChanger() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            TickEvent.ClientTickEvent.Post.BUS.addListener(TimeChanger::onTick);
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
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Optional<Holder<WorldClock>> clock = level.dimensionType().defaultClock();
        if (clock.isEmpty()) {
            return; // dimensión con hora fija (Nether, End...)
        }
        ClientClockManager manager = level.clockManager();
        long total = manager.getTotalTicks(clock.get());
        long target = total - Math.floorMod(total, DAY) + TICKS[mode];
        manager.handleUpdates(level.getGameTime(), Map.of(clock.get(), new ClockNetworkState(target, 0.0F, 0.0F)));
    }
}
