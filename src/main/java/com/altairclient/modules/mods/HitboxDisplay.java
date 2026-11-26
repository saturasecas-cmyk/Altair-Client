package com.altairclient.modules.mods;

import com.altairclient.AltairClient;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Dibuja las cajas de colisión de las entidades en color morado Altair.
 *
 * En Minecraft 26.x las hitboxes de vanilla (F3 + B) se dibujan con el sistema de "gizmos" y con
 * colores fijos, así que aquí se emiten gizmos propios cada frame. Se respeta la profundidad (no se
 * ven a través de bloques), igual que las hitboxes de vanilla.
 */
public class HitboxDisplay {

    private static final HitboxDisplay INSTANCE = new HitboxDisplay();

    /** Morado del cliente (ARGB). */
    private static final int PURPLE = 0xFF9B7BFF;

    private static final double MAX_DISTANCE = 128.0D;
    private static final double MAX_DISTANCE_SQR = MAX_DISTANCE * MAX_DISTANCE;

    private boolean enabled = false;

    private HitboxDisplay() {
    }

    public static HitboxDisplay getInstance() {
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

    public static void register() {
        // Forge 1.21.3+ no tiene RenderLevelStageEvent: los hitboxes no estan portados todavia.
    }
}
