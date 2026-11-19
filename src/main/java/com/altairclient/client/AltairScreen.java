package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.ComboCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.ArrowCounter;
import com.altairclient.modules.mods.AutoTool;
import com.altairclient.modules.mods.FastGame;
import com.altairclient.modules.mods.ItemNameDisplay;
import com.altairclient.modules.mods.BlockOutline;
import com.altairclient.modules.mods.CustomCrosshair;
import com.altairclient.modules.mods.DirectionHud;
import com.altairclient.modules.mods.ServerAddress;
import com.altairclient.modules.mods.TimeChanger;
import com.altairclient.modules.mods.WeatherChanger;
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
import com.altairclient.modules.mods.AltairSky;
import com.altairclient.modules.mods.NoAnimation;
import com.altairclient.modules.textures.TextureModules;

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

    /** Las dos carpetas de la barra izquierda: al abrir una salen sus apartados. */
    public enum Section {
        MODS("Mods", "Módulo"),
        TEXTURES("Textures", "Textura"),
        SETTINGS("Settings", "Setting");

        final String displayName;
        /** Cómo se llama cada elemento de la carpeta en el panel de detalles. */
        final String kind;

        Section(String displayName, String kind) {
            this.displayName = displayName;
            this.kind = kind;
        }
    }

    public enum Category {
        // Carpeta Mods
        COMBAT(Section.MODS, null, "Combat", "Combat", "⚔"),
        RENDER(Section.MODS, null, "Render", "Render", "🖥"),
        PLAYER(Section.MODS, null, "Player", "Player", "👤"),
        WORLD(Section.MODS, null, "World", "World", "🌐"),
        MISC(Section.MODS, null, "Misc", "Misc", "✨"),
        APPS(Section.MODS, null, "Apps", "Apps", "▦"),

        // Carpeta Textures
        TEX_PVP(Section.TEXTURES, TextureModules.PVP, "PvP Textures", "PvP", "⚔"),
        TEX_BUILD(Section.TEXTURES, TextureModules.BUILD, "Build Textures", "Build", "▣"),
        TEX_BREAKING(Section.TEXTURES, TextureModules.BREAKING, "Breaking Effects", "Breaking", "⛏"),
        TEX_MOVEMENT(Section.TEXTURES, TextureModules.MOVEMENT, "Movement Effects", "Movement", "»"),
        TEX_WALK(Section.TEXTURES, TextureModules.WALK, "Walk Effects", "Walk", "✧"),
        TEX_ANIMATIONS(Section.TEXTURES, TextureModules.ANIMATIONS, "Movement Animations", "Animations", "↻"),
        TEX_EXTRA(Section.TEXTURES, TextureModules.EXTRA, "Extra Visual Effects", "Extra", "✦"),
        TEX_MISC(Section.TEXTURES, TextureModules.MISC, "Misc", "Misc", "✺"),

        // Carpeta Settings
        SET_GENERAL(Section.SETTINGS, null, "Configuration", "Configuration", "⚙");

        final Section section;
        /** Clave del apartado en {@link TextureModules}; null en la carpeta Mods. */
        final String textureGroup;
        /** Nombre completo: título de la cuadrícula y búsqueda. */
        final String displayName;
        /** Nombre corto para la barra izquierda, donde el ancho es justo. */
        final String shortName;
        final String icon;

        Category(Section section, String textureGroup, String displayName, String shortName, String icon) {
            this.section = section;
            this.textureGroup = textureGroup;
            this.displayName = displayName;
            this.shortName = shortName;
            this.icon = icon;
        }
    }

    private static final class ModuleEntry {
        final String name;
        final String shortDesc;
        String fullDesc;
        final Category category;
        final String icon;
        final Runnable onToggle;
        final java.util.function.BooleanSupplier isEnabledSupplier;

        // Animaciones de la tarjeta: selección, hover e interruptor
        final Anim.Smooth selAnim = new Anim.Smooth();
        final Anim.Smooth hovAnim = new Anim.Smooth();
        final Anim.Smooth togAnim = new Anim.Smooth();

        // Ajuste numérico opcional (se muestra con botones - / + en el panel derecho)
        String settingLabel = null;
        java.util.function.IntSupplier settingGetter = null;
        java.util.function.IntConsumer settingSetter = null;
        int settingMin = 0;
        int settingMax = 0;
        int settingStep = 1;
        String[] settingNames = null;

        /** Si la entrada tiene color (texturas con color), devuelve su color de la paleta de Altair. */
        java.util.function.IntSupplier colorSupplier = null;

        ModuleEntry withColor(java.util.function.IntSupplier colorIndexSupplier) {
            this.colorSupplier = colorIndexSupplier;
            return this;
        }

        /** Color del icono y del círculo de color: el de la paleta si lo tiene; si no, morado Altair. */
        int accent() {
            return colorSupplier != null ? Palette.color(colorSupplier.getAsInt()) : PURPLE;
        }

        /** Ajustes adicionales (se dibujan encima del principal). */
        final java.util.List<Extra> extras = new java.util.ArrayList<>();

        static final class Extra {
            String label;
            int min;
            int max;
            int step;
            java.util.function.IntSupplier getter;
            java.util.function.IntConsumer setter;
            String[] names;

            String text() {
                int v = getter.getAsInt();
                return names != null && v >= 0 && v < names.length ? names[v] : String.valueOf(v);
            }

            void adjust(int direction) {
                setter.accept(Math.max(min, Math.min(max, getter.getAsInt() + direction * step)));
            }
        }

        ModuleEntry withExtra(String label, int min, int max, int step,
                              java.util.function.IntSupplier getter, java.util.function.IntConsumer setter,
                              String[] names) {
            Extra e = new Extra();
            e.label = label;
            e.min = min;
            e.max = max;
            e.step = step;
            e.getter = getter;
            e.setter = setter;
            e.names = names;
            extras.add(e);
            return this;
        }

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

    // Barra izquierda: carpetas Mods / Textures (solo una abierta a la vez) y su scroll
    private Section openSection = Section.MODS;
    private int sidebarScroll = 0;
    private int sidebarMaxScroll = 0;
    private final Anim.Smooth[] secHov = newSmooths(Section.values().length);
    private static final int SIDEBAR_WIDTH = 110;
    private static final int FOLDER_H = 24;
    private static final int SUB_H = 20;
    private static final int ROW_GAP = 2;

    /** Una fila de la barra izquierda: una carpeta o un apartado dentro de la carpeta abierta. */
    private static final class SideRow {
        final Section section;
        final Category category;
        final int y;
        final int h;

        SideRow(Section section, Category category, int y, int h) {
            this.section = section;
            this.category = category;
            this.y = y;
            this.h = h;
        }
    }

    /** Filas de la barra izquierda con su posición, sin aplicar el scroll. Sirve para dibujar y para los clics. */
    private List<SideRow> sidebarRows(int firstY) {
        if (rowsCache != null && rowsCacheY == firstY && rowsCacheSection == openSection) {
            return rowsCache;
        }
        List<SideRow> rows = new ArrayList<>();
        int y = firstY;
        for (Section sec : Section.values()) {
            rows.add(new SideRow(sec, null, y, FOLDER_H));
            y += FOLDER_H + ROW_GAP;
            if (sec == openSection) {
                for (Category cat : Category.values()) {
                    if (cat.section == sec) {
                        rows.add(new SideRow(sec, cat, y, SUB_H));
                        y += SUB_H + ROW_GAP;
                    }
                }
            }
        }
        rowsCache = rows;
        rowsCacheY = firstY;
        rowsCacheSection = openSection;
        return rows;
    }

    private List<SideRow> rowsCache;
    private int rowsCacheY = Integer.MIN_VALUE;
    private Section rowsCacheSection;

    // Cache de textos partidos en lineas: font.split es caro y las descripciones no cambian.
    private final java.util.HashMap<String, List<FormattedCharSequence>> splitCache = new java.util.HashMap<>();

    private List<FormattedCharSequence> splitCached(String text, int width) {
        String key = width + "|" + text;
        List<FormattedCharSequence> lines = splitCache.get(key);
        if (lines == null) {
            lines = font.split(Component.literal(text), width);
            if (splitCache.size() > 512) {
                splitCache.clear();
            }
            splitCache.put(key, lines);
        }
        return lines;
    }

    // Cache de la lista visible: solo se recalcula si cambia la categoria o la busqueda.
    private List<ModuleEntry> visibleCache;
    private Category visibleCat;
    private String visibleQuery;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    // Cuadrícula con scroll
    private static final int CARD_HEIGHT = 68;
    private int scrollOffset = 0;
    private float scrollTarget = 0.0F;
    private final Anim.Smooth scrollSmooth = new Anim.Smooth();

    // Animaciones del menú
    private final long openStart = System.nanoTime();
    private long gridAnimStart = System.nanoTime();
    private ModuleEntry lastDrawnSelected = null;
    private long selectStart = System.nanoTime();
    private final Anim.Smooth[] catSel = newSmooths(Category.values().length);
    private final Anim.Smooth[] catHov = newSmooths(Category.values().length);

    private static Anim.Smooth[] newSmooths(int n) {
        Anim.Smooth[] arr = new Anim.Smooth[n];
        for (int i = 0; i < n; i++) {
            arr[i] = new Anim.Smooth();
        }
        return arr;
    }

    /** Vuelve al inicio del scroll y repite la animación de entrada de las tarjetas. */
    private void resetGridAnim() {
        scrollOffset = 0;
        scrollTarget = 0.0F;
        scrollSmooth.snap(0.0F);
        gridAnimStart = System.nanoTime();
    }
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
                "Muestra tu latencia (ping) en milisegundos con un color que pasa suavemente de verde a amarillo y rojo según sube.",
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
                Category.APPS,
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
                Category.APPS,
                "\u2191",
                () -> {
                    net.minecraft.client.Minecraft.getInstance().gui.setScreen(null);
                    ScreenshotUpload.upload();
                },
                () -> false
        ));

        allModules.add(new ModuleEntry(
                "Notes",
                "Word-style notepad",
                "A small note-taking window you can use inside Minecraft, like a tiny Word. Bold, italic, underline, strikethrough, 8 text colors, bullet lists, numbered lists and task lists with check boxes, and a Coords button that writes your current X Y Z. Select text with the mouse or Shift + arrows and use the toolbar or Ctrl+B / Ctrl+I / Ctrl+U. Drag the title bar to move it and the bottom-right corner to resize it. Everything is saved automatically to config/altairclient-notes.json. Click the switch to open it.",
                Category.APPS,
                "\u270E",
                () -> net.minecraft.client.Minecraft.getInstance().gui.setScreen(new NotesScreen(this)),
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
                "Dynamic Island",
                "Música en la parte de arriba",
                "Una píldora arriba al centro con la música que suena en tu PC. Con un menú abierto (p. ej. el chat) haz clic en ella, o usa su tecla, para expandirla: título, artista, barra de progreso, pausa y siguiente/anterior. Windows: lee los controles multimedia del sistema; Linux: necesita playerctl. Las notificaciones del PC (Discord, etc.) no se pueden leer desde Minecraft.",
                Category.APPS,
                "\u266A",
                () -> com.altairclient.modules.mods.DynamicIsland.toggle(),
                () -> com.altairclient.modules.mods.DynamicIsland.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Ultra Optimizer",
                "Más FPS, menos carga",
                "Baja lo que el juego tiene que dibujar: sin sombras de entidades ni oclusión ambiental, nubes simples (o sin nubes), menos partículas, menos distancia de dibujado y de entidades, y no se dibujan las criaturas que están muy por debajo de ti. Elige el nivel con - y +. Al apagarlo vuelven tus ajustes de vídeo.",
                Category.MISC,
                "\u26A1",
                () -> com.altairclient.modules.mods.UltraOptimizer.toggle(),
                () -> com.altairclient.modules.mods.UltraOptimizer.isEnabled()
        ).withSetting("Nivel", 0, com.altairclient.modules.mods.UltraOptimizer.LEVEL_NAMES.length - 1, 1,
                () -> com.altairclient.modules.mods.UltraOptimizer.getLevel(),
                v -> com.altairclient.modules.mods.UltraOptimizer.setLevel(v))
                .withSettingNames(com.altairclient.modules.mods.UltraOptimizer.LEVEL_NAMES));

        allModules.add(new ModuleEntry(
                "Hide Boss Bar",
                "Quita la barra de jefe",
                "Oculta las barras de jefe de arriba de la pantalla (las del Wither, el Dragón o las de los servidores).",
                Category.RENDER,
                "\u25AC",
                () -> com.altairclient.modules.mods.HudHider.toggleBossBar(),
                () -> com.altairclient.modules.mods.HudHider.isBossBarHidden()
        ));

        allModules.add(new ModuleEntry(
                "Hide Scoreboard",
                "Quita la tabla lateral",
                "Oculta la tabla de puntuación que sale a la derecha de la pantalla en muchos servidores.",
                Category.RENDER,
                "\u25A4",
                () -> com.altairclient.modules.mods.HudHider.toggleScoreboard(),
                () -> com.altairclient.modules.mods.HudHider.isScoreboardHidden()
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
                "Direction HUD",
                "Brújula en pantalla",
                "Muestra una brújula con las direcciones (N, E, S, W...) y los grados hacia donde miras. Se mueve en el HUD Editor.",
                Category.WORLD,
                "\u2736",
                () -> DirectionHud.getInstance().toggle(),
                () -> DirectionHud.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Server Address",
                "IP del servidor",
                "Muestra la IP del servidor en el que estás jugando. Se mueve en el HUD Editor.",
                Category.WORLD,
                "\u2301",
                () -> ServerAddress.getInstance().toggle(),
                () -> ServerAddress.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Time Changer",
                "Hora visual del día",
                "Fija la hora del día solo en tu cliente (siempre de día, de noche...). No afecta al servidor ni a otros jugadores. Elige la hora con - y +.",
                Category.RENDER,
                "\u263C",
                () -> TimeChanger.toggle(),
                () -> TimeChanger.isEnabled()
        ).withSetting("Hora", 0, TimeChanger.MODE_NAMES.length - 1, 1,
                () -> TimeChanger.getMode(), v -> TimeChanger.setMode(v))
                .withSettingNames(TimeChanger.MODE_NAMES));

        allModules.add(new ModuleEntry(
                "Weather Changer",
                "Clima visual",
                "Cambia el clima solo en tu cliente: quita la lluvia y la nieve o fuerza lluvia y tormenta. No afecta al servidor. Elige el clima con - y +.",
                Category.RENDER,
                "\u2602",
                () -> WeatherChanger.toggle(),
                () -> WeatherChanger.isEnabled()
        ).withSetting("Clima", 0, WeatherChanger.MODE_NAMES.length - 1, 1,
                () -> WeatherChanger.getMode(), v -> WeatherChanger.setMode(v))
                .withSettingNames(WeatherChanger.MODE_NAMES));

        allModules.add(new ModuleEntry(
                "Block Outline",
                "Contorno de bloque",
                "Cambia el color y el grosor del contorno del bloque que miras. Elige un color de la paleta, el Ciclo Altair (pasa por los colores de la paleta) o Arcoíris.",
                Category.RENDER,
                "\u25A2",
                () -> BlockOutline.toggle(),
                () -> BlockOutline.isEnabled()
        ).withSetting("Color", 0, com.altairclient.client.Palette.MAX_INDEX, 1,
                () -> BlockOutline.getColorIndex(), v -> BlockOutline.setColorIndex(v))
                .withSettingNames(com.altairclient.client.Palette.NAMES)
                .withExtra("Grosor", BlockOutline.MIN_WIDTH, BlockOutline.MAX_WIDTH, 1,
                        () -> BlockOutline.getWidth(), v -> BlockOutline.setWidth(v), null));

        allModules.add(new ModuleEntry(
                "Custom Crosshair",
                "Mira para PvP",
                "Sustituye la mira por un modelo para PvP: punto, cruz, cruz con hueco, círculo, T, X o esquinas. Elige estilo, color (paleta de Altair, ciclo o arcoíris) y tamaño.",
                Category.COMBAT,
                "\u2316",
                () -> CustomCrosshair.toggle(),
                () -> CustomCrosshair.isEnabled()
        ).withSetting("Estilo", 0, CustomCrosshair.STYLE_NAMES.length - 1, 1,
                () -> CustomCrosshair.getStyle(), v -> CustomCrosshair.setStyle(v))
                .withSettingNames(CustomCrosshair.STYLE_NAMES)
                .withExtra("Color", 0, com.altairclient.client.Palette.MAX_INDEX, 1,
                        () -> CustomCrosshair.getColorIndex(), v -> CustomCrosshair.setColorIndex(v),
                        com.altairclient.client.Palette.NAMES)
                .withExtra("Tamaño", CustomCrosshair.MIN_SIZE, CustomCrosshair.MAX_SIZE, 1,
                        () -> CustomCrosshair.getSize(), v -> CustomCrosshair.setSize(v), null));

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

        // ==================== CARPETA TEXTURES ====================
        for (Category cat : Category.values()) {
            if (cat.textureGroup == null) {
                continue;
            }
            for (TextureModules.Entry tex : TextureModules.group(cat.textureGroup)) {
                allModules.add(textureEntry(tex, cat));
            }
        }

        // ==================== SETTINGS FOLDER (English) ====================
        allModules.add(new ModuleEntry(
                "Auto-save",
                "Save automatically",
                "Saves your enabled modules, their settings, the texture toggles and the HUD positions every few seconds, when you close the menu and when you close the game. They are restored the next time you start. When it is off, only \"Save now\" writes the file.",
                Category.SET_GENERAL,
                "\u2699",
                AltairConfig::toggleAutoSave,
                AltairConfig::isAutoSave
        ));
        allModules.add(new ModuleEntry(
                "Title Background",
                "Starry main menu",
                "Replaces the Minecraft title screen with the Altair look: the Altair Edition logo and a night sky in deep blue and purple with twinkling stars, shooting stars and a dark hill silhouette. The Options, Singleplayer and Multiplayer screens get a starry night-photo background with slowly drifting stars. Turn it off to get the normal screens back.",
                Category.SET_GENERAL,
                "\u2726",
                AltairConfig::toggleTitleBackground,
                AltairConfig::isTitleBackground
        ));
        allModules.add(new ModuleEntry(
                "Tab Fleur-de-lis",
                "Mark Altair users",
                "Shows a fleur-de-lis before the name of every player who uses Altair Client in the TAB player list. You are always marked. Other players only appear once they are detected as Altair users.",
                Category.SET_GENERAL,
                "\u269C",
                AltairConfig::toggleTabFleur,
                AltairConfig::isTabFleur
        ));
        allModules.add(new ModuleEntry(
                "Round Buttons",
                "Dark rounded buttons",
                "Draws the normal Minecraft buttons (Singleplayer, Multiplayer, Options, Edit Server, Done...) with rounded corners in the dark Altair blue, with a purple glow when you hover them. Turn it off to get the vanilla buttons back.",
                Category.SET_GENERAL,
                "\u25A2",
                AltairConfig::toggleRoundButtons,
                AltairConfig::isRoundButtons
        ));
        allModules.add(new ModuleEntry(
                "Altair Link",
                "Find other Altair users",
                "Lets Altair Client users on the same server recognise each other through the server chat, so the TAB list can show a fleur-de-lis before their name. A few seconds after you join, the client sends one chat message made of a fleur-de-lis and invisible characters; other Altair clients hide it and answer the same way. Players without the mod see a message with just a fleur-de-lis. Some servers block or punish automatic chat messages, so turn this off if yours does not allow it. You still see the marks of other users when it is off.",
                Category.SET_GENERAL,
                "\u269C",
                AltairConfig::toggleLinkChat,
                AltairConfig::isLinkChat
        ));
        allModules.add(actionEntry("Save now", "Write the config file",
                "Saves everything right now to config/altairclient-modules.json in your Minecraft folder. Click the switch to run it.",
                AltairConfig::saveNow));
        allModules.add(actionEntry("Load saved", "Restore the saved config",
                "Reads config/altairclient-modules.json and applies it: modules, settings, texture toggles and HUD positions. Click the switch to run it.",
                AltairConfig::loadNow));
        allModules.add(actionEntry("Disable all", "Turn every module off",
                "Turns off every module and texture that is currently on. Your saved file is only changed by the next save. Click the switch to run it.",
                () -> "Disabled " + disableAll() + " modules"));

        if (!allModules.isEmpty()) {
            selectedModule = allModules.get(0);
        }
    }

    /** A card in Settings that runs an action when its switch is clicked and shows the result in its description. */
    private static ModuleEntry actionEntry(String name, String shortDesc, String desc,
                                           java.util.function.Supplier<String> action) {
        ModuleEntry[] ref = new ModuleEntry[1];
        ref[0] = new ModuleEntry(name, shortDesc, desc, Category.SET_GENERAL, "\u2699",
                () -> ref[0].fullDesc = desc + "  Result: " + action.get(), () -> false);
        return ref[0];
    }

    // ==================== Saved state (used by AltairConfig) ====================

    /** Modules that are actions or momentary views, not states: they are never saved or restored. */
    private static final java.util.Set<String> NOT_SAVED = java.util.Set.of(
            "Notes", "Freecam", "Smooth Zoom", "Perspective Mod", "Freelook", "Fullbright", "Screenshot", "Screenshot Upload");

    private static AltairScreen model;

    private static synchronized AltairScreen model() {
        if (model == null) {
            model = new AltairScreen();
        }
        return model;
    }

    private static String stateKey(ModuleEntry m) {
        return m.category.displayName + "/" + m.name;
    }

    private static boolean isSaved(ModuleEntry m) {
        return m.category.section != Section.SETTINGS && !NOT_SAVED.contains(m.name);
    }

    public static com.google.gson.JsonObject exportState() {
        com.google.gson.JsonObject out = new com.google.gson.JsonObject();
        for (ModuleEntry m : model().allModules) {
            if (!isSaved(m)) {
                continue;
            }
            com.google.gson.JsonObject o = new com.google.gson.JsonObject();
            o.addProperty("enabled", m.isEnabled());
            if (m.hasSetting()) {
                o.addProperty("setting", m.settingGetter.getAsInt());
            }
            if (!m.extras.isEmpty()) {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                for (ModuleEntry.Extra e : m.extras) {
                    arr.add(e.getter.getAsInt());
                }
                o.add("extras", arr);
            }
            out.add(stateKey(m), o);
        }
        return out;
    }

    /** Applies a saved state. Returns how many modules changed their on/off state. */
    public static int importState(com.google.gson.JsonObject state) {
        int changed = 0;
        for (ModuleEntry m : model().allModules) {
            if (!isSaved(m)) {
                continue;
            }
            com.google.gson.JsonElement el = state.get(stateKey(m));
            if (el == null || !el.isJsonObject()) {
                continue;
            }
            try {
                com.google.gson.JsonObject o = el.getAsJsonObject();
                if (o.has("setting") && m.hasSetting()) {
                    m.settingSetter.accept(o.get("setting").getAsInt());
                }
                if (o.has("extras") && o.get("extras").isJsonArray()) {
                    com.google.gson.JsonArray arr = o.getAsJsonArray("extras");
                    for (int i = 0; i < m.extras.size() && i < arr.size(); i++) {
                        m.extras.get(i).setter.accept(arr.get(i).getAsInt());
                    }
                }
                if (o.has("enabled") && o.get("enabled").getAsBoolean() != m.isEnabled()) {
                    m.toggle();
                    changed++;
                }
            } catch (RuntimeException ex) {
                com.altairclient.AltairClient.LOGGER.warn("[Altair] Could not restore {}", stateKey(m), ex);
            }
        }
        return changed;
    }

    /** Turns off every saved-type module that is on. Returns how many were turned off. */
    public static int disableAll() {
        int n = 0;
        for (ModuleEntry m : model().allModules) {
            if (isSaved(m) && m.isEnabled()) {
                m.toggle();
                n++;
            }
        }
        return n;
    }

    @Override
    public void removed() {
        super.removed();
        AltairConfig.autoSaveNow();
    }

    private static boolean isLinkedToMod(TextureModules.Entry tex) {
        return TextureModules.ID_CLEAR_WATER.equals(tex.id) || TextureModules.ID_ALTAIR_SKY.equals(tex.id)
                || TextureModules.ID_NO_ANIMATION.equals(tex.id);
    }

    /** Tarjeta de una textura: interruptor propio y, si tiene color, selector de la paleta de Altair. */
    private static ModuleEntry textureEntry(TextureModules.Entry tex, Category cat) {
        Runnable toggle = tex::toggle;
        java.util.function.BooleanSupplier enabled = tex::isEnabled;
        java.util.function.IntSupplier colorGet = tex::getColorIndex;
        java.util.function.IntConsumer colorSet = tex::setColorIndex;

        // Estas dos comparten estado con módulos que ya existen en la carpeta Mods.
        if (TextureModules.ID_CLEAR_WATER.equals(tex.id)) {
            toggle = ClearWater::toggle;
            enabled = ClearWater::isEnabled;
        } else if (TextureModules.ID_NO_ANIMATION.equals(tex.id)) {
            toggle = NoAnimation::toggle;
            enabled = NoAnimation::isEnabled;
        } else if (TextureModules.ID_ALTAIR_SKY.equals(tex.id)) {
            toggle = AltairSky::toggle;
            enabled = AltairSky::isEnabled;
        } else if (TextureModules.ID_MINIMAL_OUTLINE.equals(tex.id)) {
            toggle = BlockOutline::toggle;
            enabled = BlockOutline::isEnabled;
            colorGet = BlockOutline::getColorIndex;
            colorSet = BlockOutline::setColorIndex;
        }

        String full = tex.isImplemented() || isLinkedToMod(tex)
                ? tex.description
                : tex.description + " Aún no tiene efecto en el juego: de momento solo está en el menú.";
        ModuleEntry entry = new ModuleEntry(tex.name, tex.description, full, cat, cat.icon, toggle, enabled);
        if (TextureModules.ID_ALTAIR_SKY.equals(tex.id)) {
            entry.withSetting("Estrellas", 0, AltairSky.DENSITY_NAMES.length - 1, 1,
                    AltairSky::getDensity, AltairSky::setDensity)
                    .withSettingNames(AltairSky.DENSITY_NAMES);
        }
        if (tex.hasColor()) {
            entry.withSetting("Color", 0, Palette.MAX_INDEX, 1, colorGet, colorSet)
                    .withSettingNames(Palette.NAMES)
                    .withColor(colorGet);
        }
        return entry;
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

    private void selectCategory(Category cat) {
        currentCategory = cat;
        resetGridAnim();
        if (searchBox != null && !searchBox.getValue().isEmpty()) {
            searchBox.setValue(""); // al cambiar de apartado se limpia la búsqueda
        }
        List<ModuleEntry> mods = getModulesForCategory(cat);
        if (!mods.isEmpty()) {
            selectedModule = mods.get(0);
        }
    }

    private void onSearchChanged(String text) {
        searchQuery = text == null ? "" : text.trim();
        resetGridAnim();
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
        if (visibleCache != null && visibleCat == currentCategory && searchQuery.equals(visibleQuery)) {
            return visibleCache;
        }
        List<ModuleEntry> fresh = computeVisibleModules();
        visibleCache = fresh;
        visibleCat = currentCategory;
        visibleQuery = searchQuery;
        return fresh;
    }

    private List<ModuleEntry> computeVisibleModules() {
        if (searchQuery.isEmpty()) {
            return getModulesForCategory(currentCategory);
        }
        String q = searchQuery.toLowerCase(java.util.Locale.ROOT);
        List<ModuleEntry> list = new ArrayList<>();
        for (ModuleEntry m : allModules) {
            if (m.name.toLowerCase(java.util.Locale.ROOT).contains(q)
                    || m.shortDesc.toLowerCase(java.util.Locale.ROOT).contains(q)
                    || m.category.displayName.toLowerCase(java.util.Locale.ROOT).contains(q)
                    || m.category.section.displayName.toLowerCase(java.util.Locale.ROOT).contains(q)) {
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
        // Animación de apertura: el fondo se oscurece y el panel crece desde el 94 %.
        float open = Anim.easeOut((System.nanoTime() - openStart) / 280_000_000.0F);
        boolean opening = open < 1.0F;
        graphics.fill(0, 0, width, height, Anim.scaleAlpha(OVERLAY_BG, open));
        if (opening) {
            float pivotX = panelX + panelWidth / 2.0F;
            float pivotY = panelY + panelHeight / 2.0F;
            float openScale = 0.94F + 0.06F * open;
            graphics.pose().pushMatrix();
            graphics.pose().translate(pivotX, pivotY);
            graphics.pose().scale(openScale, openScale);
            graphics.pose().translate(-pivotX, -pivotY);
        }

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
        // Engranaje de ajustes (abre Settings)
        int gearX = winCloseX - 56;
        boolean gearHover = mouseX >= gearX - 3 && mouseX <= gearX + 13 && mouseY >= winBtnY - 1 && mouseY <= winBtnY + 15;
        boolean inSettings = currentCategory.section == Section.SETTINGS;
        if (gearHover || inSettings) {
            graphics.fill(gearX - 3, winBtnY - 1, gearX + 13, winBtnY + 15, gearHover ? 0xFF272952 : 0xFF1F2750);
        }
        graphics.text(font, "\u2699", gearX + 1, winBtnY + 3, (gearHover || inSettings) ? CYAN : MUTED, false);
        graphics.text(font, "—", winCloseX - 32, winBtnY + 2, MUTED, false);
        graphics.text(font, "□", winCloseX - 16, winBtnY + 2, MUTED, false);
        graphics.text(font, "✕", winCloseX, winBtnY + 2, closeHover ? RED : MUTED, false);

        // ==================== 4. SIDEBAR IZQUIERDA ====================
        int sidebarWidth = SIDEBAR_WIDTH;
        int contentY = panelY + headerHeight + 1;

        graphics.fill(panelX, contentY, panelX + sidebarWidth, panelY + panelHeight, SIDEBAR_BG);
        graphics.fill(panelX + sidebarWidth, contentY, panelX + sidebarWidth + 1, panelY + panelHeight, BORDER_COLOR);

        // Lista de carpetas (Mods / Textures) y, dentro de la abierta, sus apartados.
        // Se recorta justo encima del botón HUD Editor y se puede desplazar con la rueda.
        int sideTop = contentY + 4;
        int sideBottom = panelY + panelHeight - 46;
        List<SideRow> sideRows = sidebarRows(contentY + 10);
        SideRow lastRow = sideRows.get(sideRows.size() - 1);
        sidebarMaxScroll = Math.max(0, lastRow.y + lastRow.h + 6 - sideBottom);
        sidebarScroll = Math.max(0, Math.min(sidebarMaxScroll, sidebarScroll));

        graphics.enableScissor(panelX + 1, sideTop, panelX + sidebarWidth, sideBottom);
        for (SideRow row : sideRows) {
            int ry = row.y - sidebarScroll;
            if (ry + row.h < sideTop || ry > sideBottom) {
                continue;
            }
            boolean isHovered = mouseX >= panelX + 8 && mouseX <= panelX + sidebarWidth - 8
                    && mouseY >= Math.max(ry, sideTop) && mouseY <= Math.min(ry + row.h, sideBottom);

            if (row.category == null) {
                // Carpeta: flecha ▸ / ▾ según esté cerrada o abierta
                boolean folderOpen = row.section == openSection;
                float hov = secHov[row.section.ordinal()].update(isHovered ? 1.0F : 0.0F, 18.0F);
                int bg = Anim.mix(folderOpen ? 0xFF1F2750 : 0xFF181F3B, 0xFF272952, hov);
                graphics.fill(panelX + 8, ry, panelX + sidebarWidth - 8, ry + row.h, bg);
                graphics.fill(panelX + 8, ry, panelX + 11, ry + row.h, folderOpen ? CYAN : PURPLE);
                int textColor = Anim.mix(folderOpen ? WHITE : MUTED, CYAN, hov);
                graphics.text(font, (folderOpen ? "▾ " : "▸ ") + row.section.displayName, panelX + 18, ry + 8, textColor, false);
            } else {
                // Apartado dentro de la carpeta abierta, con sangría
                Category cat = row.category;
                boolean isSelected = (cat == currentCategory);
                float sel = catSel[cat.ordinal()].update(isSelected ? 1.0F : 0.0F, 14.0F);
                float hov = catHov[cat.ordinal()].update(isHovered ? 1.0F : 0.0F, 18.0F);

                int bg = Anim.mix(Anim.mix(0x001B2240, 0xFF1B2240, hov), 0xFF272952, sel);
                if ((bg >>> 24) > 0) {
                    graphics.fill(panelX + 14, ry, panelX + sidebarWidth - 8, ry + row.h, bg);
                }
                int barW = Math.round(3.0F * sel);
                if (barW > 0) {
                    graphics.fill(panelX + 14, ry, panelX + 14 + barW, ry + row.h, PURPLE);
                }
                int textColor = Anim.mix(Anim.mix(MUTED, CYAN, hov), WHITE, sel);
                int textX = panelX + 22 + Math.round(2.0F * sel);
                graphics.text(font, fit(cat.icon + " " + cat.shortName, panelX + sidebarWidth - 12 - textX), textX, ry + 6, textColor, false);
            }
        }
        graphics.disableScissor();

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

        // Al elegir otro módulo, sus detalles se deslizan y aparecen con un fundido.
        if (selectedModule != lastDrawnSelected) {
            lastDrawnSelected = selectedModule;
            selectStart = System.nanoTime();
        }
        float selectT = Anim.easeOut((System.nanoTime() - selectStart) / 300_000_000.0F);

        if (selectedModule != null) {
            graphics.pose().pushMatrix();
            graphics.pose().translate((1.0F - selectT) * 10.0F, 0.0F);
            int iconBoxY = contentY + 16;
            graphics.text(font, selectedModule.icon, rightPanelX + 16, iconBoxY + 2, selectedModule.accent(), false);
            graphics.text(font, fit(selectedModule.name, rightPanelWidth - 34 - 14), rightPanelX + 34, iconBoxY + 2, WHITE, false);
            graphics.text(font, fit(selectedModule.category.displayName + " • " + selectedModule.category.section.kind, rightPanelWidth - 32),
                    rightPanelX + 16, iconBoxY + 17, MUTED, false);

            graphics.fill(rightPanelX + 14, iconBoxY + 31, panelX + panelWidth - 14, iconBoxY + 32, BORDER_COLOR);

            int descY = iconBoxY + 40;
            List<FormattedCharSequence> wrappedLines = splitCached(selectedModule.fullDesc, rightPanelWidth - 28);
            for (FormattedCharSequence line : wrappedLines) {
                if (descY + 10 > panelY + panelHeight - (selectedModule.hasSetting() ? 105 + 20 * selectedModule.extras.size() : 65)) break;
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

                for (int ei = 0; ei < selectedModule.extras.size(); ei++) {
                    ModuleEntry.Extra extra = selectedModule.extras.get(ei);
                    int ey = setY - 20 * (ei + 1);
                    boolean emHover = mouseX >= minusX && mouseX <= minusX + 16 && mouseY >= ey && mouseY <= ey + 14;
                    boolean epHover = mouseX >= plusX && mouseX <= plusX + 16 && mouseY >= ey && mouseY <= ey + 14;
                    graphics.text(font, fit(extra.label + ": " + extra.text(), minusX - (rightPanelX + 14) - 6),
                            rightPanelX + 14, ey + 3, WHITE, false);
                    graphics.fill(minusX, ey, minusX + 16, ey + 14, emHover ? 0xFF2E3550 : 0xFF22283D);
                    graphics.fill(plusX, ey, plusX + 16, ey + 14, epHover ? 0xFF2E3550 : 0xFF22283D);
                    graphics.text(font, "-", minusX + 6, ey + 3, WHITE, false);
                    graphics.text(font, "+", plusX + 5, ey + 3, WHITE, false);
                }
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
            float pillT = selectedModule.togAnim.update(isModEnabled ? 1.0F : 0.0F, 16.0F);
            graphics.fill(pillX, pillY, pillX + 20, pillY + 12, Anim.mix(0xFF4A557A, GREEN, pillT));
            int knobX = pillX + 2 + Math.round(8.0F * pillT);
            graphics.fill(knobX, pillY + 2, knobX + 8, pillY + 10, WHITE);

            graphics.pose().popMatrix();
            if (selectT < 1.0F) {
                graphics.fill(rightPanelX + 1, contentY, panelX + panelWidth - 1, panelY + panelHeight - 1,
                        Anim.scaleAlpha(0xFF141B33, 1.0F - selectT));
            }
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
        scrollTarget = Anim.clamp(scrollTarget, 0.0F, maxScroll);
        scrollOffset = Math.max(0, Math.min(maxScroll, Math.round(scrollSmooth.update(scrollTarget, 16.0F))));

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

            // Selección, hover e interruptor cambian con transición; al cambiar de categoría las tarjetas suben escalonadas.
            float selA = entry.selAnim.update(isSelected ? 1.0F : 0.0F, 12.0F);
            float hovA = entry.hovAnim.update(isHovered ? 1.0F : 0.0F, 16.0F);
            float togA = entry.togAnim.update(isEnabled ? 1.0F : 0.0F, 16.0F);
            float enter = Anim.easeOut(((System.nanoTime() - gridAnimStart) / 1.0E9F - i * 0.03F) / 0.3F);
            cy += Math.round((1.0F - enter) * 14.0F) - Math.round(hovA * 2.0F);

            int cardBgColor = Anim.mix(Anim.mix(CARD_BG, CARD_HOVER_BG, hovA), CARD_SELECTED_BG, selA);
            graphics.fill(cx, cy, cx + cardWidth, cy + cardHeight, cardBgColor);

            int borderCol = Anim.mix(Anim.mix(BORDER_COLOR, CYAN, hovA), PURPLE, selA);
            graphics.fill(cx, cy, cx + cardWidth, cy + 1, borderCol);
            graphics.fill(cx, cy + cardHeight - 1, cx + cardWidth, cy + cardHeight, borderCol);
            graphics.fill(cx, cy, cx + 1, cy + cardHeight, borderCol);
            graphics.fill(cx + cardWidth - 1, cy, cx + cardWidth, cy + cardHeight, borderCol);

            // Textos con 8 px de margen a cada lado, recortados si no caben
            int textWidth = cardWidth - 16;
            int accent = entry.accent();
            graphics.text(font, entry.icon, cx + 8, cy + 8, accent, false);
            graphics.text(font, fit(entry.name, textWidth - 14), cx + 22, cy + 8, WHITE, false);
            if (entry.colorSupplier != null) {
                // Cuadrado con el color de la paleta que usa la textura (abajo a la izquierda, con el mismo margen)
                graphics.fill(cx + 8, cy + cardHeight - 18, cx + 18, cy + cardHeight - 8, BORDER_COLOR);
                graphics.fill(cx + 9, cy + cardHeight - 17, cx + 17, cy + cardHeight - 9, accent);
            }

            List<FormattedCharSequence> descLines = splitCached(entry.shortDesc, textWidth);
            for (int l = 0; l < Math.min(2, descLines.size()); l++) {
                graphics.text(font, descLines.get(l), cx + 8, cy + 23 + l * 11, MUTED, false);
            }

            // Switch toggle en la tarjeta
            int toggleW = 24;
            int toggleH = 12;
            int toggleX = cx + cardWidth - toggleW - 8;
            int toggleY = cy + cardHeight - toggleH - 8;

            int switchBg = Anim.mix(0xFF353C57, PURPLE, togA);
            graphics.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, switchBg);

            int knobPos = toggleX + 2 + Math.round(11.0F * togA);
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

        if (opening) {
            graphics.pose().popMatrix();
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
        ServerAddress.getInstance().render(graphics);
        DirectionHud.getInstance().render(graphics);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Rueda sobre la barra izquierda: desplaza las carpetas
        if (mouseX < panelX + SIDEBAR_WIDTH && sidebarMaxScroll > 0) {
            sidebarScroll = Math.max(0, Math.min(sidebarMaxScroll, sidebarScroll - (int) (scrollY * 16)));
            return true;
        }
        if (maxScroll > 0) {
            scrollTarget = Anim.clamp(scrollTarget - (float) (scrollY * 22), 0.0F, maxScroll);
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

        // 0. Clic en el engranaje de ajustes
        int gearBtnY = panelY + 10;
        int gearBtnX = panelX + panelWidth - 24 - 56;
        if (mx >= gearBtnX - 3 && mx <= gearBtnX + 13 && my >= gearBtnY - 1 && my <= gearBtnY + 15) {
            openSection = Section.SETTINGS;
            selectCategory(Category.SET_GENERAL);
            return true;
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

        // 3. Clic en las carpetas (Mods / Textures) y sus apartados
        int headerHeight = 36;
        int contentY = panelY + headerHeight + 1;
        int sideTop = contentY + 4;
        int sideBottom = panelY + panelHeight - 46;
        if (mx >= panelX + 8 && mx <= panelX + sidebarWidth - 8 && my >= sideTop && my <= sideBottom) {
            for (SideRow row : sidebarRows(contentY + 10)) {
                int ry = row.y - sidebarScroll;
                if (my < ry || my > ry + row.h) {
                    continue;
                }
                if (row.category == null) {
                    if (row.section == openSection) {
                        openSection = null; // clic en la carpeta abierta: se cierra
                    } else {
                        openSection = row.section;
                        for (Category cat : Category.values()) {
                            if (cat.section == row.section) {
                                selectCategory(cat); // al abrir una carpeta sale su primer apartado
                                break;
                            }
                        }
                    }
                } else {
                    selectCategory(row.category);
                }
                return true;
            }
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
            for (int ei = 0; ei < selectedModule.extras.size(); ei++) {
                int ey = setY - 20 * (ei + 1);
                if (my >= ey && my <= ey + 14) {
                    if (mx >= minusX && mx <= minusX + 16) {
                        selectedModule.extras.get(ei).adjust(-1);
                        return true;
                    }
                    if (mx >= plusX && mx <= plusX + 16) {
                        selectedModule.extras.get(ei).adjust(1);
                        return true;
                    }
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
