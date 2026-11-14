package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.ComboCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.HitIndicator;
import com.altairclient.modules.mods.HitboxDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.ReachDisplay;
import com.altairclient.modules.mods.WorldInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class AltairScreen extends Screen {

    // Paleta de colores base para el layout (sin sobredecoración)
    private static final int OVERLAY_BG = 0xB0080B14;
    private static final int MAIN_PANEL_BG = 0xF00D1224;
    private static final int HEADER_BG = 0xF511172E;
    private static final int SIDEBAR_BG = 0xF00F1429;
    private static final int RIGHT_PANEL_BG = 0xEE141B33;
    private static final int CARD_BG = 0xFF18203B;
    private static final int CARD_HOVER_BG = 0xFF212A4D;
    private static final int CARD_SELECTED_BG = 0xFF27325C;
    private static final int BORDER_COLOR = 0xFF2A3459;

    private static final int PURPLE = 0xFF9B7BFF;
    private static final int CYAN = 0xFF62C8FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFF929DBD;
    private static final int GREEN = 0xFF5DFFA8;
    private static final int RED = 0xFFFF5C77;

    public enum Category {
        COMBAT("Combat", "⚔"),
        RENDER("Render", "🖥"),
        PLAYER("Player", "👤"),
        WORLD("World", "🌐"),
        MISC("Misc", "✨");

        final String displayName;
        final String icon;

        Category(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
    }

    private static final class ModuleEntry {
        final String name;
        final String shortDesc;
        final String fullDesc;
        final Category category;
        final String icon;
        final Runnable onToggle;
        final java.util.function.BooleanSupplier isEnabledSupplier;

        ModuleEntry(
                String name,
                String shortDesc,
                String fullDesc,
                Category category,
                String icon,
                Runnable onToggle,
                java.util.function.BooleanSupplier isEnabledSupplier
        ) {
            this.name = name;
            this.shortDesc = shortDesc;
            this.fullDesc = fullDesc;
            this.category = category;
            this.icon = icon;
            this.onToggle = onToggle;
            this.isEnabledSupplier = isEnabledSupplier;
        }

        boolean isEnabled() {
            return isEnabledSupplier != null ? isEnabledSupplier.getAsBoolean() : false;
        }

        void toggle() {
            if (onToggle != null) {
                onToggle.run();
            }
        }
    }

    private final List<ModuleEntry> allModules = new ArrayList<>();
    private Category currentCategory = Category.COMBAT;
    private ModuleEntry selectedModule = null;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public AltairScreen() {
        super(Component.literal("Altair Client"));

        // ==================== COMBAT ====================
        allModules.add(new ModuleEntry(
                "Combo Counter",
                "Golpes consecutivos",
                "Cuenta los golpes consecutivos que aciertas a tus oponentes en combate. Se reinicia automáticamente si pasan 2.5s o si recibes daño.",
                Category.COMBAT,
                "⚔",
                () -> ComboCounter.getInstance().toggle(),
                () -> ComboCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Reach Display",
                "Distancia de golpe",
                "Muestra con precisión matemática la distancia exacta en metros a la que conectas cada ataque sobre la caja de colisión del objetivo.",
                Category.COMBAT,
                "⌖",
                () -> ReachDisplay.getInstance().toggle(),
                () -> ReachDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Hit Indicator",
                "Impacto en mira",
                "Dibuja un indicador diagonal (hitmarker) alrededor del punto de mira al acertar un golpe a un enemigo con animación rápida.",
                Category.COMBAT,
                "✦",
                () -> HitIndicator.getInstance().toggle(),
                () -> HitIndicator.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "CPS Counter",
                "Clics por segundo",
                "Registra y muestra en tiempo real la velocidad de pulsación para el botón izquierdo y derecho del ratón (LMB | RMB).",
                Category.COMBAT,
                "🖱",
                () -> CPSCounter.getInstance().toggle(),
                () -> CPSCounter.getInstance().isEnabled()
        ));

        // ==================== RENDER ====================
        allModules.add(new ModuleEntry(
                "FPS Display",
                "Contador de FPS",
                "Muestra la tasa de cuadros por segundo de Minecraft en pantalla con actualización fluida y posición personalizable.",
                Category.RENDER,
                "🖥",
                () -> FPSDisplay.getInstance().toggle(),
                () -> FPSDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Hitbox Display",
                "Colisiones visibles",
                "Muestra de forma gráfica las cajas de colisión y líneas de visión de todas las entidades en el mundo con 0 impacto en rendimiento.",
                Category.RENDER,
                "📦",
                () -> HitboxDisplay.getInstance().toggle(),
                () -> HitboxDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Armor HUD",
                "Durabilidad armadura",
                "Muestra las piezas de armadura equipadas (casco, peto, pantalones y botas) junto a su porcentaje y barra de durabilidad.",
                Category.RENDER,
                "🛡",
                () -> ArmorHUD.getInstance().toggle(),
                () -> ArmorHUD.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Keystrokes",
                "Teclas en vivo",
                "Representación visual interactiva de las teclas WASD, clics izquierdo y derecho y la barra espaciadora en la pantalla.",
                Category.RENDER,
                "⌨",
                () -> Keystrokes.getInstance().toggle(),
                () -> Keystrokes.getInstance().isEnabled()
        ));

        // ==================== PLAYER ====================
        allModules.add(new ModuleEntry(
                "Potion Status",
                "Efectos activos",
                "Muestra la lista de pociones y efectos que tiene tu jugador, indicando el nombre, nivel y tiempo restante formateado en MM:SS.",
                Category.PLAYER,
                "🧪",
                () -> PotionStatus.getInstance().toggle(),
                () -> PotionStatus.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Smooth Zoom",
                "Zoom cinematográfico",
                "Permite acercar suavemente la vista del jugador al mantener pulsada la tecla C con interpolación dinámica del campo de visión (FOV).",
                Category.PLAYER,
                "🔍",
                null,
                () -> true
        ));

        // ==================== WORLD ====================
        allModules.add(new ModuleEntry(
                "World Info",
                "Info del entorno",
                "Panel con coordenadas XYZ, dirección cardinal con grados, ping al servidor y reloj en hora local del sistema.",
                Category.WORLD,
                "🌐",
                () -> WorldInfo.getInstance().toggle(),
                () -> WorldInfo.getInstance().isEnabled()
        ));

        if (!allModules.isEmpty()) {
            selectedModule = allModules.get(0);
        }
    }

    @Override
    protected void init() {
        super.init();

        panelWidth = Math.min(660, width - 20);
        panelHeight = Math.min(380, height - 20);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
    }

    private List<ModuleEntry> getModulesForCategory(Category cat) {
        List<ModuleEntry> list = new ArrayList<>();
        for (ModuleEntry m : allModules) {
            if (m.category == cat) {
                list.add(m);
            }
        }
        return list;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // 1. Fondo oscuro global
        graphics.fill(0, 0, width, height, OVERLAY_BG);

        // 2. Marco contenedor principal
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, MAIN_PANEL_BG);

        // Bordes del contenedor
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, BORDER_COLOR);
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, BORDER_COLOR);
        graphics.fill(panelX, panelY + 1, panelX + 1, panelY + panelHeight, BORDER_COLOR);
        graphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, BORDER_COLOR);

        // 3. Barra Superior (Header)
        int headerHeight = 36;
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + headerHeight, HEADER_BG);
        graphics.fill(panelX, panelY + headerHeight, panelX + panelWidth, panelY + headerHeight + 1, BORDER_COLOR);

        // Logo e Identidad (Izquierda del Header)
        graphics.text(font, "✦", panelX + 14, panelY + 12, CYAN, false);
        graphics.text(font, "Altair Client", panelX + 28, panelY + 13, WHITE, false);

        // Barra de búsqueda simulada (Centro del Header)
        int searchWidth = 220;
        int searchHeight = 20;
        int searchX = panelX + (panelWidth - searchWidth) / 2 - 20;
        int searchY = panelY + 8;

        graphics.fill(searchX, searchY, searchX + searchWidth, searchY + searchHeight, 0xFF182038);
        graphics.fill(searchX, searchY, searchX + searchWidth, searchY + 1, BORDER_COLOR);
        graphics.fill(searchX, searchY + searchHeight - 1, searchX + searchWidth, searchY + searchHeight, BORDER_COLOR);
        graphics.fill(searchX, searchY + 1, searchX + 1, searchY + searchHeight, BORDER_COLOR);
        graphics.fill(searchX + searchWidth - 1, searchY, searchX + searchWidth, searchY + searchHeight, BORDER_COLOR);

        graphics.text(font, "🔍  Buscar módulos...", searchX + 8, searchY + 6, MUTED, false);

        // Botones de ventana (Derecha del Header)
        int winBtnY = panelY + 10;
        int winCloseX = panelX + panelWidth - 24;
        boolean closeHover = mouseX >= winCloseX && mouseX <= winCloseX + 14 && mouseY >= winBtnY && mouseY <= winBtnY + 14;
        graphics.text(font, "—", winCloseX - 32, winBtnY + 2, MUTED, false);
        graphics.text(font, "□", winCloseX - 16, winBtnY + 2, MUTED, false);
        graphics.text(font, "✕", winCloseX, winBtnY + 2, closeHover ? RED : MUTED, false);

        // ==================== 4. SIDEBAR IZQUIERDA ====================
        int sidebarWidth = 110;
        int contentY = panelY + headerHeight + 1;

        graphics.fill(panelX, contentY, panelX + sidebarWidth, panelY + panelHeight, SIDEBAR_BG);
        graphics.fill(panelX + sidebarWidth, contentY, panelX + sidebarWidth + 1, panelY + panelHeight, BORDER_COLOR);

        // Lista de Categorías
        Category[] cats = Category.values();
        int catBtnY = contentY + 10;
        int catBtnH = 26;

        for (Category cat : cats) {
            boolean isSelected = (cat == currentCategory);
            boolean isHovered = mouseX >= panelX + 8 && mouseX <= panelX + sidebarWidth - 8
                    && mouseY >= catBtnY && mouseY <= catBtnY + catBtnH;

            int bg = isSelected ? 0xFF272952 : (isHovered ? 0xFF1B2240 : 0x00000000);
            if (bg != 0) {
                graphics.fill(panelX + 8, catBtnY, panelX + sidebarWidth - 8, catBtnY + catBtnH, bg);
            }

            if (isSelected) {
                graphics.fill(panelX + 8, catBtnY, panelX + 11, catBtnY + catBtnH, PURPLE);
            }

            int textColor = isSelected ? WHITE : (isHovered ? CYAN : MUTED);
            graphics.text(font, cat.icon + "  " + cat.displayName, panelX + 16, catBtnY + 9, textColor, false);

            catBtnY += catBtnH + 4;
        }

        // Botón inferior de HUD Editor
        int hudBtnY = panelY + panelHeight - 40;
        int hudBtnX = panelX + 8;
        int hudBtnW = sidebarWidth - 16;
        int hudBtnH = 24;

        boolean isHudHovered = mouseX >= hudBtnX && mouseX <= hudBtnX + hudBtnW
                && mouseY >= hudBtnY && mouseY <= hudBtnY + hudBtnH;

        int hudBg = isHudHovered ? 0xFF352B66 : 0xFF1C2242;
        int hudBorder = isHudHovered ? CYAN : PURPLE;

        graphics.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudBg);
        graphics.fill(hudBtnX, hudBtnY, hudBtnX + hudBtnW, hudBtnY + 1, hudBorder);
        graphics.fill(hudBtnX, hudBtnY + hudBtnH - 1, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudBorder);
        graphics.fill(hudBtnX, hudBtnY + 1, hudBtnX + 1, hudBtnY + hudBtnH, hudBorder);
        graphics.fill(hudBtnX + hudBtnW - 1, hudBtnY, hudBtnX + hudBtnW, hudBtnY + hudBtnH, hudBorder);

        String hudText = "⊞ HUD Editor";
        int hudTextW = font.width(hudText);
        graphics.text(font, hudText, hudBtnX + (hudBtnW - hudTextW) / 2, hudBtnY + 8, WHITE, false);

        // ==================== 5. PANEL DERECHO (DETALLES DEL MÓDULO) ====================
        int rightPanelWidth = 160;
        int rightPanelX = panelX + panelWidth - rightPanelWidth;

        graphics.fill(rightPanelX, contentY, panelX + panelWidth, panelY + panelHeight, RIGHT_PANEL_BG);
        graphics.fill(rightPanelX, contentY, rightPanelX + 1, panelY + panelHeight, BORDER_COLOR);

        if (selectedModule != null) {
            int iconBoxY = contentY + 16;
            graphics.text(font, selectedModule.icon, rightPanelX + 16, iconBoxY + 2, PURPLE, false);
            graphics.text(font, selectedModule.name, rightPanelX + 34, iconBoxY + 2, WHITE, false);
            graphics.text(font, selectedModule.category.displayName + " • Módulo", rightPanelX + 16, iconBoxY + 17, MUTED, false);

            graphics.fill(rightPanelX + 14, iconBoxY + 31, panelX + panelWidth - 14, iconBoxY + 32, BORDER_COLOR);

            int descY = iconBoxY + 40;
            List<FormattedCharSequence> wrappedLines = font.split(Component.literal(selectedModule.fullDesc), rightPanelWidth - 28);
            for (FormattedCharSequence line : wrappedLines) {
                if (descY + 10 > panelY + panelHeight - 65) break;
                graphics.text(font, line, rightPanelX + 14, descY, MUTED, false);
                descY += 11;
            }

            boolean isModEnabled = selectedModule.isEnabled();
            graphics.text(font, "Estado:", rightPanelX + 14, panelY + panelHeight - 62, MUTED, false);
            graphics.text(font, isModEnabled ? "ACTIVADO" : "DESACTIVADO", rightPanelX + 60, panelY + panelHeight - 62, isModEnabled ? GREEN : MUTED, false);

            int bigBtnX = rightPanelX + 14;
            int bigBtnY = panelY + panelHeight - 44;
            int bigBtnW = rightPanelWidth - 28;
            int bigBtnH = 26;

            boolean isBigHovered = mouseX >= bigBtnX && mouseX <= bigBtnX + bigBtnW
                    && mouseY >= bigBtnY && mouseY <= bigBtnY + bigBtnH;

            int bigBg = isModEnabled ? (isBigHovered ? 0xFF2A5E44 : 0xFF1E4532) : (isBigHovered ? 0xFF2E3550 : 0xFF22283D);
            int bigBorder = isModEnabled ? GREEN : 0xFF434E73;

            graphics.fill(bigBtnX, bigBtnY, bigBtnX + bigBtnW, bigBtnY + bigBtnH, bigBg);
            graphics.fill(bigBtnX, bigBtnY, bigBtnX + bigBtnW, bigBtnY + 1, bigBorder);
            graphics.fill(bigBtnX, bigBtnY + bigBtnH - 1, bigBtnX + bigBtnW, bigBtnY + bigBtnH, bigBorder);
            graphics.fill(bigBtnX, bigBtnY + 1, bigBtnX + 1, bigBtnY + bigBtnH, bigBorder);
            graphics.fill(bigBtnX + bigBtnW - 1, bigBtnY, bigBtnX + bigBtnW, bigBtnY + bigBtnH, bigBorder);

            String statusLabel = isModEnabled ? "Activado" : "Desactivado";
            graphics.text(font, statusLabel, bigBtnX + 10, bigBtnY + 9, WHITE, false);

            int pillX = bigBtnX + bigBtnW - 28;
            int pillY = bigBtnY + 7;
            graphics.fill(pillX, pillY, pillX + 20, pillY + 12, isModEnabled ? GREEN : 0xFF4A557A);
            int knobX = isModEnabled ? pillX + 10 : pillX + 2;
            graphics.fill(knobX, pillY + 2, knobX + 8, pillY + 10, WHITE);
        }

        // ==================== 6. GRID CENTRAL (TARJETAS DE MÓDULOS) ====================
        int gridStartX = panelX + sidebarWidth + 14;
        int gridStartY = contentY + 14;
        int gridWidth = rightPanelX - gridStartX - 14;

        graphics.text(font, currentCategory.displayName, gridStartX, gridStartY, WHITE, false);
        graphics.text(font, "Selecciona una tarjeta para ver sus opciones", gridStartX + 60, gridStartY + 1, MUTED, false);

        List<ModuleEntry> categoryModules = getModulesForCategory(currentCategory);

        int cols = 3;
        int cardSpacingX = 8;
        int cardSpacingY = 8;
        int cardWidth = (gridWidth - (cardSpacingX * (cols - 1))) / cols;
        int cardHeight = 62;
        int cardsAreaY = gridStartY + 18;

        for (int i = 0; i < categoryModules.size(); i++) {
            ModuleEntry entry = categoryModules.get(i);
            int col = i % cols;
            int row = i / cols;

            int cx = gridStartX + col * (cardWidth + cardSpacingX);
            int cy = cardsAreaY + row * (cardHeight + cardSpacingY);

            boolean isHovered = mouseX >= cx && mouseX <= cx + cardWidth && mouseY >= cy && mouseY <= cy + cardHeight;
            boolean isSelected = (entry == selectedModule);
            boolean isEnabled = entry.isEnabled();

            int cardBgColor = isSelected ? CARD_SELECTED_BG : (isHovered ? CARD_HOVER_BG : CARD_BG);
            graphics.fill(cx, cy, cx + cardWidth, cy + cardHeight, cardBgColor);

            int borderCol = isSelected ? PURPLE : (isHovered ? CYAN : BORDER_COLOR);
            graphics.fill(cx, cy, cx + cardWidth, cy + 1, borderCol);
            graphics.fill(cx, cy + cardHeight - 1, cx + cardWidth, cy + cardHeight, borderCol);
            graphics.fill(cx, cy, cx + 1, cy + cardHeight, borderCol);
            graphics.fill(cx + cardWidth - 1, cy, cx + cardWidth, cy + cardHeight, borderCol);

            graphics.text(font, entry.icon, cx + 8, cy + 8, PURPLE, false);
            graphics.text(font, entry.name, cx + 22, cy + 8, WHITE, false);
            graphics.text(font, entry.shortDesc, cx + 8, cy + 24, MUTED, false);

            // Switch toggle en la tarjeta
            int toggleW = 24;
            int toggleH = 12;
            int toggleX = cx + cardWidth - toggleW - 8;
            int toggleY = cy + cardHeight - toggleH - 8;

            int switchBg = isEnabled ? PURPLE : 0xFF353C57;
            graphics.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, switchBg);

            int knobPos = isEnabled ? toggleX + 13 : toggleX + 2;
            graphics.fill(knobPos, toggleY + 2, knobPos + 9, toggleY + toggleH - 2, WHITE);
        }

        // 7. Overlays activos en pantalla (previsualización fiel)
        FPSDisplay.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
        ComboCounter.getInstance().render(graphics);
        ReachDisplay.getInstance().render(graphics);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        double mx = event.x();
        double my = event.y();

        // 1. Clic en botón cerrar [✕]
        int winBtnY = panelY + 10;
        int winCloseX = panelX + panelWidth - 24;
        if (mx >= winCloseX && mx <= winCloseX + 14 && my >= winBtnY && my <= winBtnY + 14) {
            if (minecraft != null) {
                minecraft.gui.setScreen(null);
            }
            return true;
        }

        // 2. Clic en botón HUD EDITOR
        int sidebarWidth = 110;
        int hudBtnY = panelY + panelHeight - 40;
        int hudBtnX = panelX + 8;
        int hudBtnW = sidebarWidth - 16;
        int hudBtnH = 24;
        if (mx >= hudBtnX && mx <= hudBtnX + hudBtnW && my >= hudBtnY && my <= hudBtnY + hudBtnH) {
            if (minecraft != null) {
                minecraft.gui.setScreen(new AltairHudEditorScreen(this));
            }
            return true;
        }

        // 3. Clic en Categorías de la Sidebar
        int headerHeight = 36;
        int contentY = panelY + headerHeight + 1;
        int catBtnY = contentY + 10;
        int catBtnH = 26;
        Category[] cats = Category.values();

        for (Category cat : cats) {
            if (mx >= panelX + 8 && mx <= panelX + sidebarWidth - 8 && my >= catBtnY && my <= catBtnY + catBtnH) {
                currentCategory = cat;
                List<ModuleEntry> mods = getModulesForCategory(cat);
                if (!mods.isEmpty()) {
                    selectedModule = mods.get(0);
                }
                return true;
            }
            catBtnY += catBtnH + 4;
        }

        // 4. Clic en el botón toggle del panel derecho
        int rightPanelWidth = 160;
        int rightPanelX = panelX + panelWidth - rightPanelWidth;
        int bigBtnX = rightPanelX + 14;
        int bigBtnY = panelY + panelHeight - 44;
        int bigBtnW = rightPanelWidth - 28;
        int bigBtnH = 26;

        if (selectedModule != null && mx >= bigBtnX && mx <= bigBtnX + bigBtnW && my >= bigBtnY && my <= bigBtnY + bigBtnH) {
            selectedModule.toggle();
            return true;
        }

        // 5. Clic en las tarjetas del grid central
        int gridStartX = panelX + sidebarWidth + 14;
        int gridStartY = contentY + 14;
        int gridWidth = rightPanelX - gridStartX - 14;

        List<ModuleEntry> categoryModules = getModulesForCategory(currentCategory);
        int cols = 3;
        int cardSpacingX = 8;
        int cardSpacingY = 8;
        int cardWidth = (gridWidth - (cardSpacingX * (cols - 1))) / cols;
        int cardHeight = 62;
        int cardsAreaY = gridStartY + 18;

        for (int i = 0; i < categoryModules.size(); i++) {
            ModuleEntry entry = categoryModules.get(i);
            int col = i % cols;
            int row = i / cols;

            int cx = gridStartX + col * (cardWidth + cardSpacingX);
            int cy = cardsAreaY + row * (cardHeight + cardSpacingY);

            if (mx >= cx && mx <= cx + cardWidth && my >= cy && my <= cy + cardHeight) {
                selectedModule = entry;

                // Clic directo sobre el switch de la tarjeta
                int toggleW = 24;
                int toggleH = 12;
                int toggleX = cx + cardWidth - toggleW - 8;
                int toggleY = cy + cardHeight - toggleH - 8;

                if (mx >= toggleX - 4 && mx <= toggleX + toggleW + 4 && my >= toggleY - 4 && my <= toggleY + toggleH + 4) {
                    entry.toggle();
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
