package com.altairclient.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
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

    private enum Section {
        MODS,
        HACKS
    }

    private static final class ModuleEntry {
        final String name;
        final String description;
        boolean enabled;

        ModuleEntry(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private final List<ModuleEntry> mods = new ArrayList<>();
    private final List<ModuleEntry> hacks = new ArrayList<>();

    private Section selectedSection = Section.MODS;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public AltairScreen() {
        super(Component.literal("Altair Client"));

        mods.add(new ModuleEntry("Keystrokes", "Visualiza las teclas"));
        mods.add(new ModuleEntry("Coordinates", "Muestra tus coordenadas"));
        mods.add(new ModuleEntry("FPS Display", "Muestra los FPS"));
        mods.add(new ModuleEntry("Clock", "Reloj del cliente"));
        mods.add(new ModuleEntry("Armor HUD", "Estado de tu armadura"));

        // Entradas de demostración para pruebas locales.
        hacks.add(new ModuleEntry("Practice Mode", "Opciones para pruebas locales"));
        hacks.add(new ModuleEntry("Debug Overlay", "Panel de depuración visual"));
        hacks.add(new ModuleEntry("Test Module", "Módulo de demostración"));
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

        // Fondo oscuro.
        graphics.fill(0, 0, width, height, 0xB9080B18);

        // Panel principal.
        graphics.fill(
                panelX, panelY,
                panelX + panelWidth, panelY + panelHeight,
                BACKGROUND
        );

        // Barra superior.
        graphics.fill(
                panelX, panelY,
                panelX + panelWidth, panelY + 3,
                PURPLE
        );

        // Barra lateral.
        int sidebarWidth = 138;
        graphics.fill(
                panelX, panelY + 3,
                panelX + sidebarWidth, panelY + panelHeight,
                PANEL
        );

        // Identidad del cliente.
        graphics.text(
                font, "ALTAIR",
                panelX + 17, panelY + 18,
                WHITE, false
        );

        graphics.text(
                font, "CLIENT",
                panelX + 17, panelY + 32,
                PURPLE, false
        );

        graphics.text(
                font, "COSMIC EDITION",
                panelX + 17, panelY + 52,
                MUTED, false
        );

        // Navegación.
        drawSection(
                graphics, "MODS",
                panelX + 12, panelY + 82,
                selectedSection == Section.MODS
        );

        drawSection(
                graphics, "HACKS",
                panelX + 12, panelY + 113,
                selectedSection == Section.HACKS
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

        // Área de contenido.
        int contentX = panelX + sidebarWidth + 18;
        int contentY = panelY + 19;

        graphics.text(
                font,
                selectedSection == Section.MODS
                        ? "MODS / CLIENT"
                        : "HACKS / LAB",
                contentX, contentY,
                BLUE, false
        );

        graphics.text(
                font,
                "Selecciona un modulo para activarlo",
                contentX, contentY + 16,
                MUTED, false
        );

        List<ModuleEntry> entries =
                selectedSection == Section.MODS ? mods : hacks;

        int cardWidth = panelWidth - sidebarWidth - 35;
        int cardY = contentY + 35;

        for (int i = 0; i < entries.size(); i++) {
            ModuleEntry entry = entries.get(i);
            int y = cardY + i * 43;

            graphics.fill(
                    contentX - 5, y - 5,
                    contentX + cardWidth, y + 31,
                    PANEL_LIGHT
            );

            graphics.text(
                    font, entry.name,
                    contentX + 5, y + 1,
                    WHITE, false
            );

            graphics.text(
                    font, entry.description,
                    contentX + 5, y + 14,
                    MUTED, false
            );

            int toggleColor = entry.enabled ? PURPLE : 0xFF424B68;
            int toggleX = contentX + cardWidth - 35;

            graphics.fill(
                    toggleX, y + 5,
                    toggleX + 25, y + 17,
                    toggleColor
            );

            int knobX = entry.enabled ? toggleX + 14 : toggleX + 2;

            graphics.fill(
                    knobX, y + 7,
                    knobX + 9, y + 15,
                    WHITE
            );
        }
    }

    private void drawSection(
            GuiGraphicsExtractor graphics,
            String label,
            int x,
            int y,
            boolean selected
    ) {
        if (selected) {
            graphics.fill(
                    x, y - 5,
                    x + 112, y + 18,
                    0xFF29264F
            );

            graphics.fill(
                    x, y - 5,
                    x + 3, y + 18,
                    PURPLE
            );
        }

        graphics.text(
                font, label,
                x + 10, y + 3,
                selected ? WHITE : MUTED,
                false
        );
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int sidebarWidth = 138;
        int sidebarX = panelX + 12;

        if (mouseX >= sidebarX && mouseX <= panelX + sidebarWidth) {
            if (mouseY >= panelY + 77 && mouseY <= panelY + 105) {
                selectedSection = Section.MODS;
                return true;
            }

            if (mouseY >= panelY + 108 && mouseY <= panelY + 136) {
                selectedSection = Section.HACKS;
                return true;
            }
        }

        int contentX = panelX + sidebarWidth + 18;
        int cardY = panelY + 19 + 16 + 35;
        int cardWidth = panelWidth - sidebarWidth - 35;

        List<ModuleEntry> entries =
                selectedSection == Section.MODS ? mods : hacks;

        for (int i = 0; i < entries.size(); i++) {
            int y = cardY + i * 43;

            if (mouseX >= contentX - 5
                    && mouseX <= contentX + cardWidth
                    && mouseY >= y - 5
                    && mouseY <= y + 31) {
                entries.get(i).enabled = !entries.get(i).enabled;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
