package com.altairclient.modules.mods;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.server.level.ParticleStatus;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Ultra Optimizer: baja lo que el juego tiene que dibujar y calcular.
 * Sombras de entidades y oclusión ambiental fuera, nubes simples (o sin nubes), menos partículas,
 * menos distancia de dibujado y de entidades, y no se dibujan las criaturas que están muy por debajo de ti.
 * Mientras está activo pone los ajustes de vídeo del juego; al apagarlo vuelven a los que tenías.
 * El juego ya descarta lo que queda fuera de pantalla; esto le quita además lo lejano y lo oculto bajo tus pies.
 */
public final class UltraOptimizer {

    public static final String[] LEVEL_NAMES = {"Balanced", "Aggressive", "Ultra"};

    private static final int[] RENDER_DISTANCE_CAP = {10, 7, 5};
    private static final double[] ENTITY_DISTANCE = {1.0, 0.75, 0.5};
    private static final int[] WEATHER_RADIUS = {5, 3, 2};
    /** Criaturas más abajo que esto (en bloques) respecto a tus ojos no se dibujan. */
    private static final double[] BELOW_CULL = {Double.MAX_VALUE, 24.0, 8.0};

    private static final int[] SIM_DISTANCE_CAP = {10, 8, 6};

    private static boolean enabled = false;
    private static boolean registered = false;
    private static int level = 1;

    /** Valores originales de los ajustes que hemos tocado. */
    private static final Map<String, Object> SAVED = new HashMap<>();

    private UltraOptimizer() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        TickEvent.ClientTickEvent.Post.BUS.addListener(UltraOptimizer::onTick);
        RenderLivingEvent.Pre.BUS.addListener((RenderLivingEvent.Pre event) -> shouldSkip(event));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.options != null && !SAVED.isEmpty()) {
                    applyAll(mc, false);
                    mc.options.save();
                }
            } catch (RuntimeException ignored) {
                // se cierra el juego, no hay nada más que hacer
            }
        }, "altair-optimizer-restore"));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        // También oculta lo que queda tapado por bloques (antes era el módulo Fast Game).
        if (FastGame.isEnabled() != enabled) {
            FastGame.toggle();
        }
    }

    public static int getLevel() {
        return level;
    }

    public static void setLevel(int v) {
        level = Math.max(0, Math.min(LEVEL_NAMES.length - 1, v));
    }

    // ==================== No dibujar criaturas muy por debajo ====================

    @SuppressWarnings("rawtypes")
    private static boolean shouldSkip(RenderLivingEvent.Pre event) {
        if (!enabled || level == 0) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.getState() == null) {
            return false;
        }
        double entityY = event.getState().y;
        return entityY < mc.player.getEyeY() - BELOW_CULL[level];
    }

    // ==================== Ajustes de vídeo ====================

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) {
            return;
        }
        if (enabled || !SAVED.isEmpty()) {
            applyAll(mc, enabled);
        }
    }

    private static void applyAll(Minecraft mc, boolean on) {
        var o = mc.options;
        boolean ultra = level == 2;
        apply("clouds", o.cloudStatus(), on, ultra ? CloudStatus.OFF : CloudStatus.FAST);
        apply("shadows", o.entityShadows(), on, false);
        apply("ao", o.ambientOcclusion(), on, false);
        apply("vignette", o.vignette(), on, false);
        apply("particles", o.particles(), on, level == 0 ? ParticleStatus.DECREASED : ParticleStatus.MINIMAL);
        apply("blend", o.biomeBlendRadius(), on, level == 0 ? 1 : 0);
        apply("transparency", o.improvedTransparency(), on, false);
        apply("weather", o.weatherRadius(), on, WEATHER_RADIUS[level]);
        apply("entityDist", o.entityDistanceScaling(), on, ENTITY_DISTANCE[level]);
        apply("vsync", o.enableVsync(), on, false);
        apply("fps", o.framerateLimit(), on, 260);
        capInt("simDist", o.simulationDistance(), on, SIM_DISTANCE_CAP[level]);

        // La distancia de dibujado solo se baja, nunca se sube por encima de la tuya
        OptionInstance<Integer> rd = o.renderDistance();
        if (on) {
            SAVED.putIfAbsent("renderDistance", rd.get());
            int cap = Math.min((Integer) SAVED.get("renderDistance"), RENDER_DISTANCE_CAP[level]);
            if (rd.get() != cap) {
                rd.set(cap);
            }
        } else if (SAVED.containsKey("renderDistance")) {
            rd.set((Integer) SAVED.remove("renderDistance"));
        }
    }

    private static void capInt(String key, OptionInstance<Integer> option, boolean on, int cap) {
        if (on) {
            SAVED.putIfAbsent(key, option.get());
            int target = Math.min((Integer) SAVED.get(key), cap);
            if (option.get() != target) {
                option.set(target);
            }
        } else if (SAVED.containsKey(key)) {
            option.set((Integer) SAVED.remove(key));
        }
    }

    /** Pone el ajuste al valor optimizado guardando el original, o lo restaura si ya no está activo. */
    @SuppressWarnings("unchecked")
    private static <T> void apply(String key, OptionInstance<T> option, boolean on, T value) {
        if (on) {
            SAVED.putIfAbsent(key, option.get());
            if (!option.get().equals(value)) {
                option.set(value);
            }
        } else if (SAVED.containsKey(key)) {
            option.set((T) SAVED.remove(key));
        }
    }
}
