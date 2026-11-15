package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;

/** Hace una captura de pantalla del juego (se guarda en la carpeta "screenshots" de Minecraft). */
public final class ScreenshotModule {

    /** Ticks de espera antes de capturar, para que el menú de módulos se cierre y no salga en la imagen. */
    private static int pendingTicks = 0;

    private ScreenshotModule() {
    }

    public static void request() {
        pendingTicks = 3;
    }

    /** Se llama una vez por tick del cliente. */
    public static void tick(Minecraft mc) {
        if (pendingTicks <= 0 || mc.player == null) {
            return;
        }
        if (mc.gui.screen() != null) {
            return; // se espera a que no haya ningún menú abierto
        }

        pendingTicks--;
        if (pendingTicks == 0) {
            Screenshot.grab(
                    mc.gameDirectory,
                    mc.getMainRenderTarget(),
                    message -> mc.execute(() -> mc.gui.getChat().addMessage(message))
            );
        }
    }
}
