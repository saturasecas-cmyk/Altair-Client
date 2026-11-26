package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Hacks folder: combat, movement and player modules. Everything is client side and
 * many servers ban these, so use them only where they are allowed (singleplayer, your own SMP).
 */
public final class Hacks {

    /** One hack: on/off plus two numeric settings (meaning depends on the module). */
    public static final class Mod {
        public boolean enabled;
        public int a;
        public int b;
        private final Runnable onDisable;

        Mod(int a, int b, Runnable onDisable) {
            this.a = a;
            this.b = b;
            this.onDisable = onDisable;
        }

        public void toggle() {
            enabled = !enabled;
            if (!enabled && onDisable != null) {
                try {
                    onDisable.run();
                } catch (RuntimeException ignored) {
                    // leaving the world while turning off: nothing to restore
                }
            }
        }
    }

    public static final String[] FLY_MODES = {"Vanilla", "Glide", "Creative"};

    // Combat
    public static final Mod AUTO_CRIT = new Mod(0, 0, null);
    public static final Mod VELOCITY = new Mod(0, 100, null);          // a = % horizontal kept, b = % vertical kept
    public static final Mod REACH = new Mod(5, 0, () -> setAttr(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE, 3.0));
    public static final Mod HITBOXES = new Mod(3, 0, null);            // a = extra size in tenths of a block
    public static final Mod AUTO_TOTEM = new Mod(0, 0, null);
    public static final Mod AUTO_ARMOR = new Mod(0, 0, null);
    public static final Mod AUTO_GAPPLE = new Mod(10, 0, null);        // a = health points at which to eat
    public static final Mod AIM_ASSIST = new Mod(4, 40, null);         // a = range, b = strength %
    public static final Mod CRYSTAL_AURA = new Mod(5, 4, null);        // a = range, b = interval in ticks
    // Movement
    public static final Mod FLY = new Mod(0, 3, () -> {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && !p.isCreative() && !p.isSpectator()) {
            p.getAbilities().mayfly = false;
            p.getAbilities().flying = false;
        }
    });                                                                 // a = mode, b = speed
    public static final Mod SPEED = new Mod(2, 0, null);               // a = strength 1-5
    public static final Mod SPRINT = new Mod(0, 0, null);
    public static final Mod STEP = new Mod(2, 0, () -> resetStep()); // a = blocks
    public static final Mod JESUS = new Mod(0, 0, null);
    public static final Mod SPIDER = new Mod(0, 0, null);
    public static final Mod ELYTRA_FLY = new Mod(4, 0, null);          // a = speed
    public static final Mod BOAT_FLY = new Mod(3, 0, null);            // a = speed
    public static final Mod PHASE = new Mod(0, 0, null);
    // Player
    public static final Mod NO_FALL = new Mod(0, 0, null);
    public static final Mod NO_SLOW = new Mod(0, 0, null);
    public static final Mod TIMER = new Mod(15, 0, () -> applyTimer(Minecraft.getInstance(), 1.0F));                                                                 // a = speed in tenths (15 = x1.5)
    public static final Mod BLINK = new Mod(0, 0, Hacks::flushBlink);

    private static final List<Packet<?>> BLINK_QUEUE = new ArrayList<>();
    private static boolean flushing = false;
    private static boolean registered = false;
    private static int tick = 0;
    private static int lastHurt = 0;
    private static int gappleSaved = -1;
    private static int gappleTicks = 0;

    private Hacks() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(Hacks::onTick);
    }

    // ==================== Hooks used by mixins and Kill Aura ====================

    /** Blink: returns true if the packet was held back (the caller must cancel sending it now). */
    public static boolean captureBlink(Packet<?> packet) {
        if (!BLINK.enabled || flushing || !(packet instanceof ServerboundMovePlayerPacket)) {
            return false;
        }
        synchronized (BLINK_QUEUE) {
            BLINK_QUEUE.add(packet);
        }
        return true;
    }

    private static void flushBlink() {
        Minecraft mc = Minecraft.getInstance();
        List<Packet<?>> copy;
        synchronized (BLINK_QUEUE) {
            copy = new ArrayList<>(BLINK_QUEUE);
            BLINK_QUEUE.clear();
        }
        if (mc.getConnection() == null) {
            return;
        }
        flushing = true;
        try {
            for (Packet<?> pk : copy) {
                mc.getConnection().send(pk);
            }
        } finally {
            flushing = false;
        }
    }

    public static boolean phaseActive(LocalPlayer p) {
        return PHASE.enabled && (p.horizontalCollision || !p.level().noCollision(p));
    }

    public static float hitboxExpansion() {
        return HITBOXES.enabled ? HITBOXES.a / 10.0F : 0.0F;
    }

    /** AutoCrit for Kill Aura: returns true when a hit now would be a critical. Jumps first if needed. */
    public static boolean critReady(LocalPlayer p) {
        if (!AUTO_CRIT.enabled) {
            return true;
        }
        if (p.onGround()) {
            Vec3 m = p.getDeltaMovement();
            p.setDeltaMovement(m.x, 0.42, m.z);
            return false;
        }
        return p.fallDistance > 0.0F && p.getDeltaMovement().y < 0.0 && !p.isInWater() && !p.onClimbable();
    }

    // ==================== Timer ====================

    private static java.lang.reflect.Field msPerTickField;
    private static Object timerObject;

    /** Speeds up (or restores) the client game clock. The game only ever slows down by itself, so both the
     *  tick rate and the timer's own milliseconds-per-tick have to change. */
    private static void applyTimer(Minecraft mc, float speed) {
        try {
            if (msPerTickField == null) {
                java.lang.reflect.Field d = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(Minecraft.class, "timer");
                timerObject = d.get(mc);
                msPerTickField = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(timerObject.getClass(), "msPerTick");
            }
            msPerTickField.setFloat(timerObject, 50.0F / speed);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // field not available: only the tick rate changes
        }
    }

    // ==================== Tick ====================

    private static void resetStep() {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null) {
            setAttr(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT, 0.6);
        }
    }

    private static void setAttr(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double value) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && p.getAttribute(attr) != null) {
            p.getAttribute(attr).setBaseValue(value);
        }
    }

    static boolean moving(Minecraft mc) {
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
    }

    /** Sets the horizontal velocity of an entity in the direction the player is steering. */
    static void steer(Minecraft mc, Entity e, double speed) {
        double f = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
        double s = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
        if (f == 0 && s == 0) {
            return;
        }
        double len = Math.sqrt(f * f + s * s);
        f /= len;
        s /= len;
        double yaw = Math.toRadians(mc.player.getYRot());
        double x = -Math.sin(yaw) * f + Math.cos(yaw) * s;
        double z = Math.cos(yaw) * f + Math.sin(yaw) * s;
        Vec3 m = e.getDeltaMovement();
        e.setDeltaMovement(x * speed, m.y, z * speed);
    }

    private static int menuSlot(int invIndex) {
        return invIndex < 9 ? 36 + invIndex : invIndex;
    }

    private static void click(Minecraft mc, LocalPlayer p, int menuSlot, int button, ClickType type) {
        mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId, menuSlot, button, type, p);
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null || !p.isAlive()) {
            return;
        }
        tick++;
        boolean noScreen = mc.screen == null;
        Vec3 dm = p.getDeltaMovement();

        // ---- Combat ----
        if (AUTO_CRIT.enabled && noScreen && mc.options.keyAttack.isDown()
                && mc.hitResult instanceof EntityHitResult && p.onGround()) {
            p.setDeltaMovement(dm.x, 0.42, dm.z);
        }
        if (VELOCITY.enabled) {
            if (p.hurtTime > lastHurt) {
                p.setDeltaMovement(dm.x * VELOCITY.a / 100.0, dm.y * VELOCITY.b / 100.0, dm.z * VELOCITY.a / 100.0);
            }
            lastHurt = p.hurtTime;
        }
        if (REACH.enabled) {
            setAttr(net.minecraft.world.entity.ai.attributes.Attributes.ENTITY_INTERACTION_RANGE, REACH.a);
        }
        if (AUTO_TOTEM.enabled && noScreen && tick % 2 == 0 && !p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
            for (int i = 0; i < 36; i++) {
                if (p.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                    click(mc, p, menuSlot(i), 40, ClickType.SWAP);
                    break;
                }
            }
        }
        if (AUTO_ARMOR.enabled && noScreen && tick % 5 == 0) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            outer:
            for (EquipmentSlot slot : slots) {
                if (!p.getItemBySlot(slot).isEmpty()) {
                    continue;
                }
                for (int i = 0; i < 36; i++) {
                    ItemStack st = p.getInventory().getItem(i);
                    if (!st.isEmpty() && p.getEquipmentSlotForItem(st) == slot) {
                        click(mc, p, menuSlot(i), 0, ClickType.QUICK_MOVE);
                        break outer;
                    }
                }
            }
        }
        autoGapple(mc, p, noScreen);
        if (AIM_ASSIST.enabled && noScreen && mc.options.keyAttack.isDown()) {
            LivingEntity t = KillAura.findNearest(mc, AIM_ASSIST.a, 2);
            if (t != null) {
                Vec3 eye = p.getEyePosition();
                Vec3 to = t.getBoundingBox().getCenter().subtract(eye);
                double flat = Math.sqrt(to.x * to.x + to.z * to.z);
                float yaw = (float) (Math.toDegrees(Math.atan2(to.z, to.x)) - 90.0);
                float pitch = (float) -Math.toDegrees(Math.atan2(to.y, flat));
                float dy = Mth.wrapDegrees(yaw - p.getYRot());
                float dp = pitch - p.getXRot();
                if (Math.abs(dy) < 70.0F) {
                    float k = AIM_ASSIST.b / 100.0F * 0.5F;
                    p.setYRot(p.getYRot() + dy * k);
                    p.setXRot(Mth.clamp(p.getXRot() + dp * k, -90.0F, 90.0F));
                }
            }
        }
        if (CRYSTAL_AURA.enabled && noScreen && tick % Math.max(1, CRYSTAL_AURA.b) == 0) {
            crystalAura(mc, p);
        }

        // ---- Movement ----
        if (FLY.enabled) {
            double sp = FLY.b * 0.08;
            if (FLY.a == 0) {
                double y = mc.options.keyJump.isDown() ? sp : mc.options.keyShift.isDown() ? -sp : 0.0;
                p.setDeltaMovement(0.0, y, 0.0);
                steer(mc, p, sp);
                p.resetFallDistance();
            } else if (FLY.a == 1) {
                if (dm.y < -0.04) {
                    p.setDeltaMovement(dm.x, -0.04, dm.z);
                }
                p.resetFallDistance();
            } else {
                p.getAbilities().mayfly = true;
                p.getAbilities().flying = true;
                p.getAbilities().setFlyingSpeed(FLY.b * 0.02F);
            }
        }
        if (SPEED.enabled && moving(mc) && !p.isInWater() && !p.isFallFlying() && !p.isPassenger()) {
            steer(mc, p, 0.2 * (1.0 + SPEED.a * 0.3));
        }
        if (SPRINT.enabled && mc.options.keyUp.isDown() && !p.horizontalCollision
                && !p.isUsingItem() && p.getFoodData().getFoodLevel() > 6) {
            p.setSprinting(true);
        }
        if (STEP.enabled) {
            setAttr(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT, STEP.a);
        }
        if (JESUS.enabled && !p.isShiftKeyDown() && !mc.options.keyJump.isDown()) {
            BlockPos feet = p.blockPosition();
            var fluid = mc.level.getFluidState(feet);
            if (fluid.is(net.minecraft.tags.FluidTags.WATER) && mc.level.getFluidState(feet.above()).isEmpty()) {
                double surface = feet.getY() + fluid.getHeight(mc.level, feet);
                if (p.getY() <= surface + 0.15) {
                    p.setPos(p.getX(), surface, p.getZ());
                    Vec3 m = p.getDeltaMovement();
                    p.setDeltaMovement(m.x, Math.max(0.0, m.y), m.z);
                    p.setOnGround(true);
                    p.resetFallDistance();
                }
            }
        }
        if (SPIDER.enabled && p.horizontalCollision && !p.isShiftKeyDown()) {
            Vec3 m = p.getDeltaMovement();
            p.setDeltaMovement(m.x, 0.2, m.z);
        }
        if (ELYTRA_FLY.enabled && p.isFallFlying()) {
            if (mc.options.keyShift.isDown()) {
                p.setDeltaMovement(0.0, 0.0, 0.0);
            } else if (mc.options.keyUp.isDown()) {
                p.setDeltaMovement(p.getLookAngle().scale(ELYTRA_FLY.a * 0.15));
            }
            p.resetFallDistance();
        }
        Entity veh = p.getVehicle();
        if (BOAT_FLY.enabled && veh != null && !(veh instanceof LivingEntity)) {
            double sp = BOAT_FLY.a * 0.1;
            double y = mc.options.keyJump.isDown() ? sp : mc.options.keyShift.isDown() ? -sp : 0.0;
            veh.setDeltaMovement(0.0, y, 0.0);
            steer(mc, veh, sp * 1.5);
        }

        // ---- Player ----
        if (NO_FALL.enabled && p.fallDistance > 2.0F && mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true));
            p.resetFallDistance();
        }
        if (NO_SLOW.enabled && p.isUsingItem() && !p.isPassenger() && moving(mc)) {
            steer(mc, p, 0.12);
        }
        if (TIMER.enabled) {
            applyTimer(mc, TIMER.a / 10.0F);
        }
    }

    private static void autoGapple(Minecraft mc, LocalPlayer p, boolean noScreen) {
        if (!AUTO_GAPPLE.enabled) {
            return;
        }
        if (gappleSaved >= 0) {
            gappleTicks++;
            boolean healthy = p.getHealth() > AUTO_GAPPLE.a;
            if ((gappleTicks > 4 && !p.isUsingItem()) || healthy || gappleTicks > 60) {
                mc.options.keyUse.setDown(false);
                p.getInventory().selected = gappleSaved;
                gappleSaved = -1;
            }
            return;
        }
        if (!noScreen || p.getHealth() > AUTO_GAPPLE.a) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (st.is(Items.GOLDEN_APPLE) || st.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                gappleSaved = p.getInventory().selected;
                gappleTicks = 0;
                p.getInventory().selected = i;
                mc.options.keyUse.setDown(true);
                return;
            }
        }
    }

    private static void crystalAura(Minecraft mc, LocalPlayer p) {
        double range = CRYSTAL_AURA.a;
        LivingEntity enemy = KillAura.findNearest(mc, 12, 2);
        if (enemy == null) {
            return;
        }
        // 1) break a crystal near the enemy that is not too close to us
        for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(range))) {
            if (e instanceof EndCrystal && e.distanceToSqr(enemy) < 49.0 && p.distanceToSqr(e) > 4.0
                    && p.distanceToSqr(e) <= range * range) {
                mc.gameMode.attack(p, e);
                p.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
        // 2) place a crystal on obsidian or bedrock close to the enemy
        if (!p.getMainHandItem().is(Items.END_CRYSTAL)) {
            boolean found = false;
            for (int i = 0; i < 9; i++) {
                if (p.getInventory().getItem(i).is(Items.END_CRYSTAL)) {
                    p.getInventory().selected = i;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return;
            }
        }
        BlockPos me = p.blockPosition();
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        int r = (int) Math.ceil(range);
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = me.offset(dx, dy, dz);
                    var st = mc.level.getBlockState(pos);
                    if (!st.is(Blocks.OBSIDIAN) && !st.is(Blocks.BEDROCK)) {
                        continue;
                    }
                    if (!mc.level.getBlockState(pos.above()).isAir() || !mc.level.getBlockState(pos.above(2)).isAir()) {
                        continue;
                    }
                    Vec3 c = Vec3.atCenterOf(pos.above());
                    if (p.position().distanceToSqr(c) < 9.0 || p.getEyePosition().distanceToSqr(c) > range * range) {
                        continue;
                    }
                    if (!mc.level.getEntities((Entity) null, new AABB(pos.above()), x -> true).isEmpty()) {
                        continue;
                    }
                    double d = enemy.position().distanceToSqr(c);
                    if (d < 64.0 && d < bestD) {
                        bestD = d;
                        best = pos;
                    }
                }
            }
        }
        if (best != null) {
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(best).add(0.0, 0.5, 0.0), Direction.UP, best, false);
            mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }
}
