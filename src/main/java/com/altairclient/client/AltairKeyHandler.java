package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ZoomHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class AltairKeyHandler {

    // Se traduce con la clave "key.category.altairclient.altair_client" del archivo de idioma.
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AltairClient.MOD_ID, "altair_client")
    );

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

    /** Registra el evento de teclas y el tick del cliente. */
    public static void register() {
        RegisterKeyMappingsEvent.BUS.addListener(AltairKeyHandler::registerKeys);
        TickEvent.ClientTickEvent.Post.BUS.addListener(AltairKeyHandler::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ZOOM_KEY);
        event.register(MENU_KEY);
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gui.screen() != null) {
            ZoomHandler.setZoomKeyDown(false);
            return;
        }

        while (MENU_KEY.consumeClick()) {
            minecraft.gui.setScreen(new AltairScreen());
        }

        ZoomHandler.setZoomKeyDown(ZOOM_KEY.isDown());
    }
}
