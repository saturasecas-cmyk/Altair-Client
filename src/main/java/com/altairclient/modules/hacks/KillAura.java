package com.altairclient.modules.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Kill Aura: cada tick busca el objetivo valido mas cercano dentro del rango
 * y lo ataca respetando el cooldown del arma.
 *
 * Mismo patron que FPSDisplay: getName / isEnabled / toggle / setEnabled.
 * Llama a {@link #tick()} una vez por tick de cliente (ClientTickEvent.Post)
 * y a {@link #toggle()} desde la tecla o desde AltairScreen.
 */
public class KillAura {

    private boolean enabled = false;

    // ---- Configuracion ----
    private double range = 3.5;               // alcance en bloques
    private boolean targetPlayers = true;
    private boolean targetMobs = true;
    private boolean requireLineOfSight = true;
    private float minCooldown = 0.95F;        // 0..1, fuerza de ataque minima

    public String getName() {
        return "Kill Aura";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
          minecraft.player.displayClientMessage(
    Component.literal("Kill Aura: " + (enabled ? "ON" : "OFF")),
    true
);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // ---- Getters / setters de configuracion ----
    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = Math.max(1.0, Math.min(range, 6.0));
    }

    public boolean isTargetPlayers() {
        return targetPlayers;
    }

    public void setTargetPlayers(boolean targetPlayers) {
        this.targetPlayers = targetPlayers;
    }

    public boolean isTargetMobs() {
        return targetMobs;
    }

    public void setTargetMobs(boolean targetMobs) {
        this.targetMobs = targetMobs;
    }

    public boolean isRequireLineOfSight() {
        return requireLineOfSight;
    }

    public void setRequireLineOfSight(boolean requireLineOfSight) {
        this.requireLineOfSight = requireLineOfSight;
    }

    /** Se llama una vez por tick de cliente. */
    public void tick() {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || minecraft.level == null || minecraft.gameMode == null) {
            return;
        }
if (!player.isAlive() || minecraft.screen != null) {
            return;
        }

        // Espera a que el ataque este cargado (evita golpes debiles)
        if (player.getAttackStrengthScale(0.0F) < minCooldown) {
            return;
        }

        LivingEntity target = findTarget(minecraft, player);
        if (target == null) {
            return;
        }

        minecraft.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private LivingEntity findTarget(Minecraft minecraft, LocalPlayer self) {
        AABB box = self.getBoundingBox().inflate(range);
        List<Entity> nearby = minecraft.level.getEntities(self, box, e -> isValid(self, e));

        return nearby.stream()
            .map(e -> (LivingEntity) e)
            .filter(e -> self.distanceTo(e) <= range)
            .filter(e -> !requireLineOfSight || self.hasLineOfSight(e))
            .min(Comparator.comparingDouble(self::distanceToSqr))
            .orElse(null);
    }

    private boolean isValid(LocalPlayer self, Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return false;
        }
        if (entity == self || !living.isAlive() || living.isRemoved()) {
            return false;
        }
        if (living.isInvulnerable()) {
            return false;
        }
        if (entity instanceof Player) {
            return targetPlayers;
        }
        return targetMobs;
    }
}
