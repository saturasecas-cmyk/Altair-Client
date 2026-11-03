
package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class FPSDisplay {

    private boolean enabled = true;

    public String getName() {
        return "FPS Display";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void render(GuiGraphics guiGraphics) {
        if (!enabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        int fps = minecraft.getFps();

        guiGraphics.drawString(
            minecraft.font,
            Component.literal("FPS: " + fps),
            8,
            8,
            0xFFFFFF,
            true
        );
    }
}
