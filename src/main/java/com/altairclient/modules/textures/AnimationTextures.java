package com.altairclient.modules.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;

/**
 * Movement Animations: movimientos suaves de cámara y de mano en primera persona.
 * Son aproximaciones visuales (balanceos y desplazamientos pequeños), no cambian el modelo del jugador.
 */
public final class AnimationTextures {

    private static TextureModules.Entry e(String name) {
        return TextureModules.byId(TextureModules.ANIMATIONS + "/" + name);
    }

    private static final TextureModules.Entry WALKING = e("smooth-walking");
    private static final TextureModules.Entry SPRINTING = e("smooth-sprinting");
    private static final TextureModules.Entry SNEAKING = e("smooth-sneaking");
    private static final TextureModules.Entry JUMPING = e("smooth-jumping");
    private static final TextureModules.Entry FALLING = e("smooth-falling");
    private static final TextureModules.Entry LANDING = e("smooth-landing");
    private static final TextureModules.Entry SWIMMING = e("smooth-swimming");
    private static final TextureModules.Entry CLIMBING = e("smooth-climbing");
    private static final TextureModules.Entry ELYTRA = e("smooth-elytra");
    private static final TextureModules.Entry IDLE = e("idle-animation");
    private static final TextureModules.Entry HAND_SWING = e("hand-swing");
    private static final TextureModules.Entry ITEM_SWING = e("item-swing");
    private static final TextureModules.Entry EATING = e("eating-animation");
    private static final TextureModules.Entry BOW = e("bow-animation");
    private static final TextureModules.Entry ROD = e("rod-animation");
    private static final TextureModules.Entry BLOCK_PLACE = e("block-animation");
    private static final TextureModules.Entry SNEAK_CAMERA = e("sneak-camera");
    private static final TextureModules.Entry SPRINT_CAMERA = e("sprint-camera");

    private static boolean registered = false;

    // Estado suavizado (se actualiza cada frame de cámara)
    private static float sneakAmount = 0.0F;
    private static float sprintAmount = 0.0F;
    private static float elytraRoll = 0.0F;
    private static float landKick = 0.0F;

    // Estado por tick
    private static boolean prevOnGround = true;
    private static double fallPeak = 0.0;
    private static float prevYaw = 0.0F;
    private static float yawRate = 0.0F;

    private AnimationTextures() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            TickEvent.ClientTickEvent.Post.BUS.addListener(AnimationTextures::onTick);
            ViewportEvent.ComputeCameraAngles.BUS.addListener(AnimationTextures::onCamera);
            RenderHandEvent.BUS.addListener(AnimationTextures::onHand);
        }
    }

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) {
            return;
        }
        boolean ground = p.onGround();
        if (!ground) {
            fallPeak = Math.max(fallPeak, p.fallDistance);
        } else if (!prevOnGround) {
            if (LANDING.isEnabled() && fallPeak > 0.8) {
                landKick = (float) Math.min(fallPeak, 8.0) * 0.45F;
            }
            fallPeak = 0.0;
        }
        prevOnGround = ground;

        float yaw = p.getYRot();
        float delta = yaw - prevYaw;
        while (delta > 180.0F) {
            delta -= 360.0F;
        }
        while (delta < -180.0F) {
            delta += 360.0F;
        }
        yawRate = delta;
        prevYaw = yaw;
    }

    private static float time() {
        return (System.currentTimeMillis() % 1_000_000L) / 1000.0F;
    }

    private static void onCamera(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }
        float t = time();
        Vec3 v = p.getDeltaMovement();
        double hSpeed = Math.hypot(v.x, v.z);
        boolean ground = p.onGround();
        boolean moving = ground && hSpeed > 0.03;

        float roll = 0.0F;
        float pitch = 0.0F;

        if (WALKING.isEnabled() && moving && !p.isSprinting() && !p.isCrouching()) {
            roll += (float) Math.sin(t * 7.0F) * 0.35F;
            pitch += (float) Math.cos(t * 14.0F) * 0.10F;
        }
        if (SPRINTING.isEnabled() && moving && p.isSprinting()) {
            roll += (float) Math.sin(t * 9.0F) * 0.6F;
            pitch += (float) Math.cos(t * 18.0F) * 0.18F;
        }

        // Agacharse: la cámara baja y vuelve de forma suave en vez de a golpe
        sneakAmount += ((p.isCrouching() ? 1.0F : 0.0F) - sneakAmount) * 0.15F;
        if (SNEAKING.isEnabled()) {
            pitch += sneakAmount * 0.9F;
        }
        if (SNEAK_CAMERA.isEnabled()) {
            roll += sneakAmount * (float) Math.sin(t * 2.2F) * 0.25F;
        }

        // Correr: la cámara se inclina un poco hacia delante
        sprintAmount += ((p.isSprinting() ? 1.0F : 0.0F) - sprintAmount) * 0.12F;
        if (SPRINT_CAMERA.isEnabled()) {
            pitch += sprintAmount * 0.7F;
            roll += sprintAmount * (float) Math.sin(t * 1.7F) * 0.2F;
        }

        if (JUMPING.isEnabled() && !ground && v.y > 0.0) {
            pitch -= (float) Math.min(v.y, 0.6) * 2.0F;
        }
        if (FALLING.isEnabled() && !ground && v.y < -0.3) {
            pitch += (float) Math.min(-v.y - 0.3, 1.2) * 0.9F;
        }
        if (LANDING.isEnabled()) {
            pitch += landKick;
            landKick *= 0.88F;
            if (landKick < 0.01F) {
                landKick = 0.0F;
            }
        }
        if (SWIMMING.isEnabled() && p.isSwimming()) {
            roll += (float) Math.sin(t * 3.0F) * 1.2F;
            pitch += (float) Math.cos(t * 3.0F) * 0.4F;
        }
        if (CLIMBING.isEnabled() && p.onClimbable() && Math.abs(v.y) > 0.01) {
            pitch += (float) Math.sin(t * 8.0F) * 0.35F;
        }

        // Élitros: la cámara se inclina hacia donde giras
        float targetRoll = p.isFallFlying() ? Math.max(-25.0F, Math.min(25.0F, yawRate * 0.8F)) : 0.0F;
        elytraRoll += (targetRoll - elytraRoll) * 0.2F;
        if (ELYTRA.isEnabled()) {
            roll += elytraRoll;
        }

        if (IDLE.isEnabled() && !moving && ground) {
            pitch += (float) Math.sin(t * 1.6F) * 0.10F;
            roll += (float) Math.sin(t * 0.9F) * 0.05F;
        }

        if (roll != 0.0F || pitch != 0.0F) {
            event.setRoll(event.getRoll() + roll);
            event.setPitch(event.getPitch() + pitch);
        }
    }

    /** Pequeños desplazamientos de la mano y del objeto; no cancela el evento. */
    private static boolean onHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) {
            return false;
        }
        ItemStack stack = event.getItemStack();
        float side = event.getHand() == InteractionHand.MAIN_HAND ? 1.0F : -1.0F;
        float swing = event.getSwingProgress();
        float s = (float) Math.sin(swing * Math.PI);
        float t = time();
        var pose = event.getPoseStack();

        if (swing > 0.0F) {
            if (HAND_SWING.isEnabled() && stack.isEmpty()) {
                pose.translate(0.06F * s * side, -0.05F * s, -0.10F * s);
            }
            if (ITEM_SWING.isEnabled() && !stack.isEmpty()) {
                pose.translate(-0.08F * s * side, -0.04F * s, 0.06F * s);
            }
            if (ROD.isEnabled() && stack.is(Items.FISHING_ROD)) {
                pose.translate(0.0F, -0.05F * s, 0.12F * s);
            }
            if (BLOCK_PLACE.isEnabled() && stack.getItem() instanceof BlockItem) {
                pose.translate(0.0F, -0.06F * s, 0.08F * s);
            }
        }

        boolean usingThis = p.isUsingItem() && p.getUsedItemHand() == event.getHand();
        if (usingThis) {
            if (BOW.isEnabled() && stack.is(Items.BOW)) {
                float pull = Math.min(p.getTicksUsingItem() / 20.0F, 1.0F);
                pose.translate(0.0F, 0.0F, 0.07F * pull);
            } else if (EATING.isEnabled() && !stack.is(Items.SHIELD) && !stack.is(Items.BOW) && !stack.is(Items.CROSSBOW)) {
                pose.translate(0.0F, (float) Math.sin(p.getTicksUsingItem() * 0.9F) * 0.012F, 0.0F);
            }
        }

        if (IDLE.isEnabled() && swing <= 0.0F && !usingThis) {
            pose.translate(0.0F, (float) Math.sin(t * 1.5F) * 0.004F, 0.0F);
        }
        return false;
    }
}
