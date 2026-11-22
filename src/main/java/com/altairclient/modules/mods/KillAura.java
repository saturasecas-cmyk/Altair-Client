package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

import java.util.List;

/**
 * Kill Aura (Hacks > Combat): hits the closest valid target within range every few ticks.
 * Configurable: targets (mobs / players / both), range in blocks and the interval between hits.
 * Many servers ban this; only use it where it is allowed (singleplayer, your own SMP).
 */
public final class KillAura {

    public static final String[] TARGET_NAMES = {"Mobs", "Players", "Both"};
    public static final int MIN_RANGE = 2;
    public static final int MAX_RANGE = 6;
    public static final int MIN_INTERVAL = 1;
    public static final int MAX_INTERVAL = 20;

    private static boolean enabled = false;
    private static boolean registered = false;
    private static int targets = 2;
    private static int range = 4;
    private static int interval = 10;
    private static int cooldown = 0;

    private KillAura() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        TickEvent.ClientTickEvent.Post.BUS.addListener(KillAura::onTick);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        cooldown = 0;
    }

    public static int getTargets() {
        return targets;
    }

    public static void setTargets(int v) {
        targets = Math.max(0, Math.min(TARGET_NAMES.length - 1, v));
    }

    public static int getRange() {
        return range;
    }

    public static void setRange(int v) {
        range = Math.max(MIN_RANGE, Math.min(MAX_RANGE, v));
    }

    public static int getInterval() {
        return interval;
    }

    public static void setInterval(int v) {
        interval = Math.max(MIN_INTERVAL, Math.min(MAX_INTERVAL, v));
    }

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        if (!enabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.screen != null
                || !mc.player.isAlive() || mc.player.isSpectator()) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        LivingEntity target = findTarget(mc);
        if (target == null) {
            return;
        }
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
        cooldown = interval - 1;
    }

    private static LivingEntity findTarget(Minecraft mc) {
        Player me = mc.player;
        List<Entity> near = mc.level.getEntities(me, me.getBoundingBox().inflate(range));
        LivingEntity best = null;
        double bestDist = (double) range * range;
        for (Entity e : near) {
            if (!(e instanceof LivingEntity living) || !living.isAlive() || living.isSpectator()) {
                continue;
            }
            boolean isPlayer = living instanceof Player;
            boolean isMob = living instanceof Mob;
            boolean wanted = (targets != 1 && isMob) || (targets != 0 && isPlayer);
            if (!wanted) {
                continue;
            }
            double d = me.distanceToSqr(living);
            if (d <= bestDist) {
                bestDist = d;
                best = living;
            }
        }
        return best;
    }
}
