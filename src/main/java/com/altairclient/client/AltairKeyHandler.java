package com.altairclient.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

public final class AltairKeyHandler {

    private static final KeyMapping.Category ALTAIR_CATEGORY =
            KeyMapping.Category.register(
                    Identifier.fromNamespaceAndPath(
                            "altairclient",
                            "altair_client"
                    )
            );

    public static final KeyMapping MENU_KEY = new KeyMapping(
            "key.altairclient.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            ALTAIR_CATEGORY
    );

    private AltairKeyHandler() {
    }

    @Mod.EventBusSubscriber(
            modid = "altairclient",
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = Dist.CLIENT
    )
    public static class ClientEvents {

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(MENU_KEY);
        }

        @SubscribeEvent
        public static void onClientTick(
                net.minecraftforge.event.TickEvent.ClientTickEvent.Post event
        ) {
            Minecraft minecraft = Minecraft.getInstance();

            while (MENU_KEY.consumeClick()) {
                if (minecraft.gui.screen() instanceof AltairScreen) {
                    minecraft.gui.setScreen(null);
                } else if (minecraft.gui.screen() == null) {
                    minecraft.gui.setScreen(new AltairScreen());
                }
            }
        }
    }
}
