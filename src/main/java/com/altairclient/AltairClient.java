package com.altairclient;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AltairClient.MOD_ID)
public final class AltairClient {
    public static final String MOD_ID = "altairclient";

    public AltairClient() {
        this(FMLJavaModLoadingContext.get());
    }

    public AltairClient(final FMLJavaModLoadingContext context) {
        if (context != null && context.getModEventBus() != null) {
            context.getModEventBus().addListener(this::clientSetup);
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Punto de entrada de Altair Client
    }
}
