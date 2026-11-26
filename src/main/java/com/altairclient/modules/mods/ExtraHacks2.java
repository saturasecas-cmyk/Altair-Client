package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.lang.reflect.Field;

/** Third batch of hacks: Super Knockback, Target Strafe, Low Hop, Strafe, Anti Blind, Fast Eat, Auto Collect. */
public final class ExtraHacks2 {

    public static final Hacks.Mod SUPER_KNOCKBACK = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod TARGET_STRAFE = new Hacks.Mod(3, 0, null);   // a = radius
    public static final Hacks.Mod LOW_HOP = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod STRAFE = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod ANTI_BLIND = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod FAST_EAT = new Hacks.Mod(8, 0, null);        // a = extra ticks removed per tick
    public static final Hacks.Mod AUTO_COLLECT = new Hacks.Mod(8, 0, () -> Minecraft.getInstance().options.keyUp.setDown(false));

    private static boolean registered = false;
    private static boolean sprintBack = false;
    private static boolean collecting = false;
    private static Field useRemaining;
    private static boolean useLookedUp = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ExtraHacks2::onTick);
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null || !p.isAlive()) {
            return;
        }
        boolean noScreen = mc.screen == null;
        Vec3 dm = p.getDeltaMovement();

        if (sprintBack) {
            sprintBack = false;
            p.setSprinting(true);
        } else if (SUPER_KNOCKBACK.enabled && p.isSprinting() && (mc.options.keyAttack.isDown() || KillAura.isEnabled())
                && p.tickCount % 6 == 0) {
            LivingEntity t = KillAura.findNearest(mc, 4, 2);
            if (t != null) {
                p.setSprinting(false);
                sprintBack = true;
            }
        }

        if (TARGET_STRAFE.enabled && KillAura.isEnabled()) {
            LivingEntity t = KillAura.findNearest(mc, 6, 2);
            if (t != null) {
                double dx = t.getX() - p.getX();
                double dz = t.getZ() - p.getZ();
                double len = Math.max(0.01, Math.sqrt(dx * dx + dz * dz));
                double radial = Mth.clamp((len - TARGET_STRAFE.a) * 0.3, -0.2, 0.2);
                double vx = (-dz / len) * 0.26 + (dx / len) * radial;
                double vz = (dx / len) * 0.26 + (dz / len) * radial;
                p.setDeltaMovement(vx, dm.y, vz);
            }
        }

        if (LOW_HOP.enabled && p.onGround() && Hacks.moving(mc) && noScreen) {
            p.setDeltaMovement(dm.x, 0.24, dm.z);
        }

        if (STRAFE.enabled && !p.onGround() && Hacks.moving(mc) && noScreen) {
            double sp = Math.max(0.2, Math.sqrt(dm.x * dm.x + dm.z * dm.z));
            Hacks.steer(mc, p, sp);
        }

        if (ANTI_BLIND.enabled) {
            p.removeEffect(MobEffects.BLINDNESS);
            p.removeEffect(MobEffects.DARKNESS);
            p.removeEffect(MobEffects.NAUSEA);
        }

        if (FAST_EAT.enabled && p.isUsingItem() && p.getUseItem().isEdible()) {
            try {
                if (!useLookedUp) {
                    useLookedUp = true;
                    useRemaining = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(LivingEntity.class, "useItemRemaining");
                }
                if (useRemaining != null) {
                    int v = useRemaining.getInt(p);
                    useRemaining.setInt(p, Math.max(1, v - FAST_EAT.a));
                }
            } catch (Throwable ignored) {
                useRemaining = null;
            }
        }

        if (AUTO_COLLECT.enabled && noScreen) {
            ItemEntity best = null;
            double bd = AUTO_COLLECT.a * AUTO_COLLECT.a;
            for (ItemEntity ie : mc.level.getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(AUTO_COLLECT.a))) {
                double d = ie.distanceToSqr(p);
                if (d < bd && d > 0.6) {
                    bd = d;
                    best = ie;
                }
            }
            if (best != null) {
                float yaw = (float) (Math.toDegrees(Math.atan2(best.getZ() - p.getZ(), best.getX() - p.getX())) - 90.0);
                p.setYRot(yaw);
                mc.options.keyUp.setDown(true);
                if (best.getY() > p.getY() + 0.6 && p.onGround()) {
                    p.jumpFromGround();
                }
                collecting = true;
            } else if (collecting) {
                collecting = false;
                mc.options.keyUp.setDown(false);
            }
        }
    }
}
