
package com.altairclient;

import com.altairclient.client.AltairHud;
import com.altairclient.client.AltairKeyHandler;
import com.altairclient.modules.mods.AutoGG;
import com.altairclient.modules.mods.BlockOutline;
import com.altairclient.modules.mods.TimeChanger;
import com.altairclient.modules.mods.WeatherChanger;
import com.altairclient.modules.mods.ChatBubbleHandler;
import com.altairclient.modules.mods.ChatTimestamps;
import com.altairclient.modules.mods.ClearWater;
import com.altairclient.modules.mods.FastGame;
import com.altairclient.modules.mods.NametagHandler;
import com.altairclient.modules.textures.BuildTextures;
import com.altairclient.modules.textures.ExtraTextures;
import com.altairclient.modules.textures.MovementTextures;
import com.altairclient.modules.textures.PvpTextures;

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

    public AltairClient() {

        LOGGER.info("Initializing Altair Client for Forge...");

        AltairKeyHandler.register();
        com.altairclient.client.AltairLink.register();
        com.altairclient.client.AltairConfig.register();
        com.altairclient.client.SafetyGuard.register();
        com.altairclient.modules.mods.KillAura.register();
        com.altairclient.modules.mods.Hacks.register();
        com.altairclient.modules.mods.RenderHacks.register();
        com.altairclient.modules.mods.WorldHacks.register();
        com.altairclient.modules.mods.ExtraHacks.register();
        com.altairclient.modules.mods.ExtraHacks2.register();
        com.altairclient.client.TitleBackground.register();
        AltairHud.register();
        NametagHandler.register();
        ChatBubbleHandler.register();
        AutoGG.register();
        ChatTimestamps.register();
        ClearWater.register();
        FastGame.register();
        BlockOutline.register();
        TimeChanger.register();
        WeatherChanger.register();
        PvpTextures.register();
        MovementTextures.register();
        BuildTextures.register();
        com.altairclient.modules.mods.AltairSky.register();
        com.altairclient.modules.mods.NoAnimation.register();
        com.altairclient.modules.mods.UltraOptimizer.register();
        com.altairclient.modules.mods.DynamicIsland.register();
        com.altairclient.modules.textures.AltairRain.register();
        ExtraTextures.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("Altair Client setup completed.");
    }
}
