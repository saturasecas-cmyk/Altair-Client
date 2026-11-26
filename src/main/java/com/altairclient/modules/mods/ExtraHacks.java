package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.HashSet;
import java.util.Set;

/** More hacks: extra combat, movement, player and world modules. Many servers ban these. */
public final class ExtraHacks {

    // ---- Combat ----
    public static final Hacks.Mod MACE_AURA = new Hacks.Mod(4, 1, null);        // a = range, b = auto jump (0/1)
    public static final Hacks.Mod ANCHOR_AURA = new Hacks.Mod(4, 6, null);      // a = range, b = interval
    public static final Hacks.Mod AUTO_SWORD = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AUTO_CLICKER = new Hacks.Mod(8, 0, null);     // a = clicks per second
    public static final Hacks.Mod TRIGGER_BOT = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod FAST_BOW = new Hacks.Mod(20, 0, null);        // a = ticks of draw before release
    public static final Hacks.Mod BOW_AIMBOT = new Hacks.Mod(60, 0, null);      // a = strength %
    public static final Hacks.Mod SHIELD_BREAKER = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AUTO_SHIELD = new Hacks.Mod(0, 0, () -> Minecraft.getInstance().options.keyUse.setDown(false));
    public static final Hacks.Mod AUTO_CONSUME = new Hacks.Mod(14, 0, null);    // a = food level to eat at
    public static final Hacks.Mod MULTI_AURA = new Hacks.Mod(4, 10, null);      // a = range, b = interval
    public static final Hacks.Mod AUTO_WEB = new Hacks.Mod(4, 0, null);         // a = range
    // ---- Movement ----
    public static final Hacks.Mod LONG_JUMP = new Hacks.Mod(2, 0, null);
    public static final Hacks.Mod HIGH_JUMP = new Hacks.Mod(2, 0, null);
    public static final Hacks.Mod BUNNY_HOP = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AIR_JUMP = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod SAFE_WALK = new Hacks.Mod(0, 0, () -> Minecraft.getInstance().options.keyShift.setDown(false));
    public static final Hacks.Mod PARKOUR = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AUTO_JUMP = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod FAST_LADDER = new Hacks.Mod(3, 0, null);
    public static final Hacks.Mod FAST_SWIM = new Hacks.Mod(3, 0, null);
    public static final Hacks.Mod AUTO_WALK = new Hacks.Mod(0, 0, () -> Minecraft.getInstance().options.keyUp.setDown(false));
    public static final Hacks.Mod SNEAK = new Hacks.Mod(0, 0, () -> Minecraft.getInstance().options.keyShift.setDown(false));
    public static final Hacks.Mod REVERSE_STEP = new Hacks.Mod(0, 0, null);
    // ---- Player ----
    public static final Hacks.Mod AUTO_RESPAWN = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod ANTI_AFK = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AUTO_TOOL = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod CHEST_STEALER = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod SPIN = new Hacks.Mod(4, 0, null);             // a = speed
    // ---- World ----
    public static final Hacks.Mod CHEST_AURA = new Hacks.Mod(4, 0, null);       // a = range
    public static final Hacks.Mod AUTO_TOWER = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AUTO_SHEAR = new Hacks.Mod(4, 0, null);       // a = range

    public static final String[] OFF_ON = {"Off", "On"};

    private static boolean registered = false;
    private static int tick = 0;
    private static int eatSaved = -1;
    private static int eatTicks = 0;
    private static boolean airJumpHeld = false;
    private static final Set<BlockPos> OPENED = new HashSet<>();
    private static int savedSlotShield = -1;

    private ExtraHacks() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ExtraHacks::onTick);
    }

    private static boolean has(LocalPlayer p, java.util.function.Predicate<ItemStack> pred) {
        for (int i = 0; i < 9; i++) {
            if (pred.test(p.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private static void attack(Minecraft mc, LocalPlayer p, Entity e) {
        mc.gameMode.attack(p, e);
        p.swing(InteractionHand.MAIN_HAND);
    }

    private static boolean edgeAhead(Minecraft mc, LocalPlayer p) {
        Vec3 m = p.getDeltaMovement();
        double hx = m.x * 4.0;
        double hz = m.z * 4.0;
        if (Math.abs(hx) + Math.abs(hz) < 0.05) {
            return false;
        }
        BlockPos ahead = BlockPos.containing(p.getX() + hx, p.getY() - 0.5, p.getZ() + hz);
        return p.onGround() && mc.level.getBlockState(ahead).getCollisionShape(mc.level, ahead).isEmpty();
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (AUTO_RESPAWN.enabled && !p.isAlive()) {
            p.respawn();
            return;
        }
        if (!p.isAlive()) {
            return;
        }
        tick++;
        boolean noScreen = mc.screen == null;
        Vec3 dm = p.getDeltaMovement();
        boolean jumpKey = mc.options.keyJump.isDown();

        // ===== Combat =====
        if (MACE_AURA.enabled && noScreen && tick % 3 == 0) {
            LivingEntity t = KillAura.findNearest(mc, MACE_AURA.a, 2);
            if (t != null && WorldHacks.selectItem(p, s -> s.is(Items.MACE))) {
                if (!p.onGround() && p.fallDistance > 1.5) {
                    attack(mc, p, t);
                } else if (p.onGround() && MACE_AURA.b == 1) {
                    p.setDeltaMovement(dm.x, 0.9, dm.z);
                }
            }
        }
        if (ANCHOR_AURA.enabled && noScreen && tick % Math.max(1, ANCHOR_AURA.b) == 0) {
            anchorAura(mc, p);
        }
        if (AUTO_SWORD.enabled && noScreen && (mc.options.keyAttack.isDown() || KillAura.isEnabled())
                && KillAura.findNearest(mc, 5, 2) != null) {
            int best = -1;
            int bestScore = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack st = p.getInventory().getItem(i);
                if (st.is(ItemTags.SWORDS) || st.is(ItemTags.AXES)) {
                    int score = st.getMaxDamage() + (st.is(ItemTags.SWORDS) ? 1 : 0);
                    if (score > bestScore) {
                        bestScore = score;
                        best = i;
                    }
                }
            }
            if (best >= 0 && best != p.getInventory().selected) {
                p.getInventory().selected = best;
            }
        }
        if (AUTO_CLICKER.enabled && noScreen && mc.options.keyAttack.isDown()
                && tick % Math.max(1, 20 / Math.max(1, AUTO_CLICKER.a)) == 0
                && mc.hitResult instanceof EntityHitResult ehr) {
            attack(mc, p, ehr.getEntity());
        }
        if (TRIGGER_BOT.enabled && noScreen && mc.hitResult instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof LivingEntity && p.getAttackStrengthScale(0.5F) >= 0.95F) {
            attack(mc, p, ehr.getEntity());
        }
        if (FAST_BOW.enabled && p.isUsingItem() && p.getUseItem().is(Items.BOW)
                && p.getTicksUsingItem() >= FAST_BOW.a) {
            mc.gameMode.releaseUsingItem(p);
        }
        if (BOW_AIMBOT.enabled && p.isUsingItem() && p.getUseItem().is(Items.BOW)) {
            bowAim(mc, p);
        }
        if (SHIELD_BREAKER.enabled && noScreen && (mc.options.keyAttack.isDown() || KillAura.isEnabled())) {
            LivingEntity t = KillAura.findNearest(mc, 5, 2);
            if (t != null && t.isBlocking()) {
                WorldHacks.selectItem(p, s -> s.is(ItemTags.AXES));
            }
        }
        if (AUTO_SHIELD.enabled && noScreen) {
            boolean enemy = KillAura.findNearest(mc, 4, 2) != null;
            boolean shield = p.getOffhandItem().is(Items.SHIELD) || p.getMainHandItem().is(Items.SHIELD);
            mc.options.keyUse.setDown(enemy && shield);
        }
        autoConsume(mc, p, noScreen);
        if (MULTI_AURA.enabled && noScreen && tick % Math.max(1, MULTI_AURA.b) == 0) {
            int n = 0;
            for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(MULTI_AURA.a), x -> x instanceof LivingEntity)) {
                if (n < 6 && e.isAlive() && p.distanceTo(e) <= MULTI_AURA.a && !e.isSpectator()) {
                    attack(mc, p, e);
                    n++;
                }
            }
        }
        if (AUTO_WEB.enabled && noScreen && tick % 6 == 0) {
            LivingEntity t = KillAura.findNearest(mc, AUTO_WEB.a, 2);
            if (t != null && mc.level.getBlockState(t.blockPosition()).isAir()
                    && WorldHacks.selectItem(p, s -> s.is(Items.COBWEB))) {
                WorldHacks.placeAt(mc, p, t.blockPosition());
            }
        }

        // ===== Movement =====
        if (LONG_JUMP.enabled && jumpKey && !p.onGround() && Hacks.moving(mc) && dm.y > 0.2) {
            Hacks.steer(mc, p, 0.25 * (1.0 + LONG_JUMP.a * 0.5));
        }
        if (HIGH_JUMP.enabled && jumpKey && p.onGround()) {
            p.setDeltaMovement(dm.x, 0.42 * (1.0 + HIGH_JUMP.a * 0.5), dm.z);
        }
        if (BUNNY_HOP.enabled && p.onGround() && Hacks.moving(mc) && !p.isShiftKeyDown()) {
            p.setDeltaMovement(dm.x, 0.42, dm.z);
            Hacks.steer(mc, p, 0.3);
        }
        if (AIR_JUMP.enabled) {
            if (jumpKey && !airJumpHeld && !p.onGround() && !p.isInWater()) {
                p.setDeltaMovement(dm.x, 0.42, dm.z);
                p.resetFallDistance();
            }
            airJumpHeld = jumpKey;
        }
        if (SAFE_WALK.enabled) {
            mc.options.keyShift.setDown(edgeAhead(mc, p));
        }
        if (PARKOUR.enabled && Hacks.moving(mc) && edgeAhead(mc, p)) {
            p.setDeltaMovement(dm.x, 0.42, dm.z);
        }
        if (AUTO_JUMP.enabled && p.onGround() && Hacks.moving(mc) && !p.isShiftKeyDown()) {
            p.setDeltaMovement(dm.x, 0.42, dm.z);
        }
        if (FAST_LADDER.enabled && p.onClimbable() && (mc.options.keyUp.isDown() || p.horizontalCollision)) {
            p.setDeltaMovement(dm.x, 0.1 * FAST_LADDER.a, dm.z);
        }
        if (FAST_SWIM.enabled && p.isInWater() && Hacks.moving(mc) && !p.isShiftKeyDown()) {
            Hacks.steer(mc, p, 0.1 * FAST_SWIM.a);
        }
        if (AUTO_WALK.enabled && noScreen) {
            mc.options.keyUp.setDown(true);
        }
        if (SNEAK.enabled && noScreen) {
            mc.options.keyShift.setDown(true);
        }
        if (REVERSE_STEP.enabled && !p.onGround() && dm.y < 0.0 && dm.y > -0.9 && !p.isInWater() && !p.isFallFlying()) {
            for (int i = 1; i <= 3; i++) {
                BlockPos below = p.blockPosition().below(i);
                if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                    p.setDeltaMovement(dm.x, -0.9, dm.z);
                    break;
                }
            }
        }

        // ===== Player =====
        if (ANTI_AFK.enabled && tick % 400 == 0 && p.onGround() && noScreen) {
            p.setDeltaMovement(dm.x, 0.42, dm.z);
            p.setYRot(p.getYRot() + 20.0F);
        }
        if (AUTO_TOOL.enabled && noScreen && mc.options.keyAttack.isDown() && mc.hitResult instanceof BlockHitResult bhr) {
            BlockState st = mc.level.getBlockState(bhr.getBlockPos());
            int best = p.getInventory().selected;
            float bestSpeed = p.getInventory().getItem(best).getDestroySpeed(st);
            for (int i = 0; i < 9; i++) {
                float sp = p.getInventory().getItem(i).getDestroySpeed(st);
                if (sp > bestSpeed + 0.01F) {
                    bestSpeed = sp;
                    best = i;
                }
            }
            if (best != p.getInventory().selected) {
                p.getInventory().selected = best;
            }
        }
        if (CHEST_STEALER.enabled && mc.screen instanceof ContainerScreen && tick % 2 == 0
                && p.containerMenu != p.inventoryMenu) {
            AbstractContainerMenu menu = p.containerMenu;
            int chestSlots = menu.slots.size() - 36;
            for (int i = 0; i < chestSlots; i++) {
                if (menu.slots.get(i).hasItem()) {
                    mc.gameMode.handleClickType(menu.containerId, i, 0, ClickType.QUICK_MOVE, p);
                    break;
                }
            }
        }
        if (SPIN.enabled) {
            p.setYRot(p.getYRot() + SPIN.a * 8.0F);
        }

        // ===== World =====
        if (CHEST_AURA.enabled && noScreen && tick % 10 == 0) {
            chestAura(mc, p);
        }
        if (AUTO_TOWER.enabled && noScreen && jumpKey && !p.onGround() && dm.y < 0.2
                && WorldHacks.selectItem(p, s -> s.getItem() instanceof net.minecraft.world.item.BlockItem)) {
            if (WorldHacks.placeAt(mc, p, p.blockPosition().below())) {
                p.setDeltaMovement(dm.x, 0.42, dm.z);
            }
        }
        if (AUTO_SHEAR.enabled && noScreen && tick % 10 == 0) {
            for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(AUTO_SHEAR.a), x -> x instanceof Shearable)) {
                if (e instanceof Shearable s && s.readyForShearing() && WorldHacks.selectItem(p, st -> st.is(Items.SHEARS))) {
                    mc.gameMode.interact(p, e, new EntityHitResult(e), InteractionHand.MAIN_HAND);
                    break;
                }
            }
        }
    }

    private static void bowAim(Minecraft mc, LocalPlayer p) {
        LivingEntity t = KillAura.findNearest(mc, 60, 2);
        if (t == null) {
            return;
        }
        float f = p.getTicksUsingItem() / 20.0F;
        f = Math.min(1.0F, (f * f + f * 2.0F) / 3.0F);
        double v = Math.max(0.5, f * 3.0);
        Vec3 eye = p.getEyePosition();
        Vec3 tp = t.getBoundingBox().getCenter();
        double dist = eye.distanceTo(tp);
        Vec3 lead = t.getDeltaMovement().scale(dist / v);
        Vec3 aim = tp.add(lead.x, 0.0, lead.z).subtract(eye);
        double x = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
        double y = aim.y;
        double g = 0.05;
        double disc = v * v * v * v - g * (g * x * x + 2.0 * y * v * v);
        float pitch;
        if (disc < 0.0) {
            pitch = (float) -Math.toDegrees(Math.atan2(y, x));
        } else {
            pitch = (float) -Math.toDegrees(Math.atan((v * v - Math.sqrt(disc)) / (g * x)));
        }
        float yaw = (float) (Math.toDegrees(Math.atan2(aim.z, aim.x)) - 90.0);
        float k = BOW_AIMBOT.a / 100.0F;
        p.setYRot(p.getYRot() + Mth.wrapDegrees(yaw - p.getYRot()) * k);
        p.setXRot(Mth.clamp(p.getXRot() + (pitch - p.getXRot()) * k, -90.0F, 90.0F));
    }

    private static void autoConsume(Minecraft mc, LocalPlayer p, boolean noScreen) {
        if (!AUTO_CONSUME.enabled) {
            return;
        }
        if (eatSaved >= 0) {
            eatTicks++;
            if ((eatTicks > 4 && !p.isUsingItem()) || p.getFoodData().getFoodLevel() >= 20 || eatTicks > 80) {
                mc.options.keyUse.setDown(false);
                p.getInventory().selected = eatSaved;
                eatSaved = -1;
            }
            return;
        }
        if (!noScreen || p.getFoodData().getFoodLevel() > AUTO_CONSUME.a) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack st = p.getInventory().getItem(i);
            if (!st.isEmpty() && st.isEdible() && !st.is(Items.ENCHANTED_GOLDEN_APPLE)
                    && !st.is(Items.GOLDEN_APPLE) && !st.is(Items.ROTTEN_FLESH)) {
                eatSaved = p.getInventory().selected;
                eatTicks = 0;
                p.getInventory().selected = i;
                mc.options.keyUse.setDown(true);
                return;
            }
        }
    }

    private static void anchorAura(Minecraft mc, LocalPlayer p) {
        if (mc.level.dimension() == Level.NETHER) {
            return; // anchors explode only outside the Nether
        }
        LivingEntity enemy = KillAura.findNearest(mc, ANCHOR_AURA.a + 3, 2);
        if (enemy == null) {
            return;
        }
        BlockPos base = enemy.blockPosition();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 3; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(pos);
                    if (!st.is(Blocks.RESPAWN_ANCHOR)
                            || p.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > (double) ANCHOR_AURA.a * ANCHOR_AURA.a
                            || p.position().distanceToSqr(Vec3.atCenterOf(pos)) < 9.0) {
                        continue;
                    }
                    BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                    if (st.getValue(RespawnAnchorBlock.CHARGE) == 0) {
                        if (WorldHacks.selectItem(p, s -> s.is(Items.GLOWSTONE))) {
                            mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                            p.swing(InteractionHand.MAIN_HAND);
                        }
                    } else if (WorldHacks.selectItem(p, s -> !s.is(Items.GLOWSTONE))) {
                        mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                        p.swing(InteractionHand.MAIN_HAND);
                    }
                    return;
                }
            }
        }
        if (WorldHacks.selectItem(p, s -> s.is(Items.RESPAWN_ANCHOR))) {
            for (Direction d : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
                BlockPos pos = base.relative(d, 2);
                if (p.position().distanceToSqr(Vec3.atCenterOf(pos)) >= 9.0 && WorldHacks.placeAt(mc, p, pos)) {
                    return;
                }
            }
        }
    }

    private static void chestAura(Minecraft mc, LocalPlayer p) {
        if (tick % 600 == 0) {
            OPENED.clear();
        }
        int r = ChestAuraRange();
        BlockPos me = p.blockPosition();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = me.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(pos);
                    if ((st.is(Blocks.CHEST) || st.is(Blocks.TRAPPED_CHEST) || st.is(Blocks.BARREL)
                            || st.is(net.minecraft.tags.BlockTags.SHULKER_BOXES))
                            && !OPENED.contains(pos)
                            && p.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) <= (double) r * r) {
                        OPENED.add(pos);
                        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                        mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                        p.swing(InteractionHand.MAIN_HAND);
                        return;
                    }
                }
            }
        }
    }

    private static int ChestAuraRange() {
        return CHEST_AURA.a;
    }
}
