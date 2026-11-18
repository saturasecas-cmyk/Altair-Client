package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.BlockOutline;
import com.altairclient.modules.mods.CustomCrosshair;
import com.altairclient.modules.mods.TimeChanger;
import com.altairclient.modules.mods.WeatherChanger;
import com.altairclient.modules.mods.AutoRespawn;
import com.altairclient.modules.mods.AutoTool;
import com.altairclient.modules.mods.FastGame;
import com.altairclient.modules.mods.ChatBubbleHandler;
import com.altairclient.modules.mods.DamageIndicator;
import com.altairclient.modules.mods.NoHurtCamera;
import com.altairclient.modules.mods.FovHandler;
import com.altairclient.modules.mods.NametagHandler;
import com.altairclient.modules.mods.FullbrightHandler;
import com.altairclient.modules.mods.FreecamHandler;
import com.altairclient.modules.mods.PerspectiveHandler;
import com.altairclient.modules.mods.ScreenshotModule;
import com.altairclient.modules.mods.ScreenshotUpload;
import com.altairclient.modules.mods.ToggleSneak;
import com.altairclient.modules.mods.ToggleSprint;
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

    // Sin tecla por defecto: se asigna en Controles si se quiere un atajo.
    public static final KeyMapping TOGGLE_SPRINT_KEY = new KeyMapping(
            "key.altairclient.toggle_sprint",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping TOGGLE_SNEAK_KEY = new KeyMapping(
            "key.altairclient.toggle_sneak",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Mantener pulsada: la cámara orbita alrededor del jugador sin girarlo.
    public static final KeyMapping PERSPECTIVE_KEY = new KeyMapping(
            "key.altairclient.perspective",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );

    public static final KeyMapping SCREENSHOT_KEY = new KeyMapping(
            "key.altairclient.screenshot",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            CATEGORY
    );

    // Sin tecla por defecto: sube la última captura a tu webhook de Discord.
    public static final KeyMapping SCREENSHOT_UPLOAD_KEY = new KeyMapping(
            "key.altairclient.screenshot_upload",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Activa/desactiva Freelook: cámara libre en tercera persona (el mismo estado que la tarjeta del menú).
    public static final KeyMapping FREELOOK_KEY = new KeyMapping(
            "key.altairclient.freelook",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );

    // Sin tecla por defecto: activa/desactiva Fullbright.
    public static final KeyMapping FULLBRIGHT_KEY = new KeyMapping(
            "key.altairclient.fullbright",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Sin tecla por defecto: activa/desactiva Custom Nametags.
    public static final KeyMapping NAMETAGS_KEY = new KeyMapping(
            "key.altairclient.nametags",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Sin tecla por defecto: activa/desactiva Better Chat Bubbles.
    public static final KeyMapping CHAT_BUBBLES_KEY = new KeyMapping(
            "key.altairclient.chat_bubbles",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Sin tecla por defecto: se asigna en Controles.
    public static final KeyMapping TIME_CHANGER_KEY = new KeyMapping(
            "key.altairclient.time_changer",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Sin tecla por defecto: se asigna en Controles.
    public static final KeyMapping WEATHER_CHANGER_KEY = new KeyMapping(
            "key.altairclient.weather_changer",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Sin tecla por defecto: se asigna en Controles.
    public static final KeyMapping BLOCK_OUTLINE_KEY = new KeyMapping(
            "key.altairclient.block_outline",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    // Sin tecla por defecto: se asigna en Controles.
    public static final KeyMapping CROSSHAIR_KEY = new KeyMapping(
            "key.altairclient.crosshair",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
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
        event.register(TOGGLE_SPRINT_KEY);
        event.register(TOGGLE_SNEAK_KEY);
        event.register(PERSPECTIVE_KEY);
        event.register(SCREENSHOT_KEY);
        event.register(SCREENSHOT_UPLOAD_KEY);
        event.register(FREELOOK_KEY);
        event.register(FULLBRIGHT_KEY);
        event.register(NAMETAGS_KEY);
        event.register(CHAT_BUBBLES_KEY);
        event.register(TIME_CHANGER_KEY);
        event.register(WEATHER_CHANGER_KEY);
        event.register(BLOCK_OUTLINE_KEY);
        event.register(CROSSHAIR_KEY);
    }

    /** Antes de cada tick: el giro del ratón pasa a la cámara y la cámara se mueve con WASD. */
    private static void onClientTickPre(TickEvent.ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        NoHurtCamera.apply(mc);
        AutoTool.tick(mc);
        FastGame.tick(mc);
        ToggleSprint.tick(mc);
        FullbrightHandler.tick(mc);
        ToggleSneak.tick(mc);

        if (FreecamHandler.absorbRotation()) {
            FreecamHandler.tickMovement();
        } else {
            PerspectiveHandler.absorb();
        }
    }

    /** Cada frame: la cámara gira con el ratón mientras el jugador sigue mirando al mismo sitio. */
    private static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        NoHurtCamera.apply(Minecraft.getInstance());
        if (FreecamHandler.absorbRotation()) {
            event.setYaw(FreecamHandler.getCameraYaw());
            event.setPitch(FreecamHandler.getCameraPitch());
        } else if (!PerspectiveHandler.isCameraMixinActive() && PerspectiveHandler.absorb()) {
            event.setYaw(event.getYaw() + PerspectiveHandler.getYawOffset());
            event.setPitch(event.getPitch() + PerspectiveHandler.getPitchOffset());
        }
    }

    /** Aplica el FOV personalizado/dinámico y el zoom (mientras se mantiene pulsada la tecla de zoom). */
    private static void onComputeFov(ViewportEvent.ComputeFov event) {
        event.setFOV(ZoomHandler.applyZoom(FovHandler.apply(event.getFOV())));
    }

    private static void onClientTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        // Cuenta los clics también a nivel de tick (además de en cada frame del HUD).
        CPSCounter.getInstance().poll();

        AutoRespawn.tick(minecraft);
        DamageIndicator.getInstance().tick(minecraft);

        // La captura se hace aunque el menú acabe de cerrarse (espera a que no haya pantalla abierta).
        ScreenshotModule.tick(minecraft);

        if (minecraft.player == null || minecraft.gui.screen() != null) {
            ZoomHandler.setZoomKeyDown(false);
            PerspectiveHandler.setKeyDown(false);
            return;
        }

        while (MENU_KEY.consumeClick()) {
            minecraft.gui.setScreen(new AltairScreen());
        }

        while (FREECAM_KEY.consumeClick()) {
            FreecamHandler.toggle();
        }

        while (TOGGLE_SPRINT_KEY.consumeClick()) {
            ToggleSprint.toggle();
        }

        while (TOGGLE_SNEAK_KEY.consumeClick()) {
            ToggleSneak.toggle();
        }

        while (SCREENSHOT_KEY.consumeClick()) {
            ScreenshotModule.request();
        }

        while (FREELOOK_KEY.consumeClick()) {
            PerspectiveHandler.toggleLatched();
        }

        while (NAMETAGS_KEY.consumeClick()) {
            NametagHandler.toggle();
        }

        while (CHAT_BUBBLES_KEY.consumeClick()) {
            ChatBubbleHandler.toggle();
        }

        while (TIME_CHANGER_KEY.consumeClick()) {
            TimeChanger.toggle();
        }

        while (WEATHER_CHANGER_KEY.consumeClick()) {
            WeatherChanger.toggle();
        }

        while (BLOCK_OUTLINE_KEY.consumeClick()) {
            BlockOutline.toggle();
        }

        while (CROSSHAIR_KEY.consumeClick()) {
            CustomCrosshair.toggle();
        }

        while (FULLBRIGHT_KEY.consumeClick()) {
            FullbrightHandler.toggle();
        }

        while (SCREENSHOT_UPLOAD_KEY.consumeClick()) {
            ScreenshotUpload.upload();
        }

        ZoomHandler.setZoomKeyDown(ZOOM_KEY.isDown());
        PerspectiveHandler.setKeyDown(PERSPECTIVE_KEY.isDown());
    }
}
