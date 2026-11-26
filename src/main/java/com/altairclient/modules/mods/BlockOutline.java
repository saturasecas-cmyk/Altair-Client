package com.altairclient.modules.mods;

import com.altairclient.client.Palette;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderHighlightEvent;

/** Block Outline: el contorno del bloque al que miras, con el color de la paleta de Altair. */
public final class BlockOutline {

    private static boolean enabled = false;
    private static boolean registered = false;
    private static int colorIndex = 0;
    private static int width = 2;

    public static final int MIN_WIDTH = 1;
    public static final int MAX_WIDTH = 6;

    private BlockOutline() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            RenderHighlightEvent.Block.BUS.addListener(BlockOutline::onHighlight);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static int getColorIndex() {
        return colorIndex;
    }

    public static void setColorIndex(int v) {
        colorIndex = Math.max(0, Math.min(Palette.MAX_INDEX, v));
    }

    public static int getWidth() {
        return width;
    }

    public static void setWidth(int v) {
        width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, v));
    }

    /** Sustituye el contorno de vanilla por el nuestro. Devuelve false para no cancelar el evento. */
    private static boolean onHighlight(RenderHighlightEvent.Block event) {
        if (!enabled) {
            return false;
        }
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        return true;
        BlockPos pos = event.getTarget().getBlockPos();
        net.minecraft.world.phys.shapes.VoxelShape shape = mc.level.getBlockState(pos)
                .getShape(mc.level, pos, net.minecraft.world.phys.shapes.CollisionContext.of(event.getCamera().getEntity()));
        Vec3 cam = event.getCamera().getPosition();
        com.mojang.blaze3d.vertex.VertexConsumer vc = event.getMultiBufferSource()
                .getBuffer(net.minecraft.client.renderer.RenderType.lines());
        int c = Palette.color(colorIndex);
        float r = ((c >> 16) & 255) / 255.0F;
        float g = ((c >> 8) & 255) / 255.0F;
        float b = (c & 255) / 255.0F;
        com.mojang.blaze3d.systems.RenderSystem.lineWidth((float) width);
        PoseStack ps = event.getPoseStack();
        shape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> net.minecraft.client.renderer.ShapeRenderer.renderLineBox(
                ps, vc, x1 + pos.getX() - cam.x, y1 + pos.getY() - cam.y, z1 + pos.getZ() - cam.z,
                x2 + pos.getX() - cam.x, y2 + pos.getY() - cam.y, z2 + pos.getZ() - cam.z, r, g, b, 1.0F));
        return false;
    }
}
