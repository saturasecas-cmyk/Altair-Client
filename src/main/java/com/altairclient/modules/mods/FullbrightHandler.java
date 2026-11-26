package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Fullbright: visión nocturna visual solo en tu cliente (un efecto local sin partículas ni icono).
 * No envia nada al servidor; si el servidor te da visión nocturna de verdad, no se toca.
 */
public final class FullbrightHandler {

    private static boolean enabled = false;
    private static boolean added = false;

    private FullbrightHandler() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            restore(Minecraft.getInstance());
        }
    }

    /** Se llama cada tick: mantiene la visión nocturna local mientras esté activo. */
    public static void tick(Minecraft mc) {
        if (!enabled || mc.player == null) {
            return;
        }
        MobEffectInstance current = mc.player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || (added && current.getDuration() < 400)) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 100000, 0, false, false, false));
            added = true;
        }
    }

    private static void restore(Minecraft mc) {
        if (added && mc != null && mc.player != null) {
            mc.player.removeEffectNoUpdate(MobEffects.NIGHT_VISION);
        }
        added = false;
    }
}
