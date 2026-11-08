package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.WorldInfo;

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
    private static final int CYAN = 0xFF8BE9FD;
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
    private int scrollOffset = 0;

    public AltairScreen() {
        super(Component.literal("Altair Client"));

        // 1. FPS Display
        ModuleEntry fpsEntry = new ModuleEntry(
                "FPS Display",
                "Muestra los FPS actuales en pantalla",
                () -> FPSDisplay.getInstance().toggle()
        );
        fpsEntry.enabled = FPSDisplay.getInstance().isEnabled();
        modules.add(fpsEntry);

        // 2. CPS Counter
        ModuleEntry cpsEntry = new ModuleEntry(
                "CPS Counter",
                "Contador de clics por segundo (LMB | RMB)",
                () -> CPSCounter.getInstance().toggle()
        );
        cpsEntry.enabled = CPSCounter.getInstance().isEnabled();
        modules.add(cpsEntry);

        // 3. Armor HUD
        ModuleEntry armorEntry = new ModuleEntry(
                "Armor HUD",
                "Estado de piezas de armadura y durabilidad",
                () -> ArmorHUD.getInstance().toggle()
        );
        armorEntry.enabled = ArmorHUD.getInstance().isEnabled();
        modules.add(armorEntry);

        // 4. Keystrokes
        ModuleEntry keyEntry = new ModuleEntry(
                "Keystrokes",
                "Muestra teclas WASD, clics y espacio en vivo",
                () -> Keystrokes.getInstance().toggle()
        );
        keyEntry.enabled = Keystrokes.getInstance().isEnabled();
        modules.add(keyEntry);

        // 5. Potion Status
        ModuleEntry potionEntry = new ModuleEntry(
                "Potion Status",
                "Efectos de pociones activos y tiempo restante",
                () -> PotionStatus.getInstance().toggle()
        );
        potionEntry.enabled = PotionStatus.getInstance().isEnabled();
        modules.add(potionEntry);

        // 6. World Info
        ModuleEntry worldEntry = new ModuleEntry(
                "World Info",
                "Coordenadas XYZ, direcci\u00f3n, ping y hora",
                () -> WorldInfo.getInstance().toggle()
        );
        worldEntry.enabled = WorldInfo.getInstance().isEnabled();
        modules.add(worldEntry);

        // 7. Smooth Zoom
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

        panelWidth = Math.min(540, width - 24);
        panelHeight = Math.min(360, height - 24);
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

        // Fondo oscuro semi-transparente
        graphics.fill(0, 0, width, height, 0xB9080B18);

        // Panel principal
        graphics.fill(
                panelX, panelY,
                panelX + panelWidth, panelY + panelHeight,
                BACKGROUND
        );

        // Barra superior degradada
        graphics.fill(
                panelX, panelY,
                panelX + panelWidth, panelY + 3,
                PURPLE
        );

        // Barra lateral
        int sidebarWidth = 138;
        graphics.fill(
                panelX, panelY + 3,
                panelX + sidebarWidth, panelY + panelHeight,
                PANEL
        );

        // Identidad del cliente
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

        // Separador
        graphics.fill(
                panelX + 14, panelY + 62,
                panelX + sidebarWidth - 14, panelY + 63,
                0xFF303957
        );

        // Sección MODULES
        graphics.fill(
                panelX + 12, panelY + 72,
                panelX + 12 + 114, panelY + 95,
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

        // Botón EDITAR HUD en la barra lateral
        int btnY = panelY + panelHeight - 65;
        int btnX = panelX + 12;
        int btnW = 114;
        int btnH = 22;

        boolean isBtnHovered = mouseX >= btnX && mouseX <= btnX + btnW
                && mouseY >= btnY && mouseY <= btnY + btnH;

        int btnBg = isBtnHovered ? 0xFF352B66 : 0xFF201B40;
        int btnBorder = isBtnHovered ? CYAN : PURPLE;

        graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnBg);
        graphics.fill(btnX, btnY, btnX + btnW, btnY + 1, btnBorder);
        graphics.fill(btnX, btnY + btnH - 1, btnX + btnW, btnY + btnH, btnBorder);
        graphics.fill(btnX, btnY + 1, btnX + 1, btnY + btnH, btnBorder);
        graphics.fill(btnX + btnW - 1, btnY, btnX + btnW, btnY + btnH, btnBorder);

        String btnText = "\u2726 EDITAR HUD";
        int btw = font.width(btnText);
        graphics.text(font, btnText, btnX + (btnW - btw) / 2, btnY + 7, WHITE, false);

        // Keybind hint
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

        // Contenido principal
        int contentX = panelX + sidebarWidth + 18;
        int contentY = panelY + 16;

        graphics.text(
                font, "M O D U L E S",
                contentX, contentY,
                BLUE, false
        );

        graphics.text(
                font, "Activa o desactiva los m\u00f3dulos",
                contentX, contentY + 14,
                MUTED, false
        );

        int cardWidth = panelWidth - sidebarWidth - 35;
        int cardY = contentY + 30;
        int cardStep = 40;
        int visibleBottom = panelY + panelHeight - 12;

        // Renderizado de tarjetas con scroll suave
        for (int i = 0; i < modules.size(); i++) {
            ModuleEntry entry = modules.get(i);
            int y = cardY + i * cardStep - scrollOffset;

            // Descartar si está fuera del área visible
            if (y + 32 < cardY || y > visibleBottom) {
                continue;
            }

            // Fondo de la tarjeta
            graphics.fill(
                    contentX - 5, y - 4,
                    contentX + cardWidth, y + 31,
                    PANEL_LIGHT
            );

            // Borde izquierdo coloreado
            int accentColor = entry.enabled ? GREEN : 0xFF424B68;
            graphics.fill(
                    contentX - 5, y - 4,
                    contentX - 2, y + 31,
                    accentColor
            );

            // Nombre
            graphics.text(
                    font, entry.name,
                    contentX + 5, y,
                    WHITE, false
            );

            // Descripción
            graphics.text(
                    font, entry.description,
                    contentX + 5, y + 13,
                    MUTED, false
            );

            // Toggle switch
            int toggleColor = entry.enabled ? PURPLE : 0xFF424B68;
            int toggleX = contentX + cardWidth - 35;

            graphics.fill(
                    toggleX, y + 4,
                    toggleX + 25, y + 16,
                    toggleColor
            );

            int knobX = entry.enabled ? toggleX + 14 : toggleX + 2;
            graphics.fill(
                    knobX, y + 6,
                    knobX + 9, y + 14,
                    WHITE
            );
        }

        // Previsualizar overlays en pantalla si están activos
        FPSDisplay.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        int maxScroll = Math.max(0, modules.size() * 40 - (panelHeight - 65));
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * 20)));
        return true;
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

        // Clic en botón EDITAR HUD
        int btnY = panelY + panelHeight - 65;
        int btnX = panelX + 12;
        int btnW = 114;
        int btnH = 22;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
            if (minecraft != null) {
                minecraft.gui.setScreen(new AltairHudEditorScreen(this));
                return true;
            }
        }

        int sidebarWidth = 138;
        int contentX = panelX + sidebarWidth + 18;
        int cardY = panelY + 16 + 14 + 30;
        int cardWidth = panelWidth - sidebarWidth - 35;
        int cardStep = 40;

        for (int i = 0; i < modules.size(); i++) {
            int y = cardY + i * cardStep - scrollOffset;

            if (mouseX >= contentX - 5
                    && mouseX <= contentX + cardWidth
                    && mouseY >= y - 4
                    && mouseY <= y + 31) {
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
