package com.altairclient.client;

import com.altairclient.modules.mods.ZoomHandler;
import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

public final class AltairKeyHandler {

    public static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.MISC;

    public static final KeyMapping ZOOM_KEY = new KeyMapping(
            "key.altairclient.zoom",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            CATEGORY
    );

    public static final KeyMapping MENU_KEY = new KeyMapping(
            "key.altairclient.menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            CATEGORY
    );

    private AltairKeyHandler() {
    }

    public static void register() {
        KeyMappingHelper.registerKeyMapping(ZOOM_KEY);
        KeyMappingHelper.registerKeyMapping(MENU_KEY);

        ClientTickEvents.END_CLIENT_TICK.register((Minecraft minecraft) -> {
            if (minecraft.player == null || minecraft.gui.screen() != null) {
                ZoomHandler.setZoomKeyDown(false);
                return;
            }

            while (MENU_KEY.consumeClick()) {
                minecraft.gui.setScreen(new AltairScreen());
            }

            ZoomHandler.setZoomKeyDown(ZOOM_KEY.isDown());
        });
    }
}
