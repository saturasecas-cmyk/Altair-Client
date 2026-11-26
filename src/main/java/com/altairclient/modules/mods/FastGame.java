package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Fast Game: aligera el juego sin cambiar cómo se juega.
 *  - No dibuja las entidades que no ves (tapadas por bloques) ni las muy lejanas.
 *  - Baja la distancia a la que se dibujan entidades, quita las sombras de entidades.
 * Al desactivarlo (o cerrar el juego) se devuelven tus ajustes originales.
 */
public final class FastGame {

    private static final double MIN_CULL_DISTANCE_SQ = 8.0D * 8.0D;
    private static final long CACHE_MS = 200L;

    private record Visibility(boolean visible, long time) {
    }

    private static final Map<Long, Visibility> CACHE = new HashMap<>();
    private static long lastCleanup = 0L;

    private static boolean enabled = false;
    private static boolean applied = false;
    private static boolean registered = false;

    private static Double savedDistance;
    private static Boolean savedShadows;

    private FastGame() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(FastGame::onRenderLiving);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            restore(Minecraft.getInstance());
            CACHE.clear();
        }
    }

    /** Se llama cada tick: aplica los ajustes una vez al activar. */
    public static void tick(Minecraft mc) {
        if (!enabled || applied || mc.options == null) {
            return;
        }
        savedDistance = mc.options.entityDistanceScaling().get();
        savedShadows = mc.options.entityShadows().get();
        applied = true;

        mc.options.entityDistanceScaling().set(0.5D);
        mc.options.entityShadows().set(false);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (applied) {
                    restore(mc);
                    mc.options.save();
                }
            } catch (Throwable ignored) {
            }
        }, "Altair-FastGameRestore"));
    }

    private static void restore(Minecraft mc) {
        if (applied && mc != null && mc.options != null) {
            mc.options.entityDistanceScaling().set(savedDistance);
            mc.options.entityShadows().set(savedShadows);
        }
        applied = false;
    }

    /** Cancela el dibujado de entidades tapadas por bloques. Devuelve true para cancelar. */
    private static boolean onRenderLiving(RenderLivingEvent.Pre<?, ?, ?> event) {
        if (!enabled) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        Entity viewer = mc.getCameraEntity();
        if (mc.level == null || viewer == null) {
            return false;
        }

        double x = event.getState().x;
        double y = event.getState().y;
        double z = event.getState().z;
        Vec3 eye = viewer.getEyePosition();

        double dx = x - eye.x;
        double dy = y - eye.y;
        double dz = z - eye.z;
        if (dx * dx + dy * dy + dz * dz < MIN_CULL_DISTANCE_SQ) {
            return false; // lo cercano siempre se dibuja
        }

        long now = System.currentTimeMillis();
        if (now - lastCleanup > 2000L) {
            lastCleanup = now;
            CACHE.clear();
        }

        long key = (((long) Math.floor(x * 2)) & 0x1FFFFFL) << 42
                | (((long) Math.floor(y * 2)) & 0x1FFFFFL) << 21
                | (((long) Math.floor(z * 2)) & 0x1FFFFFL);
        Visibility cached = CACHE.get(key);
        if (cached == null || now - cached.time() > CACHE_MS) {
            boolean visible = canSee(mc, viewer, eye, x, y, z);
            cached = new Visibility(visible, now);
            CACHE.put(key, cached);
        }
        return !cached.visible();
    }

    /** Hay línea de visión a los pies, al centro o a la cabeza de la entidad. */
    private static boolean canSee(Minecraft mc, Entity viewer, Vec3 eye, double x, double y, double z) {
        double[] heights = {1.0D, 1.8D, 0.2D};
        for (double h : heights) {
            Vec3 target = new Vec3(x, y + h, z);
            ClipContext context = new ClipContext(eye, target, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, viewer);
            if (mc.level.clip(context).getType() == HitResult.Type.MISS) {
                return true;
            }
        }
        return false;
    }
}
