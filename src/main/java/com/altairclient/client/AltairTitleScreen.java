
package com.altairclient.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;

public final class AltairTitleScreen {

    private AltairTitleScreen() {}

    public static void register() {
        ScreenEvent.Render.Post.BUS.addListener(
                AltairTitleScreen::renderEditionLabel
        );
    }

    private static void renderEditionLabel(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof TitleScreen screen)) {
            return;
        }

        GuiGraphicsExtractor graphics = event.getGuiGraphics();

        String label = "ALTAIR EDITION";

        int textWidth = Minecraft.getInstance().font.width(label);
        int centerX = screen.width / 2;
        int y = screen.height / 4 + 42;

        graphics.fill(
                centerX - textWidth / 2 - 3,
                y - 2,
                centerX + textWidth / 2 + 3,
                y + 10,
                0xD0071126
        );

        graphics.text(
                Minecraft.getInstance().font,
                Component.literal(label),
                centerX - textWidth / 2,
                y,
                0xFFE6EDF9,
                true
        );
    }
}
