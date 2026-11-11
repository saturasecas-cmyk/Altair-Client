
package com.altairclient.client;

import com.altairclient.AltairClient;
import com.altairclient.modules.mods.ZoomHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(
        modid = AltairClient.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = net.minecraftforge.api.distmarker.Dist.CLIENT
)
public class AltairKeyHandler {

    public static final String CATEGORY =
            "key.category.altairclient.altair_client";

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

    private static boolean registered = false;

    public static void register() {
        registered = true;
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
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

            if (minecraft.player == null || minecraft.screen != null) {
                ZoomHandler.setZoomKeyDown(false);
                return;
            }

            while (MENU_KEY.consumeClick()) {
                minecraft.setScreen(new AltairScreen());
            }

            ZoomHandler.setZoomKeyDown(ZOOM_KEY.isDown());
        }
    }
}
