package com.altairclient.modules.textures;

import com.altairclient.client.Anim;
import com.altairclient.client.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Efectos de las texturas PvP. Todo lo que se puede hacer con eventos de Forge está aquí;
 * los dos mixins (partículas y tótem) solo llaman a estos métodos.
 * Cada efecto lee su interruptor y su color del registro {@link TextureModules}.
 */
public final class PvpTextures {

    private static final TextureModules.Entry SHORT_SWORDS = TextureModules.byId(TextureModules.PVP + "/short-swords");
    private static final TextureModules.Entry LOW_FIRE = TextureModules.byId(TextureModules.PVP + "/low-fire");
    private static final TextureModules.Entry TRANSPARENT_FIRE = TextureModules.byId(TextureModules.PVP + "/transparent-fire");
    private static final TextureModules.Entry LOW_SHIELD = TextureModules.byId(TextureModules.PVP + "/low-shield");
    private static final TextureModules.Entry LOW_PARTICLES = TextureModules.byId(TextureModules.PVP + "/low-particles");
    private static final TextureModules.Entry CLEAR_LAVA = TextureModules.byId(TextureModules.PVP + "/clear-lava");
    private static final TextureModules.Entry HIT_PARTICLES = TextureModules.byId(TextureModules.PVP + "/simple-hit-particles");
    private static final TextureModules.Entry CRIT_PARTICLES = TextureModules.byId(TextureModules.PVP + "/critical-particles");
    private static final TextureModules.Entry MINIMAL_TOTEM = TextureModules.byId(TextureModules.PVP + "/minimal-totem");
    private static final TextureModules.Entry CLEAR_PUMPKIN = TextureModules.byId(TextureModules.PVP + "/transparent-pumpkin-overlay");
    private static final TextureModules.Entry MINIMAL_GLINT = TextureModules.byId(TextureModules.PVP + "/minimal-enchantment-glint");
    private static final TextureModules.Entry MIN_BREAK_PARTICLES = TextureModules.byId(TextureModules.BREAKING + "/minimal-breaking-particles");
    private static final TextureModules.Entry SMALL_DEBRIS = TextureModules.byId(TextureModules.BREAKING + "/small-block-debris");
    private static final TextureModules.Entry REDUCED_DUST = TextureModules.byId(TextureModules.BREAKING + "/reduced-block-dust");

    /** true mientras Altair crea sus propias partículas (Movement/Walk): no se filtran ni se tiñen como las de vanilla. */
    private static boolean selfSpawning = false;
    /** Color (ARGB) con el que teñir la próxima partícula propia; 0 = sin teñir. */
    private static int forcedColor = 0;

    private static boolean registered = false;
    /** Última vez que el fuego de vanilla quiso dibujarse en pantalla (para pintar el nuestro). */
    private static long fireSeenAt = 0L;

    private PvpTextures() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            RenderBlockScreenEffectEvent.BUS.addListener(PvpTextures::onScreenEffect);
        }
    }

    // ==================== Short Swords / Low Shield ====================

    /**
     * Lo llama ItemInHandRendererMixin justo antes de dibujar el objeto de la mano (primera persona):
     * encoge espadas y escudos en su propio origen, así que siempre se ve.
     */
    public static void transformHeldItem(ItemStack stack, boolean leftHand, com.mojang.blaze3d.vertex.PoseStack poseStack) {
        if (SHORT_SWORDS.isEnabled() && stack.is(ItemTags.SWORDS)) {
            poseStack.scale(0.7F, 0.7F, 0.7F);
        } else if (MINIMAL_TOTEM.isEnabled() && stack.is(Items.TOTEM_OF_UNDYING)) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        } else if (LOW_SHIELD.isEnabled() && stack.is(Items.SHIELD)) {
            poseStack.translate(leftHand ? -0.06F : 0.06F, -0.12F, 0.0F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
        }
    }

    // ==================== Fuego (Low Fire / Transparent Fire) ====================

    /** Cancela el fuego de vanilla en pantalla y apunta que hay que pintar el de Altair. */
    private static boolean onScreenEffect(RenderBlockScreenEffectEvent event) {
        if (com.altairclient.modules.mods.NoAnimation.isEnabled() && String.valueOf(event.getOverlayType()).contains("FIRE")) {
            return true; // No Animation: sin fuego en pantalla
        }
        if ((LOW_FIRE.isEnabled() || TRANSPARENT_FIRE.isEnabled())
                && String.valueOf(event.getOverlayType()).contains("FIRE")) {
            fireSeenAt = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /** Fuego bajo y/o semitransparente con la paleta del cliente, desde abajo de la pantalla. */
    public static void renderFire(GuiGraphicsExtractor graphics) {
        boolean low = LOW_FIRE.isEnabled();
        boolean transparent = TRANSPARENT_FIRE.isEnabled();
        if (!low && !transparent) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - fireSeenAt > 150L) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // Si están las dos activadas manda el color de Low Fire.
        int color = Palette.color(low ? LOW_FIRE.getColorIndex() : TRANSPARENT_FIRE.getColorIndex());
        float maxHeight = h * (low ? 0.11F : 0.30F);
        float alpha = transparent ? 0.38F : 0.85F;

        final int step = 5;
        final int bands = 4;
        for (int x = 0; x < w; x += step) {
            // Cada columna vibra distinto para que parezcan llamas y no una barra.
            double wave = 0.5 + 0.5 * Math.sin(x * 0.21 + now * 0.011) * Math.sin(x * 0.07 - now * 0.006);
            float colHeight = maxHeight * (float) (0.55 + 0.45 * wave);
            int bandH = Math.max(1, Math.round(colHeight / bands));
            for (int b = 0; b < bands; b++) {
                float fade = alpha * (1.0F - b / (float) bands);
                int bottom = h - b * bandH;
                graphics.fill(x, bottom - bandH, Math.min(w, x + step), bottom, Anim.scaleAlpha(color, fade));
            }
        }
    }

    // ==================== Lava (Clear Lava) ====================

    public static boolean isClearLava() {
        return CLEAR_LAVA.isEnabled();
    }

    // ==================== Calabaza (Transparent Pumpkin Overlay) ====================

    /** true si hay que ocultar las superposiciones de cámara (la máscara de la calabaza). */
    public static boolean hideCameraOverlay() {
        if (!CLEAR_PUMPKIN.isEnabled()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getItemBySlot(EquipmentSlot.HEAD).is(Items.CARVED_PUMPKIN);
    }

    // ==================== Partículas (llamado desde el mixin) ====================

    public static void markSelfSpawn(boolean value) {
        selfSpawning = value;
    }

    public static void beginSelfSpawn(int argb) {
        forcedColor = argb;
    }

    public static void endSelfSpawn() {
        forcedColor = 0;
    }

    /** true si esta partícula no debe crearse (menos partículas, golpes simples, críticos discretos, rotura). */
    public static boolean shouldDropParticle(ParticleOptions options) {
        if (selfSpawning) {
            return false;
        }
        ParticleType<?> type = options.getType();
        if (MIN_BREAK_PARTICLES.isEnabled() && type == ParticleTypes.BLOCK) {
            return ThreadLocalRandom.current().nextFloat() < 0.5F;
        }
        if (REDUCED_DUST.isEnabled()
                && (type == ParticleTypes.FALLING_DUST || type == ParticleTypes.BLOCK_CRUMBLE
                || type == ParticleTypes.DUST_PILLAR || type == ParticleTypes.DUST_PLUME)) {
            return ThreadLocalRandom.current().nextFloat() < 0.6F;
        }
        if (HIT_PARTICLES.isEnabled()
                && (type == ParticleTypes.DAMAGE_INDICATOR || type == ParticleTypes.SWEEP_ATTACK)) {
            return true;
        }
        if (CRIT_PARTICLES.isEnabled() && type == ParticleTypes.CRIT) {
            return ThreadLocalRandom.current().nextFloat() < 0.5F;
        }
        if (LOW_PARTICLES.isEnabled() && type != ParticleTypes.TOTEM_OF_UNDYING) {
            return ThreadLocalRandom.current().nextFloat() < 0.5F;
        }
        return false;
    }

    /** Partículas de rotura (TerrainParticle) creadas directamente por el juego: menos y más pequeñas. */
    public static boolean shouldDropBreakParticle(Particle particle) {
        if (selfSpawning || !(particle instanceof net.minecraft.client.particle.TerrainParticle)) {
            return false;
        }
        if (MIN_BREAK_PARTICLES.isEnabled() && ThreadLocalRandom.current().nextFloat() < 0.6F) {
            return true;
        }
        if (REDUCED_DUST.isEnabled() && ThreadLocalRandom.current().nextFloat() < 0.4F) {
            return true;
        }
        if (SMALL_DEBRIS.isEnabled()) {
            particle.scale(0.5F);
        }
        return false;
    }

    /** Tiñe la partícula recién creada con la paleta del cliente, si alguna textura lo pide. */
    public static void tintParticle(Particle particle, ParticleOptions options) {
        ParticleType<?> type = options.getType();
        if (SMALL_DEBRIS.isEnabled() && type == ParticleTypes.BLOCK && !selfSpawning) {
            particle.scale(0.5F);
        }
        if (!(particle instanceof SingleQuadParticle quad)) {
            return;
        }
        if (forcedColor != 0) {
            quad.setColor(((forcedColor >> 16) & 0xFF) / 255.0F, ((forcedColor >> 8) & 0xFF) / 255.0F, (forcedColor & 0xFF) / 255.0F);
            return;
        }
        if (selfSpawning) {
            return;
        }
        if (AltairRain.tintsVanilla(type)) {
            int rc = AltairRain.color();
            quad.setColor(((rc >> 16) & 0xFF) / 255.0F, ((rc >> 8) & 0xFF) / 255.0F, (rc & 0xFF) / 255.0F);
            return;
        }
        TextureModules.Entry source = null;
        if (MINIMAL_TOTEM.isEnabled() && type == ParticleTypes.TOTEM_OF_UNDYING) {
            source = MINIMAL_TOTEM;
        } else if (HIT_PARTICLES.isEnabled() && (type == ParticleTypes.CRIT || type == ParticleTypes.ENCHANTED_HIT)) {
            source = HIT_PARTICLES;
        } else if (LOW_PARTICLES.isEnabled()) {
            source = LOW_PARTICLES;
        }
        if (source == null) {
            return;
        }
        int c = Palette.color(source.getColorIndex());
        quad.setColor(((c >> 16) & 0xFF) / 255.0F, ((c >> 8) & 0xFF) / 255.0F, (c & 0xFF) / 255.0F);
    }

    // ==================== Brillo de encantamiento (llamado desde el mixin) ====================

    public static boolean minimalGlint() {
        return MINIMAL_GLINT.isEnabled();
    }

    // ==================== Tótem (llamado desde el mixin) ====================

    /** Escala de la animación del tótem al revivir: más pequeña si Minimal Totem está activado. */
    public static float totemScale() {
        return MINIMAL_TOTEM.isEnabled() ? 0.45F : 1.0F;
    }
}
