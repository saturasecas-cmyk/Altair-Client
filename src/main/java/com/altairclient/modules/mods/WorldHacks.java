package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Hacks > World: Nuker, Scaffold, FastPlace, FastBreak, AutoMine, AutoFarm, AutoFish,
 * Surround, AutoTrap and BedAura. Many servers ban these; use them only where allowed.
 */
public final class WorldHacks {

    public static final Hacks.Mod NUKER = new Hacks.Mod(4, 1, null);        // a = range, b = blocks per tick (creative)
    public static final Hacks.Mod SCAFFOLD = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod FAST_PLACE = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod FAST_BREAK = new Hacks.Mod(5, 0, null);   // a = extra progress per tick (x0.02)
    public static final Hacks.Mod AUTO_MINE = new Hacks.Mod(0, 0, () -> Minecraft.getInstance().options.keyAttack.setDown(false));
    public static final Hacks.Mod AUTO_FARM = new Hacks.Mod(4, 0, null);    // a = range
    public static final Hacks.Mod AUTO_FISH = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod SURROUND = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod AUTO_TRAP = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod BED_AURA = new Hacks.Mod(4, 6, null);     // a = range, b = interval in ticks

    private static boolean registered = false;
    private static int tick = 0;
    private static BlockPos nukerPos = null;
    private static BlockPos farmPos = null;
    private static int hookAge = 0;
    private static int recast = 0;
    private static Field rightClickDelay;
    private static Field destroyDelay;
    private static Field destroyProgress;

    private WorldHacks() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(WorldHacks::onTick);
    }

    // ==================== Helpers ====================

    static boolean selectItem(LocalPlayer p, Predicate<ItemStack> wanted) {
        if (wanted.test(p.getMainHandItem())) {
            return true;
        }
        for (int i = 0; i < 9; i++) {
            if (wanted.test(p.getInventory().getItem(i))) {
                p.getInventory().selected = i;
                return true;
            }
        }
        return false;
    }

    /** Places the held block at pos by clicking a solid neighbour. Returns true if a click was sent. */
    static boolean placeAt(Minecraft mc, LocalPlayer p, BlockPos pos) {
        if (!mc.level.getBlockState(pos).canBeReplaced()) {
            return false;
        }
        for (Direction d : Direction.values()) {
            BlockPos n = pos.relative(d);
            BlockState ns = mc.level.getBlockState(n);
            if (!ns.isAir() && !ns.canBeReplaced()) {
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(n).relative(d.getOpposite(), 0.5),
                        d.getOpposite(), n, false);
                mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                p.swing(InteractionHand.MAIN_HAND);
                return true;
            }
        }
        return false;
    }

    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private static boolean breakBlock(Minecraft mc, LocalPlayer p, BlockPos pos, BlockPos current) {
        if (!pos.equals(current)) {
            mc.gameMode.startDestroyBlock(pos, Direction.UP);
        } else {
            mc.gameMode.continueDestroyBlock(pos, Direction.UP);
        }
        p.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    private static Object getField(Field f, Object target) {
        try {
            return f.get(target);
        } catch (IllegalAccessException | RuntimeException e) {
            return null;
        }
    }

    private static Field find(Class<?> cls, String name) {
        try {
            Field f = cls.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException | RuntimeException e) {
            return null;
        }
    }

    // ==================== Tick ====================

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

        if (FAST_PLACE.enabled) {
            if (rightClickDelay == null) {
                rightClickDelay = find(Minecraft.class, "rightClickDelay");
            }
            try {
                if (rightClickDelay != null) {
                    rightClickDelay.setInt(mc, 0);
                }
            } catch (IllegalAccessException | RuntimeException ignored) {
                // field not available in this version
            }
        }
        if (FAST_BREAK.enabled) {
            if (destroyDelay == null) {
                destroyDelay = find(mc.gameMode.getClass(), "destroyDelay");
                destroyProgress = find(mc.gameMode.getClass(), "destroyProgress");
            }
            try {
                if (destroyDelay != null) {
                    destroyDelay.setInt(mc.gameMode, 0);
                }
                if (destroyProgress != null && mc.gameMode.isDestroying()) {
                    float v = destroyProgress.getFloat(mc.gameMode);
                    destroyProgress.setFloat(mc.gameMode, Math.min(0.95F, v + FAST_BREAK.a * 0.02F));
                }
            } catch (IllegalAccessException | RuntimeException ignored) {
                // fields not available in this version
            }
        }
        if (AUTO_MINE.enabled && noScreen) {
            mc.options.keyAttack.setDown(mc.hitResult instanceof BlockHitResult);
        }
        if (NUKER.enabled && noScreen) {
            nuker(mc, p);
        }
        if (SCAFFOLD.enabled && noScreen && selectItem(p, s -> s.getItem() instanceof BlockItem)) {
            placeAt(mc, p, p.blockPosition().below());
        }
        if (AUTO_FARM.enabled && noScreen && tick % 2 == 0) {
            autoFarm(mc, p);
        }
        if (AUTO_FISH.enabled) {
            autoFish(mc, p);
        }
        if (SURROUND.enabled && noScreen && selectItem(p, s -> s.is(Items.OBSIDIAN))) {
            for (Direction d : SIDES) {
                if (placeAt(mc, p, p.blockPosition().relative(d))) {
                    break;
                }
            }
        }
        if (AUTO_TRAP.enabled && noScreen) {
            autoTrap(mc, p);
        }
        if (BED_AURA.enabled && noScreen && tick % Math.max(1, BED_AURA.b) == 0) {
            bedAura(mc, p);
        }
    }

    private static void nuker(Minecraft mc, LocalPlayer p) {
        int r = NUKER.a;
        BlockPos me = p.blockPosition();
        int left = p.isCreative() ? NUKER.b : 1;
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = me.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(pos);
                    if (st.isAir() || !st.getFluidState().isEmpty() || st.getDestroySpeed(mc.level, pos) < 0.0F
                            || pos.equals(me.below())) {
                        continue;
                    }
                    double d = p.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
                    if (d > (double) r * r) {
                        continue;
                    }
                    if (p.isCreative()) {
                        if (left > 0) {
                            mc.gameMode.startDestroyBlock(pos, Direction.UP);
                            left--;
                        }
                    } else if (d < bestD) {
                        bestD = d;
                        best = pos;
                    }
                }
            }
        }
        if (best != null) {
            breakBlock(mc, p, best, nukerPos);
            nukerPos = best;
        } else {
            nukerPos = null;
        }
    }

    private static void autoFarm(Minecraft mc, LocalPlayer p) {
        int r = AUTO_FARM.a;
        BlockPos me = p.blockPosition();
        BlockPos mature = null;
        BlockPos empty = null;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = me.offset(dx, dy, dz);
                    BlockState st = mc.level.getBlockState(pos);
                    if (st.getBlock() instanceof CropBlock crop && crop.isMaxAge(st)) {
                        mature = pos;
                    } else if (st.isAir() && mc.level.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
                        empty = pos;
                    }
                }
            }
        }
        if (mature != null) {
            breakBlock(mc, p, mature, farmPos);
            farmPos = mature;
            return;
        }
        farmPos = null;
        if (empty != null && selectItem(p, s -> s.getItem() instanceof BlockItem bi && bi.getBlock() instanceof CropBlock)) {
            BlockPos soil = empty.below();
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(soil).add(0.0, 0.5, 0.0), Direction.UP, soil, false);
            mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
            p.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static void autoFish(Minecraft mc, LocalPlayer p) {
        if (!p.getMainHandItem().is(Items.FISHING_ROD)) {
            return;
        }
        FishingHook hook = p.fishing;
        if (hook == null) {
            hookAge = 0;
            if (recast > 0) {
                recast--;
            } else {
                mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                recast = 20;
            }
            return;
        }
        hookAge++;
        if (hookAge > 40 && hook.isInWater() && hook.getDeltaMovement().y < -0.12) {
            mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
            hookAge = 0;
            recast = 15;
        }
    }

    private static void autoTrap(Minecraft mc, LocalPlayer p) {
        LivingEntity enemy = KillAura.findNearest(mc, 5, 1);
        if (enemy == null || !selectItem(p, s -> s.is(Items.OBSIDIAN))) {
            return;
        }
        BlockPos base = enemy.blockPosition();
        for (Direction d : SIDES) {
            if (placeAt(mc, p, base.relative(d)) || placeAt(mc, p, base.relative(d).above())) {
                return;
            }
        }
        placeAt(mc, p, base.above(2));
    }

    private static void bedAura(Minecraft mc, LocalPlayer p) {
        if (mc.level.dimension() == Level.OVERWORLD) {
            return; // beds only explode in the Nether and the End
        }
        LivingEntity enemy = KillAura.findNearest(mc, BED_AURA.a + 2, 1);
        if (enemy == null) {
            return;
        }
        BlockPos base = enemy.blockPosition();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    if (mc.level.getBlockState(pos).getBlock() instanceof BedBlock
                            && p.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) <= (double) BED_AURA.a * BED_AURA.a) {
                        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                        mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hit);
                        p.swing(InteractionHand.MAIN_HAND);
                        return;
                    }
                }
            }
        }
        if (selectItem(p, s -> s.is(ItemTags.BEDS))) {
            for (Direction d : SIDES) {
                BlockPos pos = base.relative(d);
                if (p.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) <= (double) BED_AURA.a * BED_AURA.a
                        && placeAt(mc, p, pos)) {
                    return;
                }
            }
        }
    }
}
