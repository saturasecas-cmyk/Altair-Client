package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hacks > Render: ESP, Tracers, Chams, Xray, Trajectories and the NoRender options.
 * ESP, Tracers and Trajectories are drawn in 2D over the screen from the player's first-person view.
 */
public final class RenderHacks {

    public static final String[] ESP_SHOW = {"All", "Players", "Mobs", "Items", "Chests+Spawners"};
    public static final String[] TRACER_SHOW = {"Players", "Mobs", "Both"};
    public static final String[] XRAY_MODES = {"Ores", "Ores+Containers", "Custom file"};

    public static final Hacks.Mod ESP = new Hacks.Mod(64, 0, null);          // a = range, b = what to show
    public static final Hacks.Mod TRACERS = new Hacks.Mod(64, 2, null);       // a = range, b = who
    public static final Hacks.Mod CHAMS = new Hacks.Mod(2, 0, null);          // a = who (0 players, 1 mobs, 2 both)
    public static final Hacks.Mod XRAY = new Hacks.Mod(0, 0, () -> pendingReload = true);  // a = mode
    public static final Hacks.Mod TRAJECTORIES = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod NO_FOG = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod NO_RAIN = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod NO_PARTICLES = new Hacks.Mod(0, 0, null);
    public static final Hacks.Mod NO_ANIMATIONS = new Hacks.Mod(0, 0, RenderHacks::restoreAnimations);

    public static net.minecraft.client.Camera camera = null;
    private static boolean pendingReload = false;
    private static boolean registered = false;
    private static Set<String> custom = new HashSet<>();
    private static Boolean savedBob = null;
    private static Double savedTilt = null;

    // Block scan for ESP containers
    private static final int R = 24;
    private static List<BlockPos> found = new ArrayList<>();
    private static List<BlockPos> building = new ArrayList<>();
    private static int scanLayer = -R;
    private static BlockPos scanOrigin = BlockPos.ZERO;

    private RenderHacks() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(RenderHacks::onTick);
    }

    // ==================== Xray ====================

    public static void toggleXray() {
        XRAY.toggle();
        if (XRAY.enabled) {
            loadCustom();
        }
        pendingReload = true;
    }

    /** Called when the mod menu closes: rebuilds the chunks (not the resource packs) so Xray shows up. */
    public static void applyPendingReload() {
        if (pendingReload) {
            pendingReload = false;
            reloadChunks();
        }
    }

    private static void reloadChunks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && camera != null && mc.levelRenderer != null) {
            mc.levelRenderer.invalidateCompiledGeometry(mc.level, mc.options, camera, mc.getBlockColors());
        }
    }

    private static void loadCustom() {
        Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("altairclient-xray.txt");
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, "# Xray custom block list: one block id per line, e.g. minecraft:diamond_ore\n"
                        + "minecraft:diamond_ore\nminecraft:deepslate_diamond_ore\nminecraft:ancient_debris\nminecraft:spawner\n");
            }
            Set<String> set = new HashSet<>();
            for (String line : Files.readAllLines(file)) {
                String t = line.trim();
                if (!t.isEmpty() && !t.startsWith("#")) {
                    set.add(t.contains(":") ? t : "minecraft:" + t);
                }
            }
            custom = set;
        } catch (IOException | RuntimeException e) {
            custom = new HashSet<>();
        }
    }

    /** True if Xray is on and the block should be hidden (not part of the chosen list). */
    public static boolean xrayHides(BlockState st) {
        if (!XRAY.enabled) {
            return false;
        }
        if (st.isAir()) {
            return false;
        }
        if (XRAY.a == 2) {
            return !custom.contains(BuiltInRegistries.BLOCK.getKey(st.getBlock()).toString());
        }
        if (st.is(Tags.Blocks.ORES) || st.is(Blocks.ANCIENT_DEBRIS)) {
            return false;
        }
        if (XRAY.a == 1 && (st.is(Blocks.CHEST) || st.is(Blocks.TRAPPED_CHEST) || st.is(Blocks.ENDER_CHEST)
                || st.is(Blocks.BARREL) || st.is(Blocks.SPAWNER) || st.is(BlockTags.SHULKER_BOXES))) {
            return false;
        }
        return true;
    }

    public static boolean xrayShows(BlockState st) {
        return XRAY.enabled && !xrayHides(st);
    }

    // ==================== Chams (outline through walls) ====================

    public static boolean glows(Entity e) {
        if (!CHAMS.enabled) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (e == mc.player) {
            return false;
        }
        return (CHAMS.a != 1 && e instanceof Player) || (CHAMS.a != 0 && e instanceof Mob);
    }

    // ==================== NoRender ====================

    private static void restoreAnimations() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options != null) {
            if (savedBob != null) {
                mc.options.bobView().set(savedBob);
            }
            if (savedTilt != null) {
                mc.options.damageTiltStrength().set(savedTilt);
            }
        }
        savedBob = null;
        savedTilt = null;
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options == null) {
            return;
        }
        if (NO_RAIN.enabled) {
            mc.level.setRainLevel(0.0F);
            mc.level.setThunderLevel(0.0F);
        }
        if (NO_ANIMATIONS.enabled) {
            if (savedBob == null) {
                savedBob = mc.options.bobView().get();
                savedTilt = mc.options.damageTiltStrength().get();
            }
            mc.options.bobView().set(false);
            mc.options.damageTiltStrength().set(0.0D);
        }
        if (ESP.enabled && (ESP.b == 0 || ESP.b == 4)) {
            scanStep(mc);
        } else if (!found.isEmpty()) {
            found = new ArrayList<>();
        }
    }

    private static boolean isContainer(BlockState st) {
        return st.is(Blocks.CHEST) || st.is(Blocks.TRAPPED_CHEST) || st.is(Blocks.ENDER_CHEST)
                || st.is(Blocks.BARREL) || st.is(Blocks.SPAWNER) || st.is(BlockTags.SHULKER_BOXES);
    }

    /** Scans one horizontal layer around the player per tick (so it never causes a lag spike). */
    private static void scanStep(Minecraft mc) {
        if (scanLayer == -R) {
            scanOrigin = mc.player.blockPosition();
            building = new ArrayList<>();
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                pos.set(scanOrigin.getX() + dx, scanOrigin.getY() + scanLayer, scanOrigin.getZ() + dz);
                if (isContainer(mc.level.getBlockState(pos))) {
                    building.add(pos.immutable());
                }
            }
        }
        scanLayer++;
        if (scanLayer > R) {
            scanLayer = -R;
            found = building;
        }
    }

    // ==================== 2D projection ====================

    private static double ex, ey, ez;
    private static double fx, fy, fz, rx, ry, rz, ux, uy, uz;
    private static double cx, cy, scale;
    private static final double[] OUT = new double[2];

    private static void setupCamera(Minecraft mc, LocalPlayer p, float partial) {
        net.minecraft.client.Camera cam = camera;
        double yaw;
        double pitch;
        double fovDeg;
        if (cam != null && cam.isInitialized()) {
            Vec3 pos = cam.position();
            ex = pos.x;
            ey = pos.y;
            ez = pos.z;
            yaw = Math.toRadians(cam.yRot());
            pitch = Math.toRadians(cam.xRot());
            fovDeg = cam.getFov();
        } else {
            Vec3 eye = p.getEyePosition(partial);
            ex = eye.x;
            ey = eye.y;
            ez = eye.z;
            yaw = Math.toRadians(p.getViewYRot(partial));
            pitch = Math.toRadians(p.getViewXRot(partial));
            fovDeg = mc.options.fov().get();
        }
        if (fovDeg < 5.0 || fovDeg > 170.0) {
            fovDeg = mc.options.fov().get();
        }
        fx = -Math.sin(yaw) * Math.cos(pitch);
        fy = -Math.sin(pitch);
        fz = Math.cos(yaw) * Math.cos(pitch);
        rx = -Math.cos(yaw);
        ry = 0.0;
        rz = -Math.sin(yaw);
        ux = ry * fz - rz * fy;
        uy = rz * fx - rx * fz;
        uz = rx * fy - ry * fx;
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        cx = w / 2.0;
        cy = h / 2.0;
        scale = (h / 2.0) / Math.tan(Math.toRadians(fovDeg) / 2.0);
    }

    /** Projects a world point to GUI coordinates into OUT. Returns false if it is behind the camera. */
    private static boolean project(double x, double y, double z) {
        double dx = x - ex;
        double dy = y - ey;
        double dz = z - ez;
        double zc = dx * fx + dy * fy + dz * fz;
        if (zc < 0.05) {
            return false;
        }
        double xc = dx * rx + dy * ry + dz * rz;
        double yc = dx * ux + dy * uy + dz * uz;
        OUT[0] = cx + xc / zc * scale;
        OUT[1] = cy - yc / zc * scale;
        return true;
    }

    private static void box(GuiGraphics g, AABB bb, int color) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            double x = (i & 1) == 0 ? bb.minX : bb.maxX;
            double y = (i & 2) == 0 ? bb.minY : bb.maxY;
            double z = (i & 4) == 0 ? bb.minZ : bb.maxZ;
            if (!project(x, y, z)) {
                return;
            }
            minX = Math.min(minX, OUT[0]);
            maxX = Math.max(maxX, OUT[0]);
            minY = Math.min(minY, OUT[1]);
            maxY = Math.max(maxY, OUT[1]);
        }
        int x0 = (int) minX;
        int y0 = (int) minY;
        int x1 = (int) maxX;
        int y1 = (int) maxY;
        if (x1 - x0 > 2000 || y1 - y0 > 2000) {
            return;
        }
        g.fill(x0, y0, x1, y0 + 1, color);
        g.fill(x0, y1 - 1, x1, y1, color);
        g.fill(x0, y0, x0 + 1, y1, color);
        g.fill(x1 - 1, y0, x1, y1, color);
    }

    private static void line(GuiGraphics g, double x0, double y0, double x1, double y1, int color) {
        double dx = x1 - x0;
        double dy = y1 - y0;
        int steps = (int) Math.min(600, Math.max(Math.abs(dx), Math.abs(dy)) / 1.5);
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            int x = (int) (x0 + dx * t);
            int y = (int) (y0 + dy * t);
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    private static int colorFor(Entity e) {
        if (e instanceof Player) {
            return 0xFF55E6FF;
        }
        if (e instanceof Enemy) {
            return 0xFFFF5555;
        }
        if (e instanceof ItemEntity) {
            return 0xFFFFE055;
        }
        return 0xFF66FF88;
    }

    // ==================== Render (called from the HUD layer) ====================

    public static void render(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || (!ESP.enabled && !TRACERS.enabled && !TRAJECTORIES.enabled)) {
            return;
        }
        setupCamera(mc, p, partial);
        if (ESP.enabled || TRACERS.enabled) {
            double range = Math.max(ESP.enabled ? ESP.a : 0, TRACERS.enabled ? TRACERS.a : 0);
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            int tracers = 0;
            for (Entity e : mc.level.getEntities((Entity) null, p.getBoundingBox().inflate(range), x -> true)) {
                if (e == p) {
                    continue;
                }
                double d = p.distanceTo(e);
                if (ESP.enabled && d <= ESP.a && showEsp(e)) {
                    box(g, e.getBoundingBox(), colorFor(e));
                }
                boolean tracerKind = (TRACERS.b != 1 && e instanceof Player) || (TRACERS.b != 0 && e instanceof Mob);
                if (TRACERS.enabled && tracerKind && d <= TRACERS.a && tracers < 20) {
                    Vec3 c = e.getBoundingBox().getCenter();
                    if (project(c.x, c.y, c.z)) {
                        line(g, w / 2.0, h, OUT[0], OUT[1], colorFor(e));
                        tracers++;
                    }
                }
            }
            if (ESP.enabled && (ESP.b == 0 || ESP.b == 4)) {
                for (BlockPos bp : found) {
                    if (p.blockPosition().distSqr(bp) > (double) ESP.a * ESP.a) {
                        continue;
                    }
                    BlockState st = mc.level.getBlockState(bp);
                    box(g, new AABB(bp), st.is(Blocks.SPAWNER) ? 0xFFB06BFF : 0xFFFFC04D);
                }
            }
        }
        if (TRAJECTORIES.enabled) {
            trajectory(g, mc, p);
        }
    }

    private static boolean showEsp(Entity e) {
        return switch (ESP.b) {
            case 1 -> e instanceof Player;
            case 2 -> e instanceof Mob;
            case 3 -> e instanceof ItemEntity;
            case 4 -> false;
            default -> e instanceof Player || e instanceof Mob || e instanceof ItemEntity;
        };
    }

    private static void trajectory(GuiGraphics g, Minecraft mc, LocalPlayer p) {
        ItemStack st = p.getMainHandItem();
        double speed;
        double grav;
        if (st.is(Items.BOW)) {
            if (!p.isUsingItem()) {
                return;
            }
            float f = p.getTicksUsingItem() / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if (f < 0.1F) {
                return;
            }
            speed = Math.min(f, 1.0F) * 3.0;
            grav = 0.05;
        } else if (st.is(Items.CROSSBOW)) {
            speed = 3.15;
            grav = 0.05;
        } else if (st.is(Items.TRIDENT)) {
            speed = 2.5;
            grav = 0.05;
        } else if (st.is(Items.ENDER_PEARL) || st.is(Items.SNOWBALL) || st.is(Items.EGG)) {
            speed = 1.5;
            grav = 0.03;
        } else {
            return;
        }
        Vec3 look = p.getLookAngle();
        Vec3 side = look.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 pos = p.getEyePosition().add(side.scale(-0.25)).add(0.0, -0.2, 0.0);
        Vec3 vel = look.scale(speed);
        Vec3 start = pos;
        double px = 0;
        double py = 0;
        boolean havePrev = false;
        Vec3 end = null;
        for (int i = 0; i < 160; i++) {
            Vec3 next = pos.add(vel);
            BlockPos bp = BlockPos.containing(next);
            boolean hit = !mc.level.getBlockState(bp).getCollisionShape(mc.level, bp).isEmpty();
            AABB probe = new AABB(next, next).inflate(0.3);
            boolean entityHit = false;
            for (Entity e : mc.level.getEntities(p, probe, x -> x instanceof net.minecraft.world.entity.LivingEntity)) {
                entityHit = true;
                break;
            }
            if (project(next.x, next.y, next.z)) {
                if (havePrev) {
                    int shade = 255 - Math.min(150, i);
                    line(g, px, py, OUT[0], OUT[1], 0xFF000000 | (shade << 16) | (shade << 8) | 255);
                }
                px = OUT[0];
                py = OUT[1];
                havePrev = true;
            } else {
                havePrev = false;
            }
            if (hit || entityHit || next.y < mc.level.getMinY()) {
                end = next;
                break;
            }
            pos = next;
            vel = vel.scale(0.99).add(0.0, -grav, 0.0);
        }
        if (end != null && project(end.x, end.y, end.z)) {
            int x = (int) OUT[0];
            int y = (int) OUT[1];
            g.fill(x - 5, y - 5, x + 6, y + 6, 0xFFFFFFFF);
            g.fill(x - 4, y - 4, x + 5, y + 5, 0xFFFF3B3B);
            g.fill(x - 1, y - 7, x + 2, y + 8, 0xFFFFFFFF);
            g.fill(x - 7, y - 1, x + 8, y + 2, 0xFFFFFFFF);
            String dist = String.format("%.1fm", start.distanceTo(end));
            g.drawString(mc.font, dist, x + 9, y - 4, 0xFFFFFFFF, true);
        }
    }
}
