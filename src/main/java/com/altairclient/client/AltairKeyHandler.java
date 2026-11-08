package com.altairclient.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class AltairKeyHandler {

    private static boolean wasMenuKeyDown = false;

    private AltairKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(
            net.minecraftforge.event.TickEvent.ClientTickEvent.Post event
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getWindow() == null) {
            return;
        }

        boolean isDown = InputConstants.isKeyDown(
                minecraft.getWindow(),
                GLFW.GLFW_KEY_RIGHT_SHIFT
        );

        if (isDown && !wasMenuKeyDown) {
            if (minecraft.gui.screen() instanceof AltairScreen) {
                minecraft.gui.setScreen(null);
            } else if (minecraft.gui.screen() == null) {
                minecraft.gui.setScreen(new AltairScreen());
            }
        }
        wasMenuKeyDown = isDown;
    }
}
