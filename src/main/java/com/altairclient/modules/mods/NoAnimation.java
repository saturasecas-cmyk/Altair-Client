package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraftforge.event.TickEvent;

/**
 * No Animation: reduce las animaciones del juego.
 * - Fuego en pantalla y animación del tótem: se cancelan (PvpTextures y ScreenEffectRendererMixin lo consultan).
 * - Ajustes de accesibilidad del propio juego puestos a 0 mientras está activo: inclinación al recibir daño,
 *   distorsión de pantalla (náusea, portal), cambios de FOV y animación del brillo de encantamiento.
 *   Al desactivarlo vuelven los valores que tenías.
 */
public final class NoAnimation {

    private static boolean enabled = false;
    private static boolean registered = false;

    private static Double savedTilt = null;
    private static Double savedScreenEffect = null;
    private static Double savedFov = null;
    private static Double savedGlint = null;

    private NoAnimation() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            TickEvent.ClientTickEvent.Post.BUS.addListener(NoAnimation::onTick);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        savedTilt = apply(mc.options.damageTiltStrength(), savedTilt);
        savedScreenEffect = apply(mc.options.screenEffectScale(), savedScreenEffect);
        savedFov = apply(mc.options.fovEffectScale(), savedFov);
        savedGlint = apply(mc.options.glintSpeed(), savedGlint);
    }

    /** Pone la opción a 0 guardando su valor, o lo restaura si ya no está activo. Devuelve el valor guardado. */
    private static Double apply(OptionInstance<Double> option, Double saved) {
        if (enabled) {
            if (saved == null) {
                saved = option.get();
            }
            if (option.get() != 0.0) {
                option.set(0.0);
            }
            return saved;
        }
        if (saved != null) {
            option.set(saved);
        }
        return null;
    }
}
