package com.altairclient.client;

import com.altairclient.modules.mods.FPSDisplay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class AltairScreen extends Screen {

    private static final int BACKGROUND = 0xF20A0D20;
    private static final int PANEL = 0xFF11172F;
    private static final int PANEL_LIGHT = 0xFF1A2242;
    private static final int PURPLE = 0xFF9B7BFF;
    private static final int BLUE = 0xFF62C8FF;
    private static final int WHITE = 0xFFF2F3FF;
    private static final int MUTED = 0xFF929DBD;
    private static final int GREEN = 0xFF6BFFB8;

    private static final class ModuleEntry {
        final String name;
        final String description;
        boolean enabled;
        final Runnable onToggle;

        ModuleEntry(String name, String description, Runnable onToggle) {
            this.name = name;
            this.description = description;
            this.onToggle = onToggle;
        }
    }

    private final List<ModuleEntry> modules = new ArrayList<>();

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public AltairScreen() {
        super(Component.literal("Altair Client"));

        ModuleEntry fpsEntry = new ModuleEntry(
                "FPS Display",
                "Muestra los FPS en pantalla",
                () -> FPSDisplay.getInstance().toggle()
        );
        fpsEntry.enabled = FPSDisplay.getInstance().isEnabled();
        modules.add(fpsEntry);

        ModuleEntry zoomEntry = new ModuleEntry(
                "Smooth Zoom",
                "Zoom suave con la tecla C",
                null
        );
        zoomEntry.enabled = true;
        modules.add(zoomEntry);
    }

    @Override
    protected void init() {
        super.init();

        panelWidth = Math.min(520, width - 24);
        panelHeight = Math.min(330, height - 24);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.fill(0, 0, width, height, 0xB9080B18);

        graphics.fill(
                panelX, panelY,
                panelX + panelWidth, panelY + panelHeight,
                BACKGROUND
        );

        graphics.fill(
                panelX, panelY,
                panelX + panelWidth, panelY + 3,
                PURPLE
        );

        int sidebarWidth = 138;
        graphics.fill(
                panelX, panelY + 3,
                panelX + sidebarWidth, panelY + panelHeight,
                PANEL
        );

        graphics.text(
                font, "\u2726 A L T A I R \u2726",
                panelX + 14, panelY + 18,
                WHITE, false
        );

        graphics.text(
                font, "C O S M I C",
                panelX + 21, panelY + 33,
                PURPLE, false
        );

        graphics.text(
                font, "C L I E N T",
                panelX + 24, panelY + 46,
                MUTED, false
        );

        graphics.fill(
                panelX + 14, panelY + 62,
                panelX + sidebarWidth - 14, panelY + 63,
                0xFF303957
        );

        graphics.fill(
                panelX + 12, panelY + 72,
                panelX + 12 + 112, panelY + 95,
                0xFF29264F
        );
        graphics.fill(
                panelX + 12, panelY + 72,
                panelX + 15, panelY + 95,
                PURPLE
        );
        graphics.text(
                font, "M O D U L E S",
                panelX + 22, panelY + 80,
                WHITE, false
        );

        graphics.fill(
                panelX + 12, panelY + panelHeight - 35,
                panelX + sidebarWidth - 12, panelY + panelHeight - 34,
                0xFF303957
        );
        graphics.text(
                font, "Right Shift",
                panelX + 17, panelY + panelHeight - 24,
                MUTED, false
        );

        int contentX = panelX + sidebarWidth + 18;
        int contentY = panelY + 19;

        graphics.text(
                font, "M O D U L E S",
                contentX, contentY,
                BLUE, false
        );

        graphics.text(
                font, "Activa o desactiva los m\u00f3dulos",
                contentX, contentY + 16,
                MUTED, false
        );

        int cardWidth = panelWidth - sidebarWidth - 35;
        int cardY = contentY + 40;

        for (int i = 0; i < modules.size(); i++) {
            ModuleEntry entry = modules.get(i);
            int y = cardY + i * 50;

            graphics.fill(
                    contentX - 5, y - 5,
                    contentX + cardWidth, y + 36,
                    PANEL_LIGHT
            );

            int accentColor = entry.enabled ? GREEN : 0xFF424B68;
            graphics.fill(
                    contentX - 5, y - 5,
                    contentX - 2, y + 36,
                    accentColor
            );

            graphics.text(
                    font, entry.name,
                    contentX + 5, y + 2,
                    WHITE, false
            );

            graphics.text(
                    font, entry.description,
                    contentX + 5, y + 16,
                    MUTED, false
            );

            int toggleColor = entry.enabled ? PURPLE : 0xFF424B68;
            int toggleX = contentX + cardWidth - 35;

            graphics.fill(
                    toggleX, y + 7,
                    toggleX + 25, y + 19,
                    toggleColor
            );

            int knobX = entry.enabled ? toggleX + 14 : toggleX + 2;
            graphics.fill(
                    knobX, y + 9,
                    knobX + 9, y + 17,
                    WHITE
            );
        }

        FPSDisplay.getInstance().render(graphics);
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        double mouseX = event.x();
        double mouseY = event.y();

        int sidebarWidth = 138;
        int contentX = panelX + sidebarWidth + 18;
        int contentY = panelY + 19 + 16 + 40;
        int cardWidth = panelWidth - sidebarWidth - 35;

        for (int i = 0; i < modules.size(); i++) {
            int y = contentY + i * 50;

            if (mouseX >= contentX - 5
                    && mouseX <= contentX + cardWidth
                    && mouseY >= y - 5
                    && mouseY <= y + 36) {
                ModuleEntry entry = modules.get(i);
                entry.enabled = !entry.enabled;

                if (entry.onToggle != null) {
                    entry.onToggle.run();
                }

                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
