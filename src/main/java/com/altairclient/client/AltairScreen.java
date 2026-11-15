package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.ComboCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.ArrowCounter;
import com.altairclient.modules.mods.AutoTool;
import com.altairclient.modules.mods.FastGame;
import com.altairclient.modules.mods.ItemNameDisplay;
import com.altairclient.modules.mods.AutoGG;
import com.altairclient.modules.mods.AutoRespawn;
import com.altairclient.modules.mods.ChatBubbleHandler;
import com.altairclient.modules.mods.ChatTimestamps;
import com.altairclient.modules.mods.ClearWater;
import com.altairclient.modules.mods.DamageIndicator;
import com.altairclient.modules.mods.MemoryDisplay;
import com.altairclient.modules.mods.NoHurtCamera;
import com.altairclient.modules.mods.TotemCounter;
import com.altairclient.modules.mods.FovHandler;
import com.altairclient.modules.mods.NametagHandler;
import com.altairclient.modules.mods.FullbrightHandler;
import com.altairclient.modules.mods.FreecamHandler;
import com.altairclient.modules.mods.HealthDisplay;
import com.altairclient.modules.mods.HitIndicator;
import com.altairclient.modules.mods.HitboxDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PerspectiveHandler;
import com.altairclient.modules.mods.PingDisplay;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.ReachDisplay;
import com.altairclient.modules.mods.ScreenshotModule;
import com.altairclient.modules.mods.ScreenshotUpload;
import com.altairclient.modules.mods.ToggleSneak;
import com.altairclient.modules.mods.ToggleSprint;
import com.altairclient.modules.mods.WorldInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
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

        // Ajuste numérico opcional (se muestra con botones - / + en el panel derecho)
        String settingLabel = null;
        java.util.function.IntSupplier settingGetter = null;
        java.util.function.IntConsumer settingSetter = null;
        int settingMin = 0;
        int settingMax = 0;
        int settingStep = 1;
        String[] settingNames = null;

        ModuleEntry withSetting(
                String label,
                int min,
                int max,
                int step,
                java.util.function.IntSupplier getter,
                java.util.function.IntConsumer setter
        ) {
            this.settingLabel = label;
            this.settingMin = min;
            this.settingMax = max;
            this.settingStep = step;
            this.settingGetter = getter;
            this.settingSetter = setter;
            return this;
        }

        ModuleEntry withSettingNames(String[] names) {
            this.settingNames = names;
            return this;
        }

        String settingText() {
            int v = settingGetter.getAsInt();
            if (settingNames != null && v >= 0 && v < settingNames.length) {
                return settingNames[v];
            }
            return String.valueOf(v);
        }

        boolean hasSetting() {
            return settingGetter != null && settingSetter != null;
        }

        void adjustSetting(int direction) {
            if (hasSetting()) {
                int v = settingGetter.getAsInt() + direction * settingStep;
                settingSetter.accept(Math.max(settingMin, Math.min(settingMax, v)));
            }
        }

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

    // Cuadrícula con scroll
    private static final int CARD_HEIGHT = 68;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private String fit(String text, int maxWidth) {
        if (maxWidth <= 0 || font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("\u2026"))) + "\u2026";
    }

    // Barra de búsqueda del header
    private static final int SEARCH_WIDTH = 220;
    private static final int SEARCH_HEIGHT = 20;
    private EditBox searchBox;
    private String searchQuery = "";

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

        allModules.add(new ModuleEntry(
                "Health Display",
                "Corazones del objetivo",
                "Muestra el número de corazones de la entidad o jugador al que apuntas (1 corazón = 2 de vida). Si no apuntas a nadie, muestra los tuyos.",
                Category.COMBAT,
                "\u2665",
                () -> HealthDisplay.getInstance().toggle(),
                () -> HealthDisplay.getInstance().isEnabled()
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

        allModules.add(new ModuleEntry(
                "Ping Display",
                "Latencia al servidor",
                "Muestra tu latencia (ping) en milisegundos con colores: verde hasta 80 ms, amarillo hasta 150 ms y rojo por encima.",
                Category.RENDER,
                "\u25C9",
                () -> PingDisplay.getInstance().toggle(),
                () -> PingDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Freecam",
                "Cámara libre",
                "Tu jugador se queda quieto y la cámara vuela libre como en modo espectador. Vuela con W/A/S/D, Espacio sube, Mayús baja y Ctrl acelera. Actívalo aquí o con la tecla Freecam (V por defecto, configurable en Controles). Es solo visual: no mueve a tu jugador en el servidor.",
                Category.RENDER,
                "\u25CE",
                () -> FreecamHandler.toggle(),
                () -> FreecamHandler.isActive()
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

        allModules.add(new ModuleEntry(
                "Custom FOV",
                "FOV personalizado",
                "Usa tu propio campo de visión en lugar del de las opciones de vídeo. Ajusta el valor con los botones - y + (de 30 a 110).",
                Category.RENDER,
                "\u25CD",
                () -> FovHandler.toggleCustom(),
                () -> FovHandler.isCustomEnabled()
        ).withSetting("FOV", FovHandler.MIN_FOV, FovHandler.MAX_FOV, 5,
                () -> FovHandler.getCustomFov(), v -> FovHandler.setCustomFov(v)));

        allModules.add(new ModuleEntry(
                "Dynamic FOV",
                "Cambios dinámicos",
                "Activado: el FOV cambia al correr o con efectos de velocidad, como en vanilla. Desactivado: el FOV se queda fijo.",
                Category.RENDER,
                "\u2194",
                () -> FovHandler.toggleDynamic(),
                () -> FovHandler.isDynamicEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Perspective Mod",
                "Mirar alrededor",
                "Mantén la tecla Perspective Mod (Alt izquierdo por defecto, configurable en Controles) y mueve el ratón: la cámara pasa a tercera persona y orbita sin girar a tu jugador. Es visual y no envía giros al servidor.",
                Category.RENDER,
                "\u25CE",
                () -> PerspectiveHandler.toggle(),
                () -> PerspectiveHandler.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Toggle Sprint",
                "Correr automático",
                "Corres automáticamente mientras avanzas, sin mantener la tecla de sprint. Asígnale un atajo en Controles si quieres.",
                Category.PLAYER,
                "\u00BB",
                () -> ToggleSprint.toggle(),
                () -> ToggleSprint.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Toggle Sneak",
                "Agacharse automático",
                "Te mantienes agachado automáticamente, sin mantener la tecla de agacharse. Asígnale un atajo en Controles si quieres.",
                Category.PLAYER,
                "\u2193",
                () -> ToggleSneak.toggle(),
                () -> ToggleSneak.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Screenshot",
                "Captura de pantalla",
                "Hace una captura del juego sin el menú y la guarda en la carpeta screenshots. También con la tecla Captura de pantalla (F9 por defecto, configurable en Controles).",
                Category.MISC,
                "\u25A3",
                () -> {
                    ScreenshotModule.request();
                    if (net.minecraft.client.Minecraft.getInstance() != null) {
                        net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);
                    }
                },
                () -> false
        ));

        allModules.add(new ModuleEntry(
                "Screenshot Upload",
                "Subir última captura",
                "Envía tu última captura a un webhook de Discord tuyo. Pon la URL del webhook en el archivo altairclient-webhook.txt (carpeta de Minecraft). También con la tecla Subir captura (sin asignar, configurable en Controles).",
                Category.MISC,
                "\u2191",
                () -> {
                    net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);
                    ScreenshotUpload.upload();
                },
                () -> false
        ));

        allModules.add(new ModuleEntry(
                "Freelook",
                "Cámara libre 3ª persona",
                "Pasa a tercera persona y el ratón mueve la cámara alrededor de tu jugador sin girarlo, como un orbitar libre. No vuela como Freecam: sigue anclada a ti. Actívalo aquí o con la tecla Freelook (B por defecto, configurable en Controles). Es visual y no envía giros al servidor.",
                Category.RENDER,
                "\u25CE",
                () -> PerspectiveHandler.toggleLatched(),
                () -> PerspectiveHandler.isLatched()
        ));

        allModules.add(new ModuleEntry(
                "Fullbright",
                "Brillo máximo",
                "Sube el brillo al máximo para ver en cuevas y de noche. Es el ajuste de brillo del propio juego: no añade efectos de poción ni envía nada al servidor. Si el servidor prohíbe el fullbright, no lo actives. Al desactivarlo vuelve tu brillo original.",
                Category.RENDER,
                "\u2600",
                () -> FullbrightHandler.toggle(),
                () -> FullbrightHandler.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Custom Nametags",
                "Nombres con estilo",
                "Cambia el aspecto de los nombres sobre jugadores y entidades: Altair, Dorado, Neón o Marcado. Solo lo ves tú, no cambia el nombre real de nadie. Elige el estilo con - y +. Tecla opcional en Controles.",
                Category.RENDER,
                "\u25C6",
                () -> NametagHandler.toggle(),
                () -> NametagHandler.isEnabled()
        ).withSetting("Estilo", 0, NametagHandler.STYLE_COUNT - 1, 1,
                () -> NametagHandler.getStyle(), v -> NametagHandler.setStyle(v))
                .withSettingNames(NametagHandler.STYLE_NAMES));

        allModules.add(new ModuleEntry(
                "Better Chat Bubbles",
                "Burbujas de chat",
                "Lo que escribe un jugador aparece unos segundos sobre su cabeza, bajo su nombre. Solo lo ves tú. Tecla opcional en Controles.",
                Category.RENDER,
                "\uD83D\uDCAC",
                () -> ChatBubbleHandler.toggle(),
                () -> ChatBubbleHandler.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Damage Indicator",
                "Daño en números",
                "Cada vez que le quitas vida a una entidad, el daño sale en números que flotan y se desvanecen, estilo Fortnite. Mueve el punto de salida en el HUD Editor.",
                Category.COMBAT,
                "\u2736",
                () -> DamageIndicator.getInstance().toggle(),
                () -> DamageIndicator.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Totem Counter",
                "Tótems en inventario",
                "Cuenta los tótems de la inmortalidad que llevas, incluida la mano secundaria. Se mueve en el HUD Editor.",
                Category.COMBAT,
                "\u2605",
                () -> TotemCounter.getInstance().toggle(),
                () -> TotemCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Arrow Counter",
                "Flechas en inventario",
                "Cuenta las flechas (normales, espectrales y con efecto) que llevas. Se mueve en el HUD Editor.",
                Category.COMBAT,
                "\u27B3",
                () -> ArrowCounter.getInstance().toggle(),
                () -> ArrowCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Memory Display",
                "RAM usada",
                "Muestra la RAM que usa el juego como porcentaje, como gráfico de los últimos segundos o las dos cosas. Elige el modo con - y +. Se mueve en el HUD Editor.",
                Category.WORLD,
                "\u25A6",
                () -> MemoryDisplay.getInstance().toggle(),
                () -> MemoryDisplay.getInstance().isEnabled()
        ).withSetting("Modo", 0, MemoryDisplay.MODE_NAMES.length - 1, 1,
                () -> MemoryDisplay.getInstance().getMode(), v -> MemoryDisplay.getInstance().setMode(v))
                .withSettingNames(MemoryDisplay.MODE_NAMES));

        allModules.add(new ModuleEntry(
                "Auto GG",
                "GG automático",
                "Escribe gg en el chat cuando el servidor anuncia el final de una partida. Solo reacciona a mensajes del servidor y espera 15 s entre envíos. Algunos servidores no permiten mensajes automáticos: actívalo solo si lo admiten.",
                Category.MISC,
                "\u2713",
                () -> AutoGG.toggle(),
                () -> AutoGG.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Auto Respawn",
                "Reaparecer solo",
                "Al morir, pulsa Reaparecer por ti automáticamente.",
                Category.PLAYER,
                "\u21BB",
                () -> AutoRespawn.toggle(),
                () -> AutoRespawn.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Clear Water",
                "Agua sin niebla",
                "Bajo el agua se ve lejos y sin el filtro azul de la pantalla. No cambia el aspecto de la superficie del agua.",
                Category.RENDER,
                "\u2248",
                () -> ClearWater.toggle(),
                () -> ClearWater.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "No Hurt Camera",
                "Sin sacudida de cámara",
                "Elimina el movimiento de cámara al recibir daño.",
                Category.RENDER,
                "\u2715",
                () -> NoHurtCamera.toggle(),
                () -> NoHurtCamera.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Chat Timestamps",
                "Hora en el chat",
                "Añade la hora [HH:mm] delante de cada mensaje del chat.",
                Category.MISC,
                "\u231A",
                () -> ChatTimestamps.toggle(),
                () -> ChatTimestamps.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Item Name",
                "Nombre del objeto",
                "Muestra el nombre del objeto que llevas en la mano, con el color de su rareza. Se mueve en el HUD Editor.",
                Category.PLAYER,
                "\u270E",
                () -> ItemNameDisplay.getInstance().toggle(),
                () -> ItemNameDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Auto Tool",
                "Herramienta adecuada",
                "Al picar un bloque, selecciona de tu barra rápida la herramienta más rápida para ese bloque y al terminar vuelve al hueco que tenías.",
                Category.PLAYER,
                "\u26CF",
                () -> AutoTool.toggle(),
                () -> AutoTool.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Fast Game",
                "Optimiza el juego",
                "No dibuja las entidades tapadas por bloques, baja la distancia de entidades y quita sus sombras. Al desactivarlo vuelven tus ajustes.",
                Category.MISC,
                "\u26A1",
                () -> FastGame.toggle(),
                () -> FastGame.isEnabled()
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

        // Caja de texto real dentro de la barra de búsqueda (se conserva el texto al redimensionar).
        int searchX = panelX + (panelWidth - SEARCH_WIDTH) / 2 - 20;
        int searchY = panelY + 8;
        searchBox = new EditBox(
                font,
                searchX + 24,
                searchY + 6,
                SEARCH_WIDTH - 32,
                12,
                Component.literal("Buscar módulos")
        );
        searchBox.setBordered(false);
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.literal("Buscar módulos..."));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(this::onSearchChanged);
        addWidget(searchBox);
    }

    private void onSearchChanged(String text) {
        searchQuery = text == null ? "" : text.trim();
        scrollOffset = 0;
        List<ModuleEntry> visible = getVisibleModules();
        if (!visible.isEmpty() && !visible.contains(selectedModule)) {
            selectedModule = visible.get(0);
        }
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

    /** Con búsqueda activa filtra en todas las categorías; si no, muestra la categoría actual. */
    private List<ModuleEntry> getVisibleModules() {
        if (searchQuery.isEmpty()) {
            return getModulesForCategory(currentCategory);
        }
        String q = searchQuery.toLowerCase(java.util.Locale.ROOT);
        List<ModuleEntry> list = new ArrayList<>();
        for (ModuleEntry m : allModules) {
            if (m.name.toLowerCase(java.util.Locale.ROOT).contains(q)
                    || m.shortDesc.toLowerCase(java.util.Locale.ROOT).contains(q)
                    || m.category.displayName.toLowerCase(java.util.Locale.ROOT).contains(q)) {
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
        int searchWidth = SEARCH_WIDTH;
        int searchHeight = SEARCH_HEIGHT;
        int searchX = panelX + (panelWidth - searchWidth) / 2 - 20;
        int searchY = panelY + 8;
        int searchBorder = (searchBox != null && searchBox.isFocused()) ? CYAN : BORDER_COLOR;

        graphics.fill(searchX, searchY, searchX + searchWidth, searchY + searchHeight, 0xFF182038);
        graphics.fill(searchX, searchY, searchX + searchWidth, searchY + 1, searchBorder);
        graphics.fill(searchX, searchY + searchHeight - 1, searchX + searchWidth, searchY + searchHeight, searchBorder);
        graphics.fill(searchX, searchY + 1, searchX + 1, searchY + searchHeight, searchBorder);
        graphics.fill(searchX + searchWidth - 1, searchY, searchX + searchWidth, searchY + searchHeight, searchBorder);

        graphics.text(font, "🔍", searchX + 8, searchY + 6, MUTED, false);
        if (searchBox != null) {
            searchBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

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
            graphics.text(font, fit(selectedModule.name, rightPanelWidth - 34 - 14), rightPanelX + 34, iconBoxY + 2, WHITE, false);
            graphics.text(font, selectedModule.category.displayName + " • Módulo", rightPanelX + 16, iconBoxY + 17, MUTED, false);

            graphics.fill(rightPanelX + 14, iconBoxY + 31, panelX + panelWidth - 14, iconBoxY + 32, BORDER_COLOR);

            int descY = iconBoxY + 40;
            List<FormattedCharSequence> wrappedLines = font.split(Component.literal(selectedModule.fullDesc), rightPanelWidth - 28);
            for (FormattedCharSequence line : wrappedLines) {
                if (descY + 10 > panelY + panelHeight - (selectedModule.hasSetting() ? 105 : 65)) break;
                graphics.text(font, line, rightPanelX + 14, descY, MUTED, false);
                descY += 11;
            }

            if (selectedModule.hasSetting()) {
                int setY = panelY + panelHeight - 88;
                int minusX = rightPanelX + rightPanelWidth - 14 - 38;
                int plusX = rightPanelX + rightPanelWidth - 14 - 16;
                boolean minusHover = mouseX >= minusX && mouseX <= minusX + 16 && mouseY >= setY && mouseY <= setY + 14;
                boolean plusHover = mouseX >= plusX && mouseX <= plusX + 16 && mouseY >= setY && mouseY <= setY + 14;

                graphics.text(font, fit(selectedModule.settingLabel + ": " + selectedModule.settingText(), minusX - (rightPanelX + 14) - 6),
                        rightPanelX + 14, setY + 3, WHITE, false);

                graphics.fill(minusX, setY, minusX + 16, setY + 14, minusHover ? 0xFF2E3550 : 0xFF22283D);
                graphics.fill(plusX, setY, plusX + 16, setY + 14, plusHover ? 0xFF2E3550 : 0xFF22283D);
                graphics.text(font, "-", minusX + 6, setY + 3, WHITE, false);
                graphics.text(font, "+", plusX + 5, setY + 3, WHITE, false);
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

        boolean searching = !searchQuery.isEmpty();
        String gridTitle = searching ? "Resultados" : currentCategory.displayName;
        graphics.text(font, gridTitle, gridStartX, gridStartY, WHITE, false);
        graphics.text(font, searching ? "\"" + searchQuery + "\"" : "Selecciona una tarjeta para ver sus opciones",
                gridStartX + font.width(gridTitle) + 10, gridStartY + 1, MUTED, false);

        List<ModuleEntry> categoryModules = getVisibleModules();

        if (searching && categoryModules.isEmpty()) {
            graphics.text(font, "Sin resultados", gridStartX, gridStartY + 22, MUTED, false);
        }

        int cols = 3;
        int cardSpacingX = 8;
        int cardSpacingY = 8;
        int cardWidth = (gridWidth - (cardSpacingX * (cols - 1))) / cols;
        int cardHeight = CARD_HEIGHT;
        int cardsAreaY = gridStartY + 18;
        int areaBottom = panelY + panelHeight - 8;

        int rows = (categoryModules.size() + cols - 1) / cols;
        int contentHeight = rows * (cardHeight + cardSpacingY) - cardSpacingY;
        maxScroll = Math.max(0, contentHeight - (areaBottom - cardsAreaY));
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

        // Todo lo que se salga del área de tarjetas se recorta: así los módulos salen al hacer scroll.
        graphics.enableScissor(gridStartX - 1, cardsAreaY, gridStartX + gridWidth + 1, areaBottom);

        for (int i = 0; i < categoryModules.size(); i++) {
            ModuleEntry entry = categoryModules.get(i);
            int col = i % cols;
            int row = i / cols;

            int cx = gridStartX + col * (cardWidth + cardSpacingX);
            int cy = cardsAreaY + row * (cardHeight + cardSpacingY) - scrollOffset;

            if (cy + cardHeight < cardsAreaY || cy > areaBottom) {
                continue;
            }

            boolean isHovered = mouseX >= cx && mouseX <= cx + cardWidth
                    && mouseY >= Math.max(cy, cardsAreaY) && mouseY <= Math.min(cy + cardHeight, areaBottom);
            boolean isSelected = (entry == selectedModule);
            boolean isEnabled = entry.isEnabled();

            int cardBgColor = isSelected ? CARD_SELECTED_BG : (isHovered ? CARD_HOVER_BG : CARD_BG);
            graphics.fill(cx, cy, cx + cardWidth, cy + cardHeight, cardBgColor);

            int borderCol = isSelected ? PURPLE : (isHovered ? CYAN : BORDER_COLOR);
            graphics.fill(cx, cy, cx + cardWidth, cy + 1, borderCol);
            graphics.fill(cx, cy + cardHeight - 1, cx + cardWidth, cy + cardHeight, borderCol);
            graphics.fill(cx, cy, cx + 1, cy + cardHeight, borderCol);
            graphics.fill(cx + cardWidth - 1, cy, cx + cardWidth, cy + cardHeight, borderCol);

            // Textos con 8 px de margen a cada lado, recortados si no caben
            int textWidth = cardWidth - 16;
            graphics.text(font, entry.icon, cx + 8, cy + 8, PURPLE, false);
            graphics.text(font, fit(entry.name, textWidth - 14), cx + 22, cy + 8, WHITE, false);

            List<FormattedCharSequence> descLines = font.split(Component.literal(entry.shortDesc), textWidth);
            for (int l = 0; l < Math.min(2, descLines.size()); l++) {
                graphics.text(font, descLines.get(l), cx + 8, cy + 23 + l * 11, MUTED, false);
            }

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

        graphics.disableScissor();

        // Barra de scroll
        if (maxScroll > 0) {
            int trackX = gridStartX + gridWidth + 5;
            int trackH = areaBottom - cardsAreaY;
            int thumbH = Math.max(16, trackH * trackH / (trackH + maxScroll));
            int thumbY = cardsAreaY + (trackH - thumbH) * scrollOffset / maxScroll;
            graphics.fill(trackX, cardsAreaY, trackX + 3, areaBottom, 0xFF1B2138);
            graphics.fill(trackX, thumbY, trackX + 3, thumbY + thumbH, PURPLE);
        }

        // 7. Overlays activos en pantalla (previsualización fiel)
        FPSDisplay.getInstance().render(graphics);
        PingDisplay.getInstance().render(graphics);
        HealthDisplay.getInstance().render(graphics);
        TotemCounter.getInstance().render(graphics);
        ArrowCounter.getInstance().render(graphics);
        MemoryDisplay.getInstance().render(graphics);
        DamageIndicator.getInstance().render(graphics);
        ItemNameDisplay.getInstance().render(graphics);
        CPSCounter.getInstance().render(graphics);
        ArmorHUD.getInstance().render(graphics);
        Keystrokes.getInstance().render(graphics);
        PotionStatus.getInstance().render(graphics);
        WorldInfo.getInstance().render(graphics);
        ComboCounter.getInstance().render(graphics);
        ReachDisplay.getInstance().render(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.round(scrollY * 22)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        double mx = event.x();
        double my = event.y();

        // 0. Clic en la barra de búsqueda: enfocar; en cualquier otro sitio: quitar el foco.
        if (searchBox != null) {
            int searchX = panelX + (panelWidth - SEARCH_WIDTH) / 2 - 20;
            int searchY = panelY + 8;
            boolean inSearch = mx >= searchX && mx <= searchX + SEARCH_WIDTH
                    && my >= searchY && my <= searchY + SEARCH_HEIGHT;
            if (inSearch) {
                // La pantalla manda teclado/texto a su elemento enfocado, así que hay que
                // enfocar la caja también a nivel de pantalla, no solo en la caja.
                setFocused(searchBox);
                searchBox.setFocused(true);
                searchBox.mouseClicked(event, doubleClick);
                return true;
            }
            if (getFocused() == searchBox) {
                setFocused(null);
            }
            searchBox.setFocused(false);
        }

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
                scrollOffset = 0;
                if (searchBox != null && !searchBox.getValue().isEmpty()) {
                    searchBox.setValue(""); // al cambiar de categoría se limpia la búsqueda
                }
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

        if (selectedModule != null && selectedModule.hasSetting()) {
            int setY = panelY + panelHeight - 88;
            int minusX = rightPanelX + rightPanelWidth - 14 - 38;
            int plusX = rightPanelX + rightPanelWidth - 14 - 16;
            if (my >= setY && my <= setY + 14) {
                if (mx >= minusX && mx <= minusX + 16) {
                    selectedModule.adjustSetting(-1);
                    return true;
                }
                if (mx >= plusX && mx <= plusX + 16) {
                    selectedModule.adjustSetting(1);
                    return true;
                }
            }
        }

        if (selectedModule != null && mx >= bigBtnX && mx <= bigBtnX + bigBtnW && my >= bigBtnY && my <= bigBtnY + bigBtnH) {
            selectedModule.toggle();
            return true;
        }

        // 5. Clic en las tarjetas del grid central
        int gridStartX = panelX + sidebarWidth + 14;
        int gridStartY = contentY + 14;
        int gridWidth = rightPanelX - gridStartX - 14;

        List<ModuleEntry> categoryModules = getVisibleModules();
        int cols = 3;
        int cardSpacingX = 8;
        int cardSpacingY = 8;
        int cardWidth = (gridWidth - (cardSpacingX * (cols - 1))) / cols;
        int cardHeight = CARD_HEIGHT;
        int cardsAreaY = gridStartY + 18;
        int areaBottom = panelY + panelHeight - 8;

        if (my >= cardsAreaY && my <= areaBottom) {
            for (int i = 0; i < categoryModules.size(); i++) {
                ModuleEntry entry = categoryModules.get(i);
                int col = i % cols;
                int row = i / cols;

                int cx = gridStartX + col * (cardWidth + cardSpacingX);
                int cy = cardsAreaY + row * (cardHeight + cardSpacingY) - scrollOffset;

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
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
