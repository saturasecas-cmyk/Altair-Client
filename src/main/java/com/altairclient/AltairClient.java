
package com.altairclient;

import com.altairclient.client.AltairHud;
import com.altairclient.client.AltairKeyHandler;
import com.altairclient.modules.mods.AutoGG;
import com.altairclient.modules.mods.ChatBubbleHandler;
import com.altairclient.modules.mods.ChatTimestamps;
import com.altairclient.modules.mods.ClearWater;
import com.altairclient.modules.mods.FastGame;
import com.altairclient.modules.mods.NametagHandler;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AltairClient.MOD_ID)
public class AltairClient {

    public static final String MOD_ID = "altairclient";

    public static final Logger LOGGER =
            LogManager.getLogger("AltairClient");

    public AltairClient(FMLJavaModLoadingContext context) {

        LOGGER.info("Inicializando Altair Client para Forge...");

        AltairKeyHandler.register();
        AltairHud.register();
        NametagHandler.register();
        ChatBubbleHandler.register();
        AutoGG.register();
        ChatTimestamps.register();
        ClearWater.register();
        FastGame.register();

        FMLClientSetupEvent.getBus(
                context.getModBusGroup()
        ).addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Configuración de cliente de Altair Client completada.");
    }
}
