package com.altairclient.modules.mods;

import com.altairclient.client.Palette;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.rendertype.RenderTypes;
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
        // 26.1: el contorno personalizado no esta portado todavia (vanilla).
        return false;
    }
}
