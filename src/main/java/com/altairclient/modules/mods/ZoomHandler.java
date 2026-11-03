package com.altairclient.client;

import com.altairclient.AltairClient;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public final class ZoomHandler {

    private static final float ZOOM_FACTOR = 0.25F;

    private static final KeyMapping ZOOM_KEY = new KeyMapping(
            "key.altairclient.zoom",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.altairclient"
    );

    private ZoomHandler() {
    }

    // Registro de la tecla en Minecraft.
    @Mod.EventBusSubscriber(
            modid = AltairClient.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ModEvents {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ZOOM_KEY);
        }
    }

    // Aplicación del zoom.
    @Mod.EventBusSubscriber(
            modid = AltairClient.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onComputeFov(ComputeFovModifierEvent event) {
            if (ZOOM_KEY.isDown()) {
                event.setNewFovModifier(
                        event.getNewFovModifier() * ZOOM_FACTOR
                );
            }
        }
    }
}
