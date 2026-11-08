package com.altairclient.client;
import com.altairclient.modules.mods.FPSDisplay;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class AltairHudLayerRegistrar {
    @SubscribeEvent
    public static void onAddGuiLayers(AddGuiOverlayLayersEvent event) {
        ResourceLocation layerId = ResourceLocation.fromNamespaceAndPath(
                "altairclient", "fps_display"
        );
        event.getLayeredDraw().addBefore(
                LayeredDraw.VanillaLayers.CHAT,
                layerId,
                (graphics, deltaTracker) ->
                        FPSDisplay.getInstance().render(graphics)
        );
    }
}
