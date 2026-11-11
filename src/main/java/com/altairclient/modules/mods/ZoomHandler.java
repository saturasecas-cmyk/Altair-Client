
package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "altairclient",
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = net.minecraftforge.api.distmarker.Dist.CLIENT
)
public class ZoomHandler {

    private static final float ZOOM_FACTOR = 0.25F;

    private static boolean enabled = true;
    private static boolean zoomKeyDown = false;

    // Consultar si el Zoom está activado desde el menú.
    public static boolean isEnabled() {
        return enabled;
    }

    // Activar o desactivar el Zoom desde Altair.
    public static void setEnabled(boolean value) {
        enabled = value;

        if (!enabled) {
            zoomKeyDown = false;
        }
    }

    // Estado de la tecla de Zoom.
    public static void setZoomKeyDown(boolean value) {
        zoomKeyDown = value;
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {

        Minecraft minecraft = Minecraft.getInstance();

        // No aplicar Zoom si está desactivado o hay una pantalla abierta.
        if (!enabled || !zoomKeyDown || minecraft.screen != null) {
            return;
        }

        event.setNewFovModifier(
                event.getFovModifier() * ZOOM_FACTOR
        );
    }
}
