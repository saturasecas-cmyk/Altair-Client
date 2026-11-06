package com.altairclient.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public final class AltairKeyHandler {

    private static boolean wasPressed = false;

    private AltairKeyHandler() {
    }

    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(
                net.minecraftforge.event.TickEvent.ClientTickEvent.Post event
        ) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null || minecraft.getWindow() == null) {
                return;
            }

            long window = minecraft.getWindow().getWindow();
            boolean isPressed = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);

            // Al pulsar Right Shift, abre o cierra el menú de Altair
            if (isPressed && !wasPressed) {
                if (minecraft.gui.screen() instanceof AltairScreen) {
                    minecraft.gui.setScreen(null);
                } else if (minecraft.gui.screen() == null) {
                    minecraft.gui.setScreen(new AltairScreen());
                }
            }
            wasPressed = isPressed;
        }
    }
}
