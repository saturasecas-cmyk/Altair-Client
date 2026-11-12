package com.altairclient;

import com.altairclient.client.AltairKeyHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("altairclient")
public class AltairClient {
    public static final Logger LOGGER = LogManager.getLogger("AltairClient");

    public AltairClient() {
        LOGGER.info("Inicializando Altair Client para Forge...");

        // Registrar configuración del cliente en el bus de mod de Forge
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);

        // Registrar en el bus de eventos de Forge para ticks, HUD, etc.
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Configuración de cliente de Altair Client completada.");
        
        // Inicializar las teclas y eventos de cliente propios de Forge
        AltairKeyHandler.init();
    }
}
