package com.altairclient.modules.mods;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ZoomHandler {

    private static final float ZOOM_FACTOR = 0.25F;

    private ZoomHandler() {
    }

    @SubscribeEvent
    public static void onFov(ComputeFovModifierEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getWindow() != null) {
            if (InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_C)) {
                event.setNewFovModifier(
                        event.getNewFovModifier() * ZOOM_FACTOR
                );
            }
        }
    }
}
