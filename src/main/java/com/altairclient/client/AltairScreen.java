package com.altairclient.client;

import com.altairclient.modules.mods.FPSDisplay;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class AltairScreen extends Screen {

    // Paleta de colores cósmica y refinada
    private static final int PANEL = 0xFF0E1326;            // Fondo del panel
    private static final int CARD_BG = 0xFF161C36;          // Fondo de tarjetas
    private static final int CARD_BORDER = 0xFF242E54;      // Borde sutil
    private static final int VIOLET_ACCENT = 0xFFA580FF;    // Violeta nebulosa brillante
    private static final int CYAN_ACCENT = 0xFF5CE1E6;      // Azul eléctrico estelar
    private static final int TEXT_WHITE = 0xFFF5F7FD;       // Blanco elegante
    private static final int TEXT_MUTED = 0xFF8C98B8;       // Gris lavanda suave
    private static final int TOGGLE_OFF = 0xFF28304D;       // Interruptor apagado

    private static final class ModuleEntry {
        final String name;
        final String description;
        final String shortcut;
        boolean enabled;

        ModuleEntry(String name, String description, String shortcut, boolean enabled) {
            this.name = name;
            this.description = description;
            this.shortcut = shortcut;
            this.enabled = enabled;
        }
    }

    private final List<ModuleEntry> mods = new ArrayList<>();

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public AltairScreen() {
        super(Component.literal("Altair Client"));

        // Solo módulos reales que existen en el código
        mods.add(new ModuleEntry(
                "FPS Display",
                "Muestra los fotogramas en tiempo real en la pantalla",
                "HUD",
                FPSDisplay.getInstance().isEnabled()
        ));

        mods.add(new ModuleEntry(
                "Smooth Zoom",
                "Acercamiento dinámico cinemático de cámara",
                "Tecla [ C ]",
                true
        ));
    }

    @Override
    protected void init() {
        super.init();

        panelWidth = Math.min(460, width - 24);
        panelHeight = Math.min(270, height - 24);
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

        // 1. Fondo sombreado cinemático
        graphics.fill(0, 0, width, height, 0xB004060E);

        // 2. Panel con contorno elegante
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, CARD_BORDER);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL);

        // 3. Barra superior violeta neón
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 2, VIOLET_ACCENT);

        // 4. Cabecera con tipografía estilizada
        graphics.text(font, "✦  A L T A I R  ✦", panelX + 24, panelY + 16, VIOLET_ACCENT, false);
        graphics.text(font, "C O S M I C   C L I E N T", panelX + 24, panelY + 29, TEXT_MUTED, false);

        // Línea divisoria elegante
        graphics.fill(panelX + 24, panelY + 44, panelX + panelWidth - 24, panelY + 45, 0xFF1C2442);

        // 5. Título de sección
        graphics.text(font, "M Ó D U L O S   D E L   C L I E N T E", panelX + 24, panelY + 54, CYAN_ACCENT, false);

        // 6. Tarjetas de módulos reales
        int startY = panelY + 70;
        int cardHeight = 44;
        int cardWidth = panelWidth - 48;

        for (int i = 0; i < mods.size(); i++) {
            ModuleEntry entry = mods.get(i);
            int y = startY + (i * (cardHeight + 8));

            // Borde y fondo de la tarjeta
            graphics.fill(panelX + 23, y - 1, panelX + 24 + cardWidth + 1, y + cardHeight + 1, CARD_BORDER);
            graphics.fill(panelX + 24, y, panelX + 24 + cardWidth, y + cardHeight, CARD_BG);

            // Indicador activo en el lateral izquierdo
            int indicatorColor = entry.enabled ? VIOLET_ACCENT : 0xFF353E61;
            graphics.fill(panelX + 24, y, panelX + 27, y + cardHeight, indicatorColor);

            // Título del módulo y badge de atajo
            graphics.text(font, entry.name, panelX + 36, y + 8, TEXT_WHITE, false);
            graphics.text(font, entry.shortcut, panelX + 38 + font.width(entry.name) + 8, y + 8, CYAN_ACCENT, false);

            // Descripción elegante
            graphics.text(font, entry.description, panelX + 36, y + 24, TEXT_MUTED, false);

            // Interruptor (Toggle en pastilla)
            int toggleWidth = 32;
            int toggleHeight = 16;
            int toggleX = panelX + 24 + cardWidth - toggleWidth - 14;
            int toggleY = y + (cardHeight - toggleHeight) / 2;

            int toggleColor = entry.enabled ? VIOLET_ACCENT : TOGGLE_OFF;
            graphics.fill(toggleX, toggleY, toggleX + toggleWidth, toggleY + toggleHeight, toggleColor);

            int knobX = entry.enabled ? toggleX + toggleWidth - 13 : toggleX + 3;
            graphics.fill(knobX, toggleY + 3, knobX + 10, toggleY + toggleHeight - 3, TEXT_WHITE);
        }

        // 7. Pie con atajo para salir
        String closeText = "✦ Pulsa [ Right Shift ] o [ Esc ] para cerrar ✦";
        graphics.text(font, closeText, panelX + (panelWidth / 2) - (font.width(closeText) / 2), panelY + panelHeight - 16, TEXT_MUTED, false);

        // Dibuja también el indicador de FPS si está activo mientras tienes el menú abierto
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

        int startY = panelY + 70;
        int cardHeight = 44;
        int cardWidth = panelWidth - 48;

        for (int i = 0; i < mods.size(); i++) {
            int y = startY + (i * (cardHeight + 8));

            if (mouseX >= panelX + 24
                    && mouseX <= panelX + 24 + cardWidth
                    && mouseY >= y
                    && mouseY <= y + cardHeight) {
                ModuleEntry entry = mods.get(i);
                entry.enabled = !entry.enabled;

                if ("FPS Display".equals(entry.name)) {
                    FPSDisplay.getInstance().setEnabled(entry.enabled);
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
