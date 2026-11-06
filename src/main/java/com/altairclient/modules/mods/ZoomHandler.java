package com.altairclient.modules.mods;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public final class ZoomHandler {

    private static final float ZOOM_FACTOR = 0.25F;

    private ZoomHandler() {
    }

    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onFov(ComputeFovModifierEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.getWindow() == null || minecraft.player == null) {
                return;
            }

            // Solo hace zoom mientras juegas (sin ningún menú abierto)
            if (minecraft.gui.screen() == null) {
                // Pasa directamente minecraft.getWindow()
                if (InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_C)) {
                    event.setNewFovModifier(
                            event.getNewFovModifier() * ZOOM_FACTOR
                    );
                }
            }
        }
    }
}
