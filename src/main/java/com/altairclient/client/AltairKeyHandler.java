package com.altairclient.client;

import com.altairclient.modules.mods.ZoomHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "altairclient", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AltairKeyHandler {

    public static final String CATEGORY = "key.categories.altairclient";

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

    public static void init() {
        // Registrar la clase en el bus de eventos global de Forge para escuchar los Ticks del juego
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(AltairKeyHandler.class);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ZOOM_KEY);
        event.register(MENU_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

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
