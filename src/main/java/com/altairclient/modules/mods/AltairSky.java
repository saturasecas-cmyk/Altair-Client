package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ViewportEvent;

/**
 * Altair Sky: cielo azul marino con horizonte morado y muchas estrellas, de día y de noche.
 * El color del cielo y las estrellas los cambia {@code SkyRendererMixin}; aquí está el estado,
 * el color del horizonte (niebla) y las constantes.
 */
public final class AltairSky {

    /** Azul marino del cielo (ARGB). */
    public static final int ZENITH = 0xFF060B2C;
    /** Morado del horizonte (niebla), en 0-255. */
    private static final int HORIZON_R = 0x2E;
    private static final int HORIZON_G = 0x16;
    private static final int HORIZON_B = 0x5E;

    public static final String[] DENSITY_NAMES = {"Normal", "Dense", "Very dense"};
    /** Capas extra de estrellas dibujadas encima de las de vanilla, por nivel de densidad. */
    private static final int[] EXTRA_LAYERS = {0, 2, 4};

    private static boolean enabled = false;
    private static boolean registered = false;
    private static int density = 1;

    private AltairSky() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            ViewportEvent.ComputeFogColor.BUS.addListener(AltairSky::onFogColor);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static int getDensity() {
        return density;
    }

    public static void setDensity(int v) {
        density = Math.max(0, Math.min(DENSITY_NAMES.length - 1, v));
    }

    public static int extraStarLayers() {
        return EXTRA_LAYERS[density];
    }

    /** Horizonte morado en el Overworld, salvo bajo el agua o en lava. */
    private static void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!enabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.level.dimension() != Level.OVERWORLD
                || mc.player.isUnderWater() || mc.player.isInLava()) {
            return;
        }
        event.setRed(HORIZON_R / 255.0F);
        event.setGreen(HORIZON_G / 255.0F);
        event.setBlue(HORIZON_B / 255.0F);
    }
}
