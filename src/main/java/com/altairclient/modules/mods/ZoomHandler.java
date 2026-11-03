package com.altairclient.modules.mods;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public final class ZoomHandler {

    private static final float ZOOM_FACTOR = 0.25F;

    private static final KeyMapping.Category ALTAIR_CATEGORY =
            KeyMapping.Category.register("key.categories.altairclient");

    private static final KeyMapping ZOOM_KEY = new KeyMapping(
            "key.altairclient.zoom",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            ALTAIR_CATEGORY
    );

    private ZoomHandler() {
    }

    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ModEvents {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ZOOM_KEY);
        }
    }

    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onFov(ComputeFovModifierEvent event) {
            if (ZOOM_KEY.isDown()) {
                event.setNewFovModifier(
                        event.getNewFovModifier() * ZOOM_FACTOR
                );
            }
        }
    }
}
