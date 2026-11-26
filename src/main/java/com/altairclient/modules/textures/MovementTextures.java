package com.altairclient.modules.textures;

import com.altairclient.client.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Movement Effects y Walk Effects: partículas al correr, saltar, aterrizar y caminar, huellas,
 * auras, líneas de velocidad y menos balanceo de cámara. Todo es solo visual y local.
 */
public final class MovementTextures {

    private static TextureModules.Entry e(String group, String name) {
        return TextureModules.byId(group + "/" + name);
    }

    // Movement Effects
    private static final TextureModules.Entry SPRINT = e(TextureModules.MOVEMENT, "sprint-particles");
    private static final TextureModules.Entry JUMP = e(TextureModules.MOVEMENT, "jump-particles");
    private static final TextureModules.Entry LANDING = e(TextureModules.MOVEMENT, "landing-particles");
    private static final TextureModules.Entry FOOTSTEP = e(TextureModules.MOVEMENT, "footstep-particles");
    private static final TextureModules.Entry SPEED_LINES = e(TextureModules.MOVEMENT, "small-speed-lines");
    private static final TextureModules.Entry NO_BOBBING = e(TextureModules.MOVEMENT, "reduced-camera-bobbing");
    private static final TextureModules.Entry WATER_SPLASH = e(TextureModules.MOVEMENT, "water-splash");
    private static final TextureModules.Entry SNOW_PRINTS = e(TextureModules.MOVEMENT, "snow-footprints");
    private static final TextureModules.Entry SAND_PRINTS = e(TextureModules.MOVEMENT, "sand-footprints");

    // Walk Effects
    private static final TextureModules.Entry WALK_DUST = e(TextureModules.WALK, "walk-dust");
    private static final TextureModules.Entry WALK_SMOKE = e(TextureModules.WALK, "walk-smoke");
    private static final TextureModules.Entry WALK_SPARKS = e(TextureModules.WALK, "walk-sparks");
    private static final TextureModules.Entry WALK_HEARTS = e(TextureModules.WALK, "walk-hearts");
    private static final TextureModules.Entry WALK_STARS = e(TextureModules.WALK, "walk-stars");
    private static final TextureModules.Entry WALK_SNOW = e(TextureModules.WALK, "walk-snow");
    private static final TextureModules.Entry WALK_LEAVES = e(TextureModules.WALK, "walk-leaves");
    private static final TextureModules.Entry WALK_BUBBLES = e(TextureModules.WALK, "walk-bubbles");
    private static final TextureModules.Entry WALK_PRINTS = e(TextureModules.WALK, "walk-footprints");
    private static final TextureModules.Entry WALK_CIRCLES = e(TextureModules.WALK, "walk-circles");
    private static final TextureModules.Entry WALK_SHADOW = e(TextureModules.WALK, "walk-shadow");
    private static final TextureModules.Entry WALK_AURA = e(TextureModules.WALK, "walk-aura");
    private static final TextureModules.Entry WALK_ALTAIR = e(TextureModules.WALK, "walk-altair");

    private static boolean registered = false;

    private static int tick = 0;
    private static boolean prevOnGround = true;
    private static boolean prevInWater = false;
    private static double fallPeak = 0.0;
    private static double stepDistance = 0.0;
    private static boolean stepLeft = false;
    private static double lastX = Double.NaN;
    private static double lastZ = Double.NaN;
    /** Valor original de "balanceo de cámara" para devolverlo al desactivar el efecto. */
    private static Boolean savedBobbing = null;

    private MovementTextures() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(MovementTextures::onTick);
        }
    }

    // ==================== Tick ====================

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        updateBobbing(mc);

        LocalPlayer p = mc.player;
        ClientLevel level = mc.level;
        if (p == null || level == null) {
            lastX = Double.NaN;
            return;
        }
        tick++;

        Vec3 v = p.getDeltaMovement();
        double hSpeed = Math.hypot(v.x, v.z);
        boolean ground = p.onGround();
        boolean inWater = p.isInWater();
        boolean moving = ground && hSpeed > 0.04;

        // Distancia recorrida a pie, para las pisadas
        if (!Double.isNaN(lastX) && ground) {
            stepDistance += Math.hypot(p.getX() - lastX, p.getZ() - lastZ);
        }
        lastX = p.getX();
        lastZ = p.getZ();

        if (!ground) {
            fallPeak = Math.max(fallPeak, p.fallDistance);
        }
        boolean jumped = prevOnGround && !ground && v.y > 0.1;
        boolean landed = !prevOnGround && ground;
        boolean splashed = !prevInWater && inWater;

        BlockState below = level.getBlockState(p.getOnPos());
        boolean solidBelow = !below.isAir();

        // ----- Movement Effects -----
        if (SPRINT.isEnabled() && p.isSprinting() && ground) {
            for (int n = 0; n < 3; n++) {
                spawn(level, ParticleTypes.POOF, p, 0.0, 0.08, rnd(0.08), 0.04, rnd(0.08));
            }
        }
        if (JUMP.isEnabled() && jumped) {
            ring(level, ParticleTypes.POOF, p, 0.45, 22, 0.07);
        }
        if (landed && fallPeak > 0.8) {
            if (LANDING.isEnabled()) {
                ring(level, ParticleTypes.POOF, p, 0.5, 28, 0.1);
            }
        }
        if (WATER_SPLASH.isEnabled() && splashed) {
            ring(level, ParticleTypes.SPLASH, p, 0.4, 28, 0.15);
        }

        // Pisadas: una cada 0,65 bloques, alternando pie izquierdo y derecho
        if (moving && stepDistance >= 0.65) {
            stepDistance = 0.0;
            stepLeft = !stepLeft;
            onStep(level, p, below);
        }

        // ----- Walk Effects -----
        if (moving) {
          for (int n = 0; n < 3; n++) {
            if (WALK_DUST.isEnabled() && solidBelow) {
                spawn(level, new BlockParticleOption(ParticleTypes.BLOCK, below), p, 0.0, 0.05, rnd(0.2), 0.03, rnd(0.2));
            }
            if (WALK_SMOKE.isEnabled()) {
                spawn(level, ParticleTypes.SMOKE, p, 0.0, 0.1, rnd(0.2), 0.01, rnd(0.2));
            }
            if (WALK_SPARKS.isEnabled()) {
                spawnColored(level, ParticleTypes.ELECTRIC_SPARK, WALK_SPARKS, p, 0.0, 0.1, rnd(0.2), 0.03, rnd(0.2));
            }
            if (WALK_HEARTS.isEnabled()) {
                spawn(level, ParticleTypes.HEART, p, 0.0, 0.9, rnd(0.25), 0.03, rnd(0.25));
            }
            if (WALK_STARS.isEnabled()) {
                spawnColored(level, ParticleTypes.END_ROD, WALK_STARS, p, 0.0, 0.1, rnd(0.25), 0.04, rnd(0.25));
            }
            if (WALK_SNOW.isEnabled()) {
                spawn(level, ParticleTypes.SNOWFLAKE, p, 0.0, 0.15, rnd(0.25), 0.02, rnd(0.25));
            }
            if (WALK_LEAVES.isEnabled()) {
                spawn(level, ParticleTypes.CHERRY_LEAVES, p, 0.0, 0.8, rnd(0.3), 0.0, rnd(0.3));
            }
            if (WALK_BUBBLES.isEnabled()) {
                spawn(level, ParticleTypes.BUBBLE_POP, p, 0.0, 0.1, rnd(0.2), 0.03, rnd(0.2));
            }
            if (WALK_ALTAIR.isEnabled()) {
                spawnColored(level, ParticleTypes.END_ROD, WALK_ALTAIR, p, 0.0, 0.1, rnd(0.3), 0.04, rnd(0.3));
            }
          }
        }
        if (WALK_PRINTS.isEnabled() && moving && stepDistance == 0.0) {
            printAt(level, p, 0x2A2A33);
        }
        // Círculo bajo los pies y sombra: se mantienen mientras estás en el suelo
        if (WALK_CIRCLES.isEnabled() && ground && tick % 2 == 0) {
            dustRing(level, p, Palette.color(WALK_CIRCLES.getColorIndex()), 0.55, 20, 0.9F);
        }
        if (WALK_SHADOW.isEnabled() && moving && tick % 2 == 0) {
            dustRing(level, p, 0x0C0C12, 0.25, 10, 1.3F);
        }
        if (WALK_AURA.isEnabled() && !mc.options.getCameraType().isFirstPerson()) {
            // Aura intensa: varias partículas grandes por tick, en dos anillos y subiendo
            ParticleOptions dust = new DustParticleOptions(dustColor(Palette.color(WALK_AURA.getColorIndex()) & 0xFFFFFF), 1.3F);
            for (int k = 0; k < 4; k++) {
                double a = ThreadLocalRandom.current().nextDouble() * Math.PI * 2.0;
                double r = 0.45 + ThreadLocalRandom.current().nextDouble() * 0.35;
                raw(level, dust, p.getX() + Math.cos(a) * r, p.getY() + rnd(0.0, 2.0), p.getZ() + Math.sin(a) * r, 0.0, 0.04, 0.0);
            }
        }

        prevOnGround = ground;
        prevInWater = inWater;
        if (landed) {
            fallPeak = 0.0;
        }
    }

    private static void onStep(ClientLevel level, LocalPlayer p, BlockState below) {
        if (FOOTSTEP.isEnabled() && !below.isAir()) {
            spawn(level, new BlockParticleOption(ParticleTypes.BLOCK, below), p, side(0.15), 0.03, 0.0, 0.03, 0.0);
        }
        boolean onSnow = below.is(Blocks.SNOW) || below.is(Blocks.SNOW_BLOCK) || below.is(Blocks.POWDER_SNOW);
        boolean onSand = below.is(BlockTags.SAND);
        if ((SNOW_PRINTS.isEnabled() && onSnow) || (SAND_PRINTS.isEnabled() && onSand)) {
            printAt(level, p, onSnow ? 0xB9C6D6 : 0x9C8A5A);
        }
    }

    private static void printAt(ClientLevel level, LocalPlayer p, int rgb) {
        double yaw = Math.toRadians(p.getYRot());
        double off = stepLeft ? 0.14 : -0.14;
        double x = p.getX() + Math.cos(yaw) * off;
        double z = p.getZ() + Math.sin(yaw) * off;
        raw(level, new DustParticleOptions(dustColor(rgb), 0.8F), x, p.getY() + 0.02, z, 0.0, 0.0, 0.0);
    }

    // ==================== Cámara: balanceo ====================

    private static void updateBobbing(Minecraft mc) {
        if (mc.options == null) {
            return;
        }
        if (NO_BOBBING.isEnabled() && mc.player != null) {
            if (savedBobbing == null) {
                savedBobbing = mc.options.bobView().get();
            }
            if (mc.options.bobView().get()) {
                mc.options.bobView().set(false);
            }
        } else if (savedBobbing != null) {
            mc.options.bobView().set(savedBobbing);
            savedBobbing = null;
        }
    }

    // ==================== HUD: líneas de velocidad ====================

    public static void renderSpeedLines(GuiGraphics graphics) {
        if (!SPEED_LINES.isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || !(p.isSprinting() || p.isFallFlying())) {
            return;
        }
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        int base = Palette.color(SPEED_LINES.getColorIndex());
        long frame = System.currentTimeMillis() / 70L;
        int band = Math.max(8, w / 7);
        for (int i = 0; i < 44; i++) {
            long seed = (frame * 31L + i * 7919L) * 2654435761L;
            int y = (int) Math.floorMod(seed >>> 7, (long) h);
            int len = 22 + (int) Math.floorMod(seed >>> 21, 44L);
            int inset = (int) Math.floorMod(seed >>> 33, (long) band);
            int alpha = 150 + (int) Math.floorMod(seed >>> 41, 100L);
            int color = (alpha << 24) | (base & 0x00FFFFFF);
            if ((i & 1) == 0) {
                graphics.fill(inset, y, inset + len, y + 2, color);
            } else {
                graphics.fill(w - inset - len, y, w - inset, y + 2, color);
            }
        }
    }

    // ==================== HUD: desenfoque de movimiento (aproximado) ====================

    private static final TextureModules.Entry MOTION_BLUR = TextureModules.byId(TextureModules.MOVEMENT + "/minimal-motion-blur");
    private static float blurYaw = 0.0F;
    private static float blurPitch = 0.0F;
    private static float blurX = 0.0F;
    private static float blurY = 0.0F;
    private static boolean blurInit = false;

    /**
     * Desenfoque ligero al girar la cámara. No es un desenfoque real de la imagen (eso necesita un shader):
     * se dibujan franjas translúcidas en los bordes que crecen con la velocidad del giro.
     */
    public static void renderMotionBlur(GuiGraphics graphics) {
        if (!MOTION_BLUR.isEnabled()) {
            blurInit = false;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }
        float yaw = p.getYRot();
        float pitch = p.getXRot();
        if (!blurInit) {
            blurYaw = yaw;
            blurPitch = pitch;
            blurInit = true;
            return;
        }
        float dy = yaw - blurYaw;
        while (dy > 180.0F) {
            dy -= 360.0F;
        }
        while (dy < -180.0F) {
            dy += 360.0F;
        }
        float dp = pitch - blurPitch;
        blurYaw = yaw;
        blurPitch = pitch;
        // Suavizado: sube rápido con el giro y baja poco a poco
        blurX += (Math.min(Math.abs(dy), 40.0F) - blurX) * 0.35F;
        blurY += (Math.min(Math.abs(dp), 40.0F) - blurY) * 0.35F;
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        if (blurX > 1.0F) {
            int width = Math.round(Math.min(blurX, 30.0F) * w / 70.0F);
            int steps = Math.max(1, width / 4);
            for (int i = 0; i < steps; i++) {
                int a = Math.round(120.0F * (1.0F - i / (float) steps) * Math.min(1.0F, blurX / 6.0F));
                int c = (a << 24) | 0xFFFFFF;
                graphics.fill(i * 4, 0, i * 4 + 4, h, c);
                graphics.fill(w - i * 4 - 4, 0, w - i * 4, h, c);
            }
        }
        if (blurY > 1.0F) {
            int height = Math.round(Math.min(blurY, 30.0F) * h / 70.0F);
            int steps = Math.max(1, height / 4);
            for (int i = 0; i < steps; i++) {
                int a = Math.round(120.0F * (1.0F - i / (float) steps) * Math.min(1.0F, blurY / 6.0F));
                int c = (a << 24) | 0xFFFFFF;
                graphics.fill(0, i * 4, w, i * 4 + 4, c);
                graphics.fill(0, h - i * 4 - 4, w, h - i * 4, c);
            }
        }
    }

    // ==================== Utilidades de partículas ====================

    private static double rnd(double range) {
        return (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0 * range;
    }

    private static double rnd(double min, double max) {
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    private static double side(double amount) {
        return stepLeft ? amount : -amount;
    }

    /** Partícula en los pies del jugador, con desplazamiento (ox, oz) y velocidad (dx, dy, dz). */
    private static void spawn(ClientLevel level, ParticleOptions options, LocalPlayer p,
                              double oy, double dy, double ox, double dx, double oz) {
        raw(level, options, p.getX() + ox, p.getY() + oy + 0.05, p.getZ() + oz, dx, dy, 0.0);
    }

    private static void spawn(ClientLevel level, ParticleOptions options, LocalPlayer p,
                              double ox, double dy, double dx, double dz) {
        raw(level, options, p.getX() + ox, p.getY() + 0.05, p.getZ(), dx, dy, dz);
    }

    private static void spawnColored(ClientLevel level, ParticleOptions options, TextureModules.Entry colorSource,
                                     LocalPlayer p, double oy, double dy, double ox, double dx, double oz) {
        PvpTextures.beginSelfSpawn(Palette.color(colorSource.getColorIndex()));
        try {
            spawn(level, options, p, oy, dy, ox, dx, oz);
        } finally {
            PvpTextures.endSelfSpawn();
        }
    }

    /** Anillo de partículas alrededor de los pies. */
    private static void ring(ClientLevel level, ParticleOptions options, LocalPlayer p, double radius, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double a = i * (Math.PI * 2.0 / count);
            raw(level, options, p.getX() + Math.cos(a) * radius * 0.5, p.getY() + 0.08, p.getZ() + Math.sin(a) * radius * 0.5,
                    Math.cos(a) * speed, 0.02, Math.sin(a) * speed);
        }
    }

    /** Anillo plano de polvo de color (no necesita teñido: el color va en la propia partícula). */
    private static void dustRing(ClientLevel level, LocalPlayer p, int argb, double radius, int count, float scale) {
        ParticleOptions dust = new DustParticleOptions(dustColor(argb & 0xFFFFFF), scale);
        for (int i = 0; i < count; i++) {
            double a = i * (Math.PI * 2.0 / count);
            raw(level, dust, p.getX() + Math.cos(a) * radius, p.getY() + 0.03, p.getZ() + Math.sin(a) * radius, 0.0, 0.0, 0.0);
        }
    }

    private static void raw(ClientLevel level, ParticleOptions options, double x, double y, double z,
                            double dx, double dy, double dz) {
        PvpTextures.markSelfSpawn(true);
        try {
            // Directo al motor de partículas: no pasa por los límites de distancia y de ajustes de ClientLevel.
            Minecraft.getInstance().particleEngine.createParticle(options, x, y, z, dx, dy, dz);
        } finally {
            PvpTextures.markSelfSpawn(false);
        }
    }

    private static org.joml.Vector3f dustColor(int rgb) {
        return new org.joml.Vector3f(((rgb >> 16) & 255) / 255.0F, ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F);
    }
}
