package com.altairclient.modules.textures;

import com.altairclient.client.Anim;
import com.altairclient.client.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.event.TickEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Altair Rain: cuando llueve, las gotas y las salpicaduras toman la paleta del cliente, caen más gotas
 * de colores a tu alrededor y brillan pequeñas chispas de luz de estrella al ras del suelo.
 * La lluvia de vanilla sigue ahí; esto se suma encima.
 */
public final class AltairRain {

    private static final TextureModules.Entry RAIN = TextureModules.byId(TextureModules.ID_ALTAIR_RAIN);
    private static boolean registered = false;

    private AltairRain() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AltairRain::onTick);
        }
    }

    /** true si hay que teñir con la paleta las gotas y salpicaduras de lluvia de vanilla. */
    public static boolean tintsVanilla(ParticleType<?> type) {
        return RAIN.isEnabled() && (type == ParticleTypes.RAIN || type == ParticleTypes.SPLASH);
    }

    public static int color() {
        return Palette.color(RAIN.getColorIndex());
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!RAIN.isEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer p = mc.player;
        if (level == null || p == null || mc.isPaused() || !level.isRaining()) {
            return;
        }
        // Solo si la lluvia te alcanza: bajo techo no cae nada
        if (!level.canSeeSky(BlockPos.containing(p.getX(), p.getEyeY(), p.getZ()))) {
            return;
        }
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int base = color();
        int light = Anim.mix(base, 0xFFFFFFFF, 0.55F);

        // Gotas de colores cayendo desde arriba
        for (int i = 0; i < 16; i++) {
            double ox = (r.nextDouble() - 0.5) * 20.0;
            double oz = (r.nextDouble() - 0.5) * 20.0;
            double oy = 5.0 + r.nextDouble() * 7.0;
            spawn(level, ParticleTypes.RAIN, r.nextBoolean() ? base : light,
                    p.getX() + ox, p.getY() + oy, p.getZ() + oz, 0.0, -0.25, 0.0);
        }

        // Chispas de estrella al ras del suelo
        if (r.nextInt(3) == 0) {
            double ox = (r.nextDouble() - 0.5) * 12.0;
            double oz = (r.nextDouble() - 0.5) * 12.0;
            spawn(level, ParticleTypes.END_ROD, light, p.getX() + ox, p.getY() + 0.1 + r.nextDouble() * 1.2,
                    p.getZ() + oz, 0.0, 0.01, 0.0);
        }
    }

    private static void spawn(ClientLevel level, ParticleOptions options, int argb,
                              double x, double y, double z, double dx, double dy, double dz) {
        PvpTextures.beginSelfSpawn(argb);
        PvpTextures.markSelfSpawn(true);
        try {
            level.addParticle(options, x, y, z, dx, dy, dz);
        } finally {
            PvpTextures.markSelfSpawn(false);
            PvpTextures.endSelfSpawn();
        }
    }
}
