package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FreecamHandler;
import com.altairclient.modules.mods.ZoomHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
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

    // Activa/desactiva Freecam (el mismo estado que la tarjeta del menú de módulos).
    public static final KeyMapping FREECAM_KEY = new KeyMapping(
            "key.altairclient.freecam",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );

    private AltairKeyHandler() {
    }

    /** Registra el evento de teclas, el tick del cliente, el FOV (zoom) y la cámara (freecam). */
    public static void register() {
        RegisterKeyMappingsEvent.BUS.addListener(AltairKeyHandler::registerKeys);
        TickEvent.ClientTickEvent.Pre.BUS.addListener(AltairKeyHandler::onClientTickPre);
        TickEvent.ClientTickEvent.Post.BUS.addListener(AltairKeyHandler::onClientTick);
        ViewportEvent.ComputeFov.BUS.addListener(AltairKeyHandler::onComputeFov);
        ViewportEvent.ComputeCameraAngles.BUS.addListener(AltairKeyHandler::onComputeCameraAngles);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ZOOM_KEY);
        event.register(MENU_KEY);
        event.register(FREECAM_KEY);
    }

    /** Antes de cada tick: el giro del ratón pasa a la cámara y la cámara se mueve con WASD. */
    private static void onClientTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (FreecamHandler.absorbRotation()) {
            FreecamHandler.tickMovement();
        }
    }

    /** Cada frame: la cámara gira con el ratón mientras el jugador sigue mirando al mismo sitio. */
    private static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (FreecamHandler.absorbRotation()) {
            event.setYaw(FreecamHandler.getCameraYaw());
            event.setPitch(FreecamHandler.getCameraPitch());
        }
    }

    /** Aplica el zoom al campo de visión mientras se mantiene pulsada la tecla de zoom. */
    private static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(ZoomHandler.applyZoom(event.getFOV()));
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Cuenta los clics también a nivel de tick (además de en cada frame del HUD).
        CPSCounter.getInstance().poll();

        if (minecraft.player == null || minecraft.gui.screen() != null) {
            ZoomHandler.setZoomKeyDown(false);
            return;
        }

        while (MENU_KEY.consumeClick()) {
            minecraft.gui.setScreen(new AltairScreen());
        }

        while (FREECAM_KEY.consumeClick()) {
            FreecamHandler.toggle();
        }

        ZoomHandler.setZoomKeyDown(ZOOM_KEY.isDown());
    }
}
