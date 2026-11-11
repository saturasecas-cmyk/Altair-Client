
package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ZoomHandler;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = AltairClient.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = net.minecraftforge.api.distmarker.Dist.CLIENT
)
public class AltairKeyHandler {

    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(
                    Identifier.fromNamespaceAndPath(
                            AltairClient.MOD_ID,
                            "altair_client"
                    )
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

    // Se conserva para no romper AltairClient.java.
    public static void register() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(ZOOM_KEY);
        event.register(MENU_KEY);
    }

    @Mod.EventBusSubscriber(
            modid = AltairClient.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.FORGE,
            value = net.minecraftforge.api.distmarker.Dist.CLIENT
    )
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent.Post event) {

            Minecraft minecraft = Minecraft.getInstance();

            // No activar controles mientras hay una pantalla abierta.
            // Esto evita que C active el Zoom al escribir en el buscador
            // del inventario creativo.
            if (minecraft.player == null || minecraft.gui.screen() != null) {
                ZoomHandler.setZoomKeyDown(false);
                return;
            }

            // Abrir el menú de Altair.
            while (MENU_KEY.consumeClick()) {
                minecraft.gui.setScreen(new AltairScreen());
            }

            // Zoom mientras se mantiene pulsada la tecla asignada.
            ZoomHandler.setZoomKeyDown(ZOOM_KEY.isDown());
        }
    }
}
