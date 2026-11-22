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
import com.altairclient.modules.mods.ClockHud;
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
import com.altairclient.modules.mods.ExtraHacks;
import com.altairclient.modules.mods.Hacks;
import com.altairclient.modules.mods.RenderHacks;
import com.altairclient.modules.mods.WorldHacks;
import com.altairclient.modules.mods.KillAura;
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
    private static final int OVERLAY_BG = 0x70080B14;
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
        MODS("Mods", "Module"),
        TEXTURES("Textures", "Texture"),
        HACKS("Hacks", "Hack"),
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
        TEX_EXTRA(Section.TEXTURES, TextureModules.EXTRA, "Extra Visual Effects", "Extra", "✦"),
        TEX_MISC(Section.TEXTURES, TextureModules.MISC, "Misc", "Misc", "✺"),

        // Carpeta Hacks
        HACK_COMBAT(Section.HACKS, null, "Hack Combat", "Combat", "\u2694"),
        HACK_MOVE(Section.HACKS, null, "Hack Movement", "Movement", "\u00BB"),
        HACK_PLAYER(Section.HACKS, null, "Hack Player", "Player", "\u2726"),
        HACK_RENDER(Section.HACKS, null, "Hack Render", "Render", "\u25C9"),
        HACK_WORLD(Section.HACKS, null, "Hack World", "World", "\u25A6"),

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

    private final java.util.HashMap<String, String> fitCache = new java.util.HashMap<>();

    /** Recorta el texto con "..." si no cabe; el resultado se guarda para no medir fuentes en cada frame. */
    private String fit(String text, int maxWidth) {
        if (maxWidth <= 0) {
            return text;
        }
        String key = maxWidth + "|" + text;
        String cached = fitCache.get(key);
        if (cached != null) {
            return cached;
        }
        String result = text;
        if (font.width(text) > maxWidth) {
            result = font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("\u2026"))) + "\u2026";
        }
        if (fitCache.size() > 1024) {
            fitCache.clear();
        }
        fitCache.put(key, result);
        return result;
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
                "Hit streak",
                "Counts the consecutive hits you land on your opponents in combat. Resets automatically after 2.5s or when you take damage.",
                Category.COMBAT,
                "⚔",
                () -> ComboCounter.getInstance().toggle(),
                () -> ComboCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Reach Display",
                "Hit distance",
                "Shows the exact distance in blocks from which you land each attack on the target's hitbox.",
                Category.COMBAT,
                "⌖",
                () -> ReachDisplay.getInstance().toggle(),
                () -> ReachDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Hit Indicator",
                "Crosshair hit marker",
                "Draws a diagonal hitmarker around the crosshair with a quick animation when you hit an enemy.",
                Category.COMBAT,
                "✦",
                () -> HitIndicator.getInstance().toggle(),
                () -> HitIndicator.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "CPS Counter",
                "Clicks per second",
                "Tracks and shows in real time how fast you click the left and right mouse buttons (LMB | RMB).",
                Category.COMBAT,
                "🖱",
                () -> CPSCounter.getInstance().toggle(),
                () -> CPSCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Health Display",
                "Target hearts",
                "Shows the number of hearts of the entity or player you are aiming at (1 heart = 2 health). If you aren't aiming at anyone, it shows yours.",
                Category.COMBAT,
                "\u2665",
                () -> HealthDisplay.getInstance().toggle(),
                () -> HealthDisplay.getInstance().isEnabled()
        ));

        // ==================== RENDER ====================
        allModules.add(new ModuleEntry(
                "FPS Display",
                "FPS counter",
                "Shows Minecraft's frames per second on screen with smooth updates and a customizable position.",
                Category.RENDER,
                "🖥",
                () -> FPSDisplay.getInstance().toggle(),
                () -> FPSDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Hitbox Display",
                "Visible hitboxes",
                "Draws the hitboxes and sight lines of all entities in the world with no performance impact.",
                Category.RENDER,
                "📦",
                () -> HitboxDisplay.getInstance().toggle(),
                () -> HitboxDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Armor HUD",
                "Armor durability",
                "Shows your equipped armor pieces (helmet, chestplate, leggings and boots) with their percentage and durability bar.",
                Category.RENDER,
                "🛡",
                () -> ArmorHUD.getInstance().toggle(),
                () -> ArmorHUD.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Keystrokes",
                "Live keys",
                "An interactive on-screen display of the WASD keys, left and right clicks and the space bar.",
                Category.RENDER,
                "⌨",
                () -> Keystrokes.getInstance().toggle(),
                () -> Keystrokes.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Ping Display",
                "Server latency",
                "Shows your latency (ping) in milliseconds with a color that smoothly shifts from green to yellow to red as it rises.",
                Category.RENDER,
                "\u25C9",
                () -> PingDisplay.getInstance().toggle(),
                () -> PingDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Freecam",
                "Free camera",
                "Your player stays still and the camera flies freely like in spectator mode. Fly with W/A/S/D, Space goes up, Shift goes down and Ctrl speeds up. Enable it here or with the Freecam key (V by default, configurable in Controls). It is visual only: it does not move your player on the server.",
                Category.RENDER,
                "\u25CE",
                () -> FreecamHandler.toggle(),
                () -> FreecamHandler.isActive()
        ));

        // ==================== PLAYER ====================
        allModules.add(new ModuleEntry(
                "Potion Status",
                "Active effects",
                "Shows the list of potions and effects on your player, with name, level and remaining time formatted as MM:SS.",
                Category.PLAYER,
                "🧪",
                () -> PotionStatus.getInstance().toggle(),
                () -> PotionStatus.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Smooth Zoom",
                "Cinematic zoom",
                "Smoothly zooms in your view while you hold the C key, with dynamic field of view (FOV) interpolation.",
                Category.PLAYER,
                "🔍",
                null,
                () -> true
        ));

        allModules.add(new ModuleEntry(
                "Custom FOV",
                "Custom FOV",
                "Uses your own field of view instead of the one from the video options. Adjust the value with the - and + buttons (30 to 110).",
                Category.RENDER,
                "\u25CD",
                () -> FovHandler.toggleCustom(),
                () -> FovHandler.isCustomEnabled()
        ).withSetting("FOV", FovHandler.MIN_FOV, FovHandler.MAX_FOV, 5,
                () -> FovHandler.getCustomFov(), v -> FovHandler.setCustomFov(v)));

        allModules.add(new ModuleEntry(
                "Dynamic FOV",
                "Dynamic changes",
                "On: the FOV changes when sprinting or with speed effects, like in vanilla. Off: the FOV stays fixed.",
                Category.RENDER,
                "\u2194",
                () -> FovHandler.toggleDynamic(),
                () -> FovHandler.isDynamicEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Perspective Mod",
                "Look around",
                "Hold the Perspective Mod key (Left Alt by default, configurable in Controls) and move the mouse: the camera switches to third person and orbits without turning your player. It is visual only and does not send rotations to the server.",
                Category.RENDER,
                "\u25CE",
                () -> PerspectiveHandler.toggle(),
                () -> PerspectiveHandler.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Toggle Sprint",
                "Auto sprint",
                "You sprint automatically while moving forward, without holding the sprint key. Assign a shortcut in Controls if you like.",
                Category.PLAYER,
                "\u00BB",
                () -> ToggleSprint.toggle(),
                () -> ToggleSprint.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Toggle Sneak",
                "Auto sneak",
                "You stay sneaking automatically, without holding the sneak key. Assign a shortcut in Controls if you like.",
                Category.PLAYER,
                "\u2193",
                () -> ToggleSneak.toggle(),
                () -> ToggleSneak.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Screenshot",
                "Take a screenshot",
                "Takes a screenshot of the game without the menu and saves it in the screenshots folder. Also available with the Screenshot key (F9 by default, configurable in Controls).",
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
                "Upload last screenshot",
                "Sends your latest screenshot to your own Discord webhook. Put the webhook URL in the altairclient-webhook.txt file (Minecraft folder). Also available with the Upload screenshot key (unbound by default, configurable in Controls).",
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
                "Fullbright",
                "Maximum brightness",
                "Raises brightness to the maximum so you can see in caves and at night. It uses the game's own brightness setting: it adds no potion effects and sends nothing to the server. If the server forbids fullbright, don't enable it. When you turn it off, your original brightness returns.",
                Category.RENDER,
                "\u2600",
                () -> FullbrightHandler.toggle(),
                () -> FullbrightHandler.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Dynamic Island",
                "Music at the top",
                "A pill at the top center showing the music playing on your PC. With a menu open (e.g. the chat) click it, or use its key, to expand it: title, artist, progress bar, pause and next/previous. Windows: reads the system media controls; Linux: needs playerctl. PC notifications (Discord, etc.) cannot be read from Minecraft.",
                Category.APPS,
                "\u266A",
                () -> com.altairclient.modules.mods.DynamicIsland.toggle(),
                () -> com.altairclient.modules.mods.DynamicIsland.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Ultra Optimizer",
                "More FPS, less load",
                "Lowers what the game has to draw: no entity shadows or ambient occlusion, simple clouds (or none), fewer particles, shorter render and entity distance, and mobs far below you are not drawn. Choose the level with - and +. When you turn it off, your video settings return.",
                Category.MISC,
                "\u26A1",
                () -> com.altairclient.modules.mods.UltraOptimizer.toggle(),
                () -> com.altairclient.modules.mods.UltraOptimizer.isEnabled()
        ).withSetting("Level", 0, com.altairclient.modules.mods.UltraOptimizer.LEVEL_NAMES.length - 1, 1,
                () -> com.altairclient.modules.mods.UltraOptimizer.getLevel(),
                v -> com.altairclient.modules.mods.UltraOptimizer.setLevel(v))
                .withSettingNames(com.altairclient.modules.mods.UltraOptimizer.LEVEL_NAMES));

        allModules.add(new ModuleEntry(
                "Hide Boss Bar",
                "Remove the boss bar",
                "Hides the boss bars at the top of the screen (Wither, Dragon or server bars).",
                Category.RENDER,
                "\u25AC",
                () -> com.altairclient.modules.mods.HudHider.toggleBossBar(),
                () -> com.altairclient.modules.mods.HudHider.isBossBarHidden()
        ));

        allModules.add(new ModuleEntry(
                "Hide Scoreboard",
                "Remove the sidebar",
                "Hides the scoreboard shown on the right side of the screen on many servers.",
                Category.RENDER,
                "\u25A4",
                () -> com.altairclient.modules.mods.HudHider.toggleScoreboard(),
                () -> com.altairclient.modules.mods.HudHider.isScoreboardHidden()
        ));

        allModules.add(new ModuleEntry(
                "Custom Nametags",
                "Styled names",
                "Changes the look of the names above players and entities: Altair, Gold, Neon or Marked. Only you see it; nobody's real name changes. Choose the style with - and +. Optional key in Controls.",
                Category.RENDER,
                "\u25C6",
                () -> NametagHandler.toggle(),
                () -> NametagHandler.isEnabled()
        ).withSetting("Style", 0, NametagHandler.STYLE_COUNT - 1, 1,
                () -> NametagHandler.getStyle(), v -> NametagHandler.setStyle(v))
                .withSettingNames(NametagHandler.STYLE_NAMES));

        allModules.add(new ModuleEntry(
                "Better Chat Bubbles",
                "Chat bubbles",
                "What a player writes appears for a few seconds above their head, under their name. Only you see it. Optional key in Controls.",
                Category.RENDER,
                "\uD83D\uDCAC",
                () -> ChatBubbleHandler.toggle(),
                () -> ChatBubbleHandler.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Damage Indicator",
                "Damage numbers",
                "Every time you damage an entity, the damage appears as floating numbers that fade away, Fortnite style. Move the spawn point in the HUD Editor.",
                Category.COMBAT,
                "\u2736",
                () -> DamageIndicator.getInstance().toggle(),
                () -> DamageIndicator.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Totem Counter",
                "Totems in inventory",
                "Counts the totems of undying you carry, including the offhand. Movable in the HUD Editor.",
                Category.COMBAT,
                "\u2605",
                () -> TotemCounter.getInstance().toggle(),
                () -> TotemCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Arrow Counter",
                "Arrows in inventory",
                "Counts the arrows (normal, spectral and tipped) you carry. Movable in the HUD Editor.",
                Category.COMBAT,
                "\u27B3",
                () -> ArrowCounter.getInstance().toggle(),
                () -> ArrowCounter.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Memory Display",
                "RAM used",
                "Shows the RAM the game uses as a percentage, as a graph of the last few seconds, or both. Choose the mode with - and +. Movable in the HUD Editor.",
                Category.WORLD,
                "\u25A6",
                () -> MemoryDisplay.getInstance().toggle(),
                () -> MemoryDisplay.getInstance().isEnabled()
        ).withSetting("Mode", 0, MemoryDisplay.MODE_NAMES.length - 1, 1,
                () -> MemoryDisplay.getInstance().getMode(), v -> MemoryDisplay.getInstance().setMode(v))
                .withSettingNames(MemoryDisplay.MODE_NAMES));

        allModules.add(new ModuleEntry(
                "Direction HUD",
                "On-screen compass",
                "Shows a compass with the directions (N, E, S, W...) and the degrees you are facing. Movable in the HUD Editor.",
                Category.WORLD,
                "\u2736",
                () -> DirectionHud.getInstance().toggle(),
                () -> DirectionHud.getInstance().isEnabled()
        ).withSetting("Size %", 50, 200, 10,
                () -> DirectionHud.getInstance().getSizePercent(), v -> DirectionHud.getInstance().setSizePercent(v)));

        allModules.add(new ModuleEntry(
                "Kill Aura",
                "Auto-hit nearby targets",
                "Automatically hits the closest target within range. Range is the main setting (blocks); Targets chooses mobs, players or both; Interval is the number of ticks between hits (20 ticks = 1 second). Many servers ban this, so only use it where it is allowed.",
                Category.HACK_COMBAT,
                "\u2694",
                () -> KillAura.toggle(),
                () -> KillAura.isEnabled()
        ).withSetting("Range", KillAura.MIN_RANGE, KillAura.MAX_RANGE, 1,
                () -> KillAura.getRange(), v -> KillAura.setRange(v))
                .withExtra("Targets", 0, KillAura.TARGET_NAMES.length - 1, 1,
                        () -> KillAura.getTargets(), v -> KillAura.setTargets(v), KillAura.TARGET_NAMES)
                .withExtra("Interval", KillAura.MIN_INTERVAL, KillAura.MAX_INTERVAL, 1,
                        () -> KillAura.getInterval(), v -> KillAura.setInterval(v), null));

        allModules.add(new ModuleEntry(
                "Auto Crit",
                "Automatic critical hits",
                "Makes your hits critical. With Kill Aura it jumps and hits on the way down; when you hold attack on a target it jumps for you.",
                Category.HACK_COMBAT,
                "\u2694",
                () -> Hacks.AUTO_CRIT.toggle(),
                () -> Hacks.AUTO_CRIT.enabled
        ));

        allModules.add(new ModuleEntry(
                "Velocity",
                "Less knockback",
                "Reduces the knockback you take. Horizontal and Vertical are the percent of knockback you keep (0 = none). Servers may still move you.",
                Category.HACK_COMBAT,
                "\u21E9",
                () -> Hacks.VELOCITY.toggle(),
                () -> Hacks.VELOCITY.enabled
        ).withSetting("Horizontal %", 0, 100, 10, () -> Hacks.VELOCITY.a, v -> Hacks.VELOCITY.a = v).withExtra("Vertical %", 0, 100, 10, () -> Hacks.VELOCITY.b, v -> Hacks.VELOCITY.b = v, null));

        allModules.add(new ModuleEntry(
                "Reach",
                "Longer attack range",
                "Raises how far you can hit entities (in blocks). The server can reject hits that are too far.",
                Category.HACK_COMBAT,
                "\u2194",
                () -> Hacks.REACH.toggle(),
                () -> Hacks.REACH.enabled
        ).withSetting("Blocks", 3, 6, 1, () -> Hacks.REACH.a, v -> Hacks.REACH.a = v));

        allModules.add(new ModuleEntry(
                "Hitboxes",
                "Bigger enemy hitboxes",
                "Makes other entities easier to hit by growing the area your crosshair checks (in tenths of a block).",
                Category.HACK_COMBAT,
                "\u25A3",
                () -> Hacks.HITBOXES.toggle(),
                () -> Hacks.HITBOXES.enabled
        ).withSetting("Size /10", 0, 10, 1, () -> Hacks.HITBOXES.a, v -> Hacks.HITBOXES.a = v));

        allModules.add(new ModuleEntry(
                "Auto Totem",
                "Totem in off hand",
                "Keeps a Totem of Undying in your off hand, taking one from your inventory when needed. Needs no screen open.",
                Category.HACK_COMBAT,
                "\u2726",
                () -> Hacks.AUTO_TOTEM.toggle(),
                () -> Hacks.AUTO_TOTEM.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Armor",
                "Equips empty armor slots",
                "Puts armor from your inventory into any empty armor slot.",
                Category.HACK_COMBAT,
                "\u25A4",
                () -> Hacks.AUTO_ARMOR.toggle(),
                () -> Hacks.AUTO_ARMOR.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Gapple",
                "Eats a golden apple when low",
                "When your health (in points, 2 = 1 heart) falls to the setting, it holds a golden apple from your hotbar until you are healthy, then goes back to your slot.",
                Category.HACK_COMBAT,
                "\u2764",
                () -> Hacks.AUTO_GAPPLE.toggle(),
                () -> Hacks.AUTO_GAPPLE.enabled
        ).withSetting("Health", 2, 18, 2, () -> Hacks.AUTO_GAPPLE.a, v -> Hacks.AUTO_GAPPLE.a = v));

        allModules.add(new ModuleEntry(
                "Aim Assist",
                "Soft aim correction",
                "While you hold attack, gently turns your view toward the closest target in range. Strength is how fast it corrects.",
                Category.HACK_COMBAT,
                "\u25CE",
                () -> Hacks.AIM_ASSIST.toggle(),
                () -> Hacks.AIM_ASSIST.enabled
        ).withSetting("Range", 2, 6, 1, () -> Hacks.AIM_ASSIST.a, v -> Hacks.AIM_ASSIST.a = v).withExtra("Strength %", 10, 100, 10, () -> Hacks.AIM_ASSIST.b, v -> Hacks.AIM_ASSIST.b = v, null));

        allModules.add(new ModuleEntry(
                "Crystal Aura",
                "Auto end crystals",
                "Breaks end crystals near an enemy player and places new ones on obsidian or bedrock (hold end crystals). Range is the main setting; Interval is ticks between actions. Be careful: crystals hurt you too.",
                Category.HACK_COMBAT,
                "\u2736",
                () -> Hacks.CRYSTAL_AURA.toggle(),
                () -> Hacks.CRYSTAL_AURA.enabled
        ).withSetting("Range", 2, 6, 1, () -> Hacks.CRYSTAL_AURA.a, v -> Hacks.CRYSTAL_AURA.a = v).withExtra("Interval", 1, 20, 1, () -> Hacks.CRYSTAL_AURA.b, v -> Hacks.CRYSTAL_AURA.b = v, null));

        allModules.add(new ModuleEntry(
                "Fly",
                "Fly like a bird",
                "Vanilla = move freely (Jump up, Sneak down), Glide = fall very slowly, Creative = the creative flight. Speed is the second setting.",
                Category.HACK_MOVE,
                "\u2708",
                () -> Hacks.FLY.toggle(),
                () -> Hacks.FLY.enabled
        ).withSetting("Mode", 0, 2, 1, () -> Hacks.FLY.a, v -> Hacks.FLY.a = v).withSettingNames(Hacks.FLY_MODES).withExtra("Speed", 1, 10, 1, () -> Hacks.FLY.b, v -> Hacks.FLY.b = v, null));

        allModules.add(new ModuleEntry(
                "Speed",
                "Move faster",
                "Boosts your walking speed. Strength 1 to 5.",
                Category.HACK_MOVE,
                "\u00BB",
                () -> Hacks.SPEED.toggle(),
                () -> Hacks.SPEED.enabled
        ).withSetting("Strength", 1, 5, 1, () -> Hacks.SPEED.a, v -> Hacks.SPEED.a = v));

        allModules.add(new ModuleEntry(
                "Sprint",
                "Always sprint",
                "Sprints automatically while you walk forward.",
                Category.HACK_MOVE,
                "\u21E8",
                () -> Hacks.SPRINT.toggle(),
                () -> Hacks.SPRINT.enabled
        ));

        allModules.add(new ModuleEntry(
                "Step",
                "Climb blocks",
                "Lets you walk up blocks without jumping. Height in blocks.",
                Category.HACK_MOVE,
                "\u2B06",
                () -> Hacks.STEP.toggle(),
                () -> Hacks.STEP.enabled
        ).withSetting("Height", 1, 5, 1, () -> Hacks.STEP.a, v -> Hacks.STEP.a = v));

        allModules.add(new ModuleEntry(
                "Jesus",
                "Walk on water",
                "Keeps you on top of water. Sneak to sink.",
                Category.HACK_MOVE,
                "\u2248",
                () -> Hacks.JESUS.toggle(),
                () -> Hacks.JESUS.enabled
        ));

        allModules.add(new ModuleEntry(
                "Spider",
                "Climb walls",
                "Climb any wall you are touching. Sneak to stop.",
                Category.HACK_MOVE,
                "\u2191",
                () -> Hacks.SPIDER.toggle(),
                () -> Hacks.SPIDER.enabled
        ));

        allModules.add(new ModuleEntry(
                "Elytra Fly",
                "Better elytra",
                "While gliding: hold forward to fly fast along where you look, hold sneak to hover. Speed is the setting.",
                Category.HACK_MOVE,
                "\u2742",
                () -> Hacks.ELYTRA_FLY.toggle(),
                () -> Hacks.ELYTRA_FLY.enabled
        ).withSetting("Speed", 1, 10, 1, () -> Hacks.ELYTRA_FLY.a, v -> Hacks.ELYTRA_FLY.a = v));

        allModules.add(new ModuleEntry(
                "Boat Fly",
                "Fly in boats",
                "While riding a boat or minecart: Jump goes up, Sneak goes down, and you steer in the air. Speed is the setting.",
                Category.HACK_MOVE,
                "\u26F5",
                () -> Hacks.BOAT_FLY.toggle(),
                () -> Hacks.BOAT_FLY.enabled
        ).withSetting("Speed", 1, 10, 1, () -> Hacks.BOAT_FLY.a, v -> Hacks.BOAT_FLY.a = v));

        allModules.add(new ModuleEntry(
                "Phase",
                "Walk through blocks",
                "Lets you move through blocks when you push into them. The server may pull you back.",
                Category.HACK_MOVE,
                "\u25CC",
                () -> Hacks.PHASE.toggle(),
                () -> Hacks.PHASE.enabled
        ));

        allModules.add(new ModuleEntry(
                "No Fall",
                "No fall damage",
                "Tells the server you landed before you hit the ground, so you take no fall damage.",
                Category.HACK_PLAYER,
                "\u2193",
                () -> Hacks.NO_FALL.toggle(),
                () -> Hacks.NO_FALL.enabled
        ));

        allModules.add(new ModuleEntry(
                "No Slow",
                "No slowdown when using items",
                "Keeps your walking speed while eating, blocking or drawing a bow.",
                Category.HACK_PLAYER,
                "\u27A4",
                () -> Hacks.NO_SLOW.toggle(),
                () -> Hacks.NO_SLOW.enabled
        ));

        allModules.add(new ModuleEntry(
                "Timer",
                "Speeds up the game",
                "Runs the game faster than normal. The setting is the speed in tenths (15 = x1.5). The server may rubber-band you.",
                Category.HACK_PLAYER,
                "\u23F1",
                () -> Hacks.TIMER.toggle(),
                () -> Hacks.TIMER.enabled
        ).withSetting("Speed /10", 10, 30, 1, () -> Hacks.TIMER.a, v -> Hacks.TIMER.a = v));

        allModules.add(new ModuleEntry(
                "Blink",
                "Hold your movement",
                "Holds back your movement from the server while on, then sends it all at once when you turn it off. Don't leave it on for long.",
                Category.HACK_PLAYER,
                "\u2059",
                () -> Hacks.BLINK.toggle(),
                () -> Hacks.BLINK.enabled
        ));

        allModules.add(new ModuleEntry(
                "ESP",
                "Boxes around things",
                "Draws boxes you can see through walls. Show: All, Players, Mobs, Items or Chests+Spawners (chests, ender chests, barrels, shulkers, spawners). Range is the main setting. Drawn on the screen from your first-person view.",
                Category.HACK_RENDER,
                "\u25A2",
                () -> RenderHacks.ESP.toggle(),
                () -> RenderHacks.ESP.enabled
        ).withSetting("Range", 16, 128, 16, () -> RenderHacks.ESP.a, v -> RenderHacks.ESP.a = v).withExtra("Show", 0, 4, 1, () -> RenderHacks.ESP.b, v -> RenderHacks.ESP.b = v, RenderHacks.ESP_SHOW));

        allModules.add(new ModuleEntry(
                "Tracers",
                "Lines to targets",
                "Draws lines from the bottom of the screen to players and/or mobs. Range is the main setting; Who chooses players, mobs or both.",
                Category.HACK_RENDER,
                "\u2571",
                () -> RenderHacks.TRACERS.toggle(),
                () -> RenderHacks.TRACERS.enabled
        ).withSetting("Range", 16, 128, 16, () -> RenderHacks.TRACERS.a, v -> RenderHacks.TRACERS.a = v).withExtra("Who", 0, 2, 1, () -> RenderHacks.TRACERS.b, v -> RenderHacks.TRACERS.b = v, RenderHacks.TRACER_SHOW));

        allModules.add(new ModuleEntry(
                "Chams",
                "Outline through walls",
                "Gives players and/or mobs a glowing outline you can see through walls (uses the glow effect, not custom colors).",
                Category.HACK_RENDER,
                "\u25CD",
                () -> RenderHacks.CHAMS.toggle(),
                () -> RenderHacks.CHAMS.enabled
        ).withSetting("Who", 0, 2, 1, () -> RenderHacks.CHAMS.a, v -> RenderHacks.CHAMS.a = v).withSettingNames(RenderHacks.TRACER_SHOW));

        allModules.add(new ModuleEntry(
                "Xray",
                "See ores through blocks",
                "Hides every block that is not in the list, so ores show through the ground. Mode: Ores, Ores+Containers, or Custom file (config/altairclient-xray.txt, one block id per line, created on first use). Turn on Full Bright to see better.",
                Category.HACK_RENDER,
                "\u2318",
                () -> RenderHacks.toggleXray(),
                () -> RenderHacks.XRAY.enabled
        ).withSetting("Mode", 0, 2, 1, () -> RenderHacks.XRAY.a, v -> RenderHacks.XRAY.a = v).withSettingNames(RenderHacks.XRAY_MODES));

        allModules.add(new ModuleEntry(
                "Trajectories",
                "Where it will land",
                "Shows the path of arrows (bow while drawn), crossbow, trident, ender pearls, snowballs and eggs when held. Red dot = where it hits.",
                Category.HACK_RENDER,
                "\u2934",
                () -> RenderHacks.TRAJECTORIES.toggle(),
                () -> RenderHacks.TRAJECTORIES.enabled
        ));

        allModules.add(new ModuleEntry(
                "No Fog",
                "Removes fog",
                "Pushes all fog (air, water, lava) far away.",
                Category.HACK_RENDER,
                "\u2601",
                () -> RenderHacks.NO_FOG.toggle(),
                () -> RenderHacks.NO_FOG.enabled
        ));

        allModules.add(new ModuleEntry(
                "No Rain",
                "Removes rain",
                "Clears rain and thunder on your screen.",
                Category.HACK_RENDER,
                "\u2602",
                () -> RenderHacks.NO_RAIN.toggle(),
                () -> RenderHacks.NO_RAIN.enabled
        ));

        allModules.add(new ModuleEntry(
                "No Particles",
                "Removes particles",
                "Stops particles from being drawn.",
                Category.HACK_RENDER,
                "\u2735",
                () -> RenderHacks.NO_PARTICLES.toggle(),
                () -> RenderHacks.NO_PARTICLES.enabled
        ));

        allModules.add(new ModuleEntry(
                "No Animations",
                "Removes bobbing",
                "Turns off view bobbing and the camera tilt when you are hurt. Your old settings come back when you turn it off.",
                Category.HACK_RENDER,
                "\u21BB",
                () -> RenderHacks.NO_ANIMATIONS.toggle(),
                () -> RenderHacks.NO_ANIMATIONS.enabled
        ));

        allModules.add(new ModuleEntry(
                "Nuker",
                "Breaks blocks around you",
                "Breaks the blocks around you. Range is the main setting; Blocks per tick works only in creative. Be careful: it breaks everything in range except the block you stand on.",
                Category.HACK_WORLD,
                "\u26CF",
                () -> WorldHacks.NUKER.toggle(),
                () -> WorldHacks.NUKER.enabled
        ).withSetting("Range", 2, 6, 1, () -> WorldHacks.NUKER.a, v -> WorldHacks.NUKER.a = v).withExtra("Blocks/tick", 1, 5, 1, () -> WorldHacks.NUKER.b, v -> WorldHacks.NUKER.b = v, null));

        allModules.add(new ModuleEntry(
                "Scaffold",
                "Builds under your feet",
                "Places the block you hold (or one from your hotbar) under your feet while you walk.",
                Category.HACK_WORLD,
                "\u25A4",
                () -> WorldHacks.SCAFFOLD.toggle(),
                () -> WorldHacks.SCAFFOLD.enabled
        ));

        allModules.add(new ModuleEntry(
                "Fast Place",
                "No delay when placing",
                "Removes the delay between placing blocks when you hold the button.",
                Category.HACK_WORLD,
                "\u25A3",
                () -> WorldHacks.FAST_PLACE.toggle(),
                () -> WorldHacks.FAST_PLACE.enabled
        ));

        allModules.add(new ModuleEntry(
                "Fast Break",
                "Breaks faster",
                "Removes the delay after breaking and adds extra progress each tick. The server may reject it.",
                Category.HACK_WORLD,
                "\u26A1",
                () -> WorldHacks.FAST_BREAK.toggle(),
                () -> WorldHacks.FAST_BREAK.enabled
        ).withSetting("Boost", 0, 10, 1, () -> WorldHacks.FAST_BREAK.a, v -> WorldHacks.FAST_BREAK.a = v));

        allModules.add(new ModuleEntry(
                "Auto Mine",
                "Keeps mining",
                "Holds the attack button while you look at a block.",
                Category.HACK_WORLD,
                "\u26CF",
                () -> WorldHacks.AUTO_MINE.toggle(),
                () -> WorldHacks.AUTO_MINE.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Farm",
                "Harvests and replants",
                "Breaks fully grown crops in range and replants seeds on empty farmland (hold or carry the seeds in your hotbar).",
                Category.HACK_WORLD,
                "\u2698",
                () -> WorldHacks.AUTO_FARM.toggle(),
                () -> WorldHacks.AUTO_FARM.enabled
        ).withSetting("Range", 2, 6, 1, () -> WorldHacks.AUTO_FARM.a, v -> WorldHacks.AUTO_FARM.a = v));

        allModules.add(new ModuleEntry(
                "Auto Fish",
                "Fishes for you",
                "Hold a fishing rod: it casts, reels in when it sees a bite and casts again.",
                Category.HACK_WORLD,
                "\u2693",
                () -> WorldHacks.AUTO_FISH.toggle(),
                () -> WorldHacks.AUTO_FISH.enabled
        ));

        allModules.add(new ModuleEntry(
                "Surround",
                "Obsidian around your feet",
                "Places obsidian (from your hotbar) on the four sides of your feet.",
                Category.HACK_WORLD,
                "\u25A6",
                () -> WorldHacks.SURROUND.toggle(),
                () -> WorldHacks.SURROUND.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Trap",
                "Traps the nearest player",
                "Places obsidian around the nearest player within 5 blocks, one block per tick.",
                Category.HACK_WORLD,
                "\u25A9",
                () -> WorldHacks.AUTO_TRAP.toggle(),
                () -> WorldHacks.AUTO_TRAP.enabled
        ));

        allModules.add(new ModuleEntry(
                "Bed Aura",
                "Bed explosions",
                "In the Nether or the End only: places a bed next to the nearest player and right-clicks it to explode. Range is the main setting; Interval is ticks between actions. Dangerous for you too.",
                Category.HACK_COMBAT,
                "\u2616",
                () -> WorldHacks.BED_AURA.toggle(),
                () -> WorldHacks.BED_AURA.enabled
        ).withSetting("Range", 2, 6, 1, () -> WorldHacks.BED_AURA.a, v -> WorldHacks.BED_AURA.a = v).withExtra("Interval", 1, 20, 1, () -> WorldHacks.BED_AURA.b, v -> WorldHacks.BED_AURA.b = v, null));

        allModules.add(new ModuleEntry(
                "Mace Aura",
                "Smash attacks with the mace",
                "Holds your mace and hits the nearest target when you fall onto it (the mace needs a fall of 1.5+ blocks). Auto jump jumps high from the ground to start the fall.",
                Category.HACK_COMBAT,
                "\u2692",
                () -> ExtraHacks.MACE_AURA.toggle(),
                () -> ExtraHacks.MACE_AURA.enabled
        ).withSetting("Range", 2, 6, 1, () -> ExtraHacks.MACE_AURA.a, v -> ExtraHacks.MACE_AURA.a = v).withExtra("Auto jump", 0, 1, 1, () -> ExtraHacks.MACE_AURA.b, v -> ExtraHacks.MACE_AURA.b = v, ExtraHacks.OFF_ON));

        allModules.add(new ModuleEntry(
                "Anchor Aura",
                "Respawn anchor explosions",
                "Outside the Nether: places a respawn anchor next to the nearest target, charges it with glowstone and explodes it. Hotbar needs anchors and glowstone. Dangerous for you too.",
                Category.HACK_COMBAT,
                "\u2693",
                () -> ExtraHacks.ANCHOR_AURA.toggle(),
                () -> ExtraHacks.ANCHOR_AURA.enabled
        ).withSetting("Range", 2, 6, 1, () -> ExtraHacks.ANCHOR_AURA.a, v -> ExtraHacks.ANCHOR_AURA.a = v).withExtra("Interval", 1, 20, 1, () -> ExtraHacks.ANCHOR_AURA.b, v -> ExtraHacks.ANCHOR_AURA.b = v, null));

        allModules.add(new ModuleEntry(
                "Auto Sword",
                "Best weapon in hand",
                "Switches to the best sword or axe in your hotbar when you attack or Kill Aura is on and something is close.",
                Category.HACK_COMBAT,
                "\u2694",
                () -> ExtraHacks.AUTO_SWORD.toggle(),
                () -> ExtraHacks.AUTO_SWORD.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Clicker",
                "Clicks for you",
                "While you hold attack on an entity, hits it at the chosen clicks per second.",
                Category.HACK_COMBAT,
                "\u261D",
                () -> ExtraHacks.AUTO_CLICKER.toggle(),
                () -> ExtraHacks.AUTO_CLICKER.enabled
        ).withSetting("CPS", 1, 20, 1, () -> ExtraHacks.AUTO_CLICKER.a, v -> ExtraHacks.AUTO_CLICKER.a = v));

        allModules.add(new ModuleEntry(
                "Trigger Bot",
                "Hits what you aim at",
                "Attacks the living thing under your crosshair as soon as your weapon has recharged.",
                Category.HACK_COMBAT,
                "\u2316",
                () -> ExtraHacks.TRIGGER_BOT.toggle(),
                () -> ExtraHacks.TRIGGER_BOT.enabled
        ));

        allModules.add(new ModuleEntry(
                "Fast Bow",
                "Quick bow shots",
                "Releases the bow after the chosen number of draw ticks (20 = full power; lower is faster but weaker). Keep the use button held to spam.",
                Category.HACK_COMBAT,
                "\u27B6",
                () -> ExtraHacks.FAST_BOW.toggle(),
                () -> ExtraHacks.FAST_BOW.enabled
        ).withSetting("Ticks", 3, 20, 1, () -> ExtraHacks.FAST_BOW.a, v -> ExtraHacks.FAST_BOW.a = v));

        allModules.add(new ModuleEntry(
                "Bow Aimbot",
                "Aims the bow for you",
                "While drawing a bow, aims at the closest target with lead and arc compensation. Strength is how fast it turns.",
                Category.HACK_COMBAT,
                "\u2299",
                () -> ExtraHacks.BOW_AIMBOT.toggle(),
                () -> ExtraHacks.BOW_AIMBOT.enabled
        ).withSetting("Strength %", 10, 100, 10, () -> ExtraHacks.BOW_AIMBOT.a, v -> ExtraHacks.BOW_AIMBOT.a = v));

        allModules.add(new ModuleEntry(
                "Shield Breaker",
                "Axe against shields",
                "Switches to an axe when the nearest target is blocking with a shield.",
                Category.HACK_COMBAT,
                "\u26CF",
                () -> ExtraHacks.SHIELD_BREAKER.toggle(),
                () -> ExtraHacks.SHIELD_BREAKER.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Shield",
                "Raises your shield",
                "Holds your shield up while an enemy is within 4 blocks.",
                Category.HACK_COMBAT,
                "\u26E8",
                () -> ExtraHacks.AUTO_SHIELD.toggle(),
                () -> ExtraHacks.AUTO_SHIELD.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Consume",
                "Eats when hungry",
                "Eats food from your hotbar when your food level falls to the setting (20 = full), then goes back to your slot. Skips golden apples and rotten flesh.",
                Category.HACK_COMBAT,
                "\u2615",
                () -> ExtraHacks.AUTO_CONSUME.toggle(),
                () -> ExtraHacks.AUTO_CONSUME.enabled
        ).withSetting("Food level", 2, 19, 1, () -> ExtraHacks.AUTO_CONSUME.a, v -> ExtraHacks.AUTO_CONSUME.a = v));

        allModules.add(new ModuleEntry(
                "Multi Aura",
                "Hits everything around",
                "Hits up to 6 living things in range at once, every Interval ticks.",
                Category.HACK_COMBAT,
                "\u2736",
                () -> ExtraHacks.MULTI_AURA.toggle(),
                () -> ExtraHacks.MULTI_AURA.enabled
        ).withSetting("Range", 2, 6, 1, () -> ExtraHacks.MULTI_AURA.a, v -> ExtraHacks.MULTI_AURA.a = v).withExtra("Interval", 1, 20, 1, () -> ExtraHacks.MULTI_AURA.b, v -> ExtraHacks.MULTI_AURA.b = v, null));

        allModules.add(new ModuleEntry(
                "Auto Web",
                "Cobwebs on targets",
                "Places a cobweb on the nearest target (cobwebs in your hotbar).",
                Category.HACK_COMBAT,
                "\u2637",
                () -> ExtraHacks.AUTO_WEB.toggle(),
                () -> ExtraHacks.AUTO_WEB.enabled
        ).withSetting("Range", 2, 6, 1, () -> ExtraHacks.AUTO_WEB.a, v -> ExtraHacks.AUTO_WEB.a = v));

        allModules.add(new ModuleEntry(
                "Long Jump",
                "Jump farther",
                "While you jump, pushes you forward. Strength 1 to 5.",
                Category.HACK_MOVE,
                "\u21E5",
                () -> ExtraHacks.LONG_JUMP.toggle(),
                () -> ExtraHacks.LONG_JUMP.enabled
        ).withSetting("Strength", 1, 5, 1, () -> ExtraHacks.LONG_JUMP.a, v -> ExtraHacks.LONG_JUMP.a = v));

        allModules.add(new ModuleEntry(
                "High Jump",
                "Jump higher",
                "Makes your jumps higher. Strength 1 to 5.",
                Category.HACK_MOVE,
                "\u21EA",
                () -> ExtraHacks.HIGH_JUMP.toggle(),
                () -> ExtraHacks.HIGH_JUMP.enabled
        ).withSetting("Strength", 1, 5, 1, () -> ExtraHacks.HIGH_JUMP.a, v -> ExtraHacks.HIGH_JUMP.a = v));

        allModules.add(new ModuleEntry(
                "Bunny Hop",
                "Auto hop and speed",
                "Keeps jumping while you walk, with extra speed.",
                Category.HACK_MOVE,
                "\u2608",
                () -> ExtraHacks.BUNNY_HOP.toggle(),
                () -> ExtraHacks.BUNNY_HOP.enabled
        ));

        allModules.add(new ModuleEntry(
                "Air Jump",
                "Jump in the air",
                "Press jump again in the air to jump again.",
                Category.HACK_MOVE,
                "\u2191",
                () -> ExtraHacks.AIR_JUMP.toggle(),
                () -> ExtraHacks.AIR_JUMP.enabled
        ));

        allModules.add(new ModuleEntry(
                "Safe Walk",
                "Never fall off edges",
                "Sneaks for you at the edge of a block so you do not fall.",
                Category.HACK_MOVE,
                "\u26A0",
                () -> ExtraHacks.SAFE_WALK.toggle(),
                () -> ExtraHacks.SAFE_WALK.enabled
        ));

        allModules.add(new ModuleEntry(
                "Parkour",
                "Auto jump at edges",
                "Jumps by itself when you walk off the edge of a block.",
                Category.HACK_MOVE,
                "\u21B1",
                () -> ExtraHacks.PARKOUR.toggle(),
                () -> ExtraHacks.PARKOUR.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Jump",
                "Always jumping",
                "Jumps by itself whenever you walk on the ground.",
                Category.HACK_MOVE,
                "\u2B06",
                () -> ExtraHacks.AUTO_JUMP.toggle(),
                () -> ExtraHacks.AUTO_JUMP.enabled
        ));

        allModules.add(new ModuleEntry(
                "Fast Ladder",
                "Climb ladders faster",
                "Climbs ladders and vines faster.",
                Category.HACK_MOVE,
                "\u2630",
                () -> ExtraHacks.FAST_LADDER.toggle(),
                () -> ExtraHacks.FAST_LADDER.enabled
        ).withSetting("Speed", 1, 8, 1, () -> ExtraHacks.FAST_LADDER.a, v -> ExtraHacks.FAST_LADDER.a = v));

        allModules.add(new ModuleEntry(
                "Fast Swim",
                "Swim faster",
                "Swims faster in water.",
                Category.HACK_MOVE,
                "\u2248",
                () -> ExtraHacks.FAST_SWIM.toggle(),
                () -> ExtraHacks.FAST_SWIM.enabled
        ).withSetting("Speed", 1, 8, 1, () -> ExtraHacks.FAST_SWIM.a, v -> ExtraHacks.FAST_SWIM.a = v));

        allModules.add(new ModuleEntry(
                "Auto Walk",
                "Walks forward",
                "Holds the forward key for you.",
                Category.HACK_MOVE,
                "\u21E7",
                () -> ExtraHacks.AUTO_WALK.toggle(),
                () -> ExtraHacks.AUTO_WALK.enabled
        ));

        allModules.add(new ModuleEntry(
                "Sneak",
                "Always sneaking",
                "Holds the sneak key for you.",
                Category.HACK_MOVE,
                "\u2193",
                () -> ExtraHacks.SNEAK.toggle(),
                () -> ExtraHacks.SNEAK.enabled
        ));

        allModules.add(new ModuleEntry(
                "Reverse Step",
                "Fall down fast",
                "Drops you quickly when the ground is within 3 blocks below.",
                Category.HACK_MOVE,
                "\u21A7",
                () -> ExtraHacks.REVERSE_STEP.toggle(),
                () -> ExtraHacks.REVERSE_STEP.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Respawn",
                "Respawns right away",
                "Respawns you as soon as you die.",
                Category.HACK_PLAYER,
                "\u21BA",
                () -> ExtraHacks.AUTO_RESPAWN.toggle(),
                () -> ExtraHacks.AUTO_RESPAWN.enabled
        ));

        allModules.add(new ModuleEntry(
                "Anti AFK",
                "Avoids being kicked for AFK",
                "Jumps and turns a little every 20 seconds.",
                Category.HACK_PLAYER,
                "\u23F3",
                () -> ExtraHacks.ANTI_AFK.toggle(),
                () -> ExtraHacks.ANTI_AFK.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Tool",
                "Best tool for the block",
                "Switches to the fastest tool in your hotbar for the block you are mining.",
                Category.HACK_PLAYER,
                "\u26CF",
                () -> ExtraHacks.AUTO_TOOL.toggle(),
                () -> ExtraHacks.AUTO_TOOL.enabled
        ));

        allModules.add(new ModuleEntry(
                "Chest Stealer",
                "Takes everything from chests",
                "Quickly moves every item out of a chest you open into your inventory.",
                Category.HACK_PLAYER,
                "\u2B07",
                () -> ExtraHacks.CHEST_STEALER.toggle(),
                () -> ExtraHacks.CHEST_STEALER.enabled
        ));

        allModules.add(new ModuleEntry(
                "Spin",
                "Spins your view",
                "Rotates you around. Speed 1 to 10.",
                Category.HACK_PLAYER,
                "\u21BB",
                () -> ExtraHacks.SPIN.toggle(),
                () -> ExtraHacks.SPIN.enabled
        ).withSetting("Speed", 1, 10, 1, () -> ExtraHacks.SPIN.a, v -> ExtraHacks.SPIN.a = v));

        allModules.add(new ModuleEntry(
                "Chest Aura",
                "Opens nearby chests",
                "Opens chests, barrels and shulker boxes within range, one every half second. Works well with Chest Stealer.",
                Category.HACK_WORLD,
                "\u2610",
                () -> ExtraHacks.CHEST_AURA.toggle(),
                () -> ExtraHacks.CHEST_AURA.enabled
        ).withSetting("Range", 2, 6, 1, () -> ExtraHacks.CHEST_AURA.a, v -> ExtraHacks.CHEST_AURA.a = v));

        allModules.add(new ModuleEntry(
                "Auto Tower",
                "Builds up under you",
                "While you hold jump in the air with blocks in your hotbar, places a block under you and keeps going up.",
                Category.HACK_WORLD,
                "\u25B2",
                () -> ExtraHacks.AUTO_TOWER.toggle(),
                () -> ExtraHacks.AUTO_TOWER.enabled
        ));

        allModules.add(new ModuleEntry(
                "Auto Shear",
                "Shears sheep",
                "Shears sheep that are ready, within range (shears in your hotbar).",
                Category.HACK_WORLD,
                "\u2702",
                () -> ExtraHacks.AUTO_SHEAR.toggle(),
                () -> ExtraHacks.AUTO_SHEAR.enabled
        ).withSetting("Range", 2, 6, 1, () -> ExtraHacks.AUTO_SHEAR.a, v -> ExtraHacks.AUTO_SHEAR.a = v));

        allModules.add(new ModuleEntry(
                "Clock",
                "Your computer's time",
                "Shows the time of your computer (hours, minutes and seconds). Movable in the HUD Editor.",
                Category.WORLD,
                "\u23F0",
                () -> ClockHud.getInstance().toggle(),
                () -> ClockHud.getInstance().isEnabled()
        ));
        allModules.add(new ModuleEntry(
                "Server Address",
                "Server IP",
                "Shows the IP of the server you are playing on. Movable in the HUD Editor.",
                Category.WORLD,
                "\u2301",
                () -> ServerAddress.getInstance().toggle(),
                () -> ServerAddress.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Time Changer",
                "Visual time of day",
                "Sets the time of day on your client only (always day, always night...). It does not affect the server or other players. Choose the time with - and +.",
                Category.RENDER,
                "\u263C",
                () -> TimeChanger.toggle(),
                () -> TimeChanger.isEnabled()
        ).withSetting("Time", 0, TimeChanger.MODE_NAMES.length - 1, 1,
                () -> TimeChanger.getMode(), v -> TimeChanger.setMode(v))
                .withSettingNames(TimeChanger.MODE_NAMES));

        allModules.add(new ModuleEntry(
                "Weather Changer",
                "Visual weather",
                "Changes the weather on your client only: removes rain and snow or forces rain and thunderstorms. It does not affect the server. Choose the weather with - and +.",
                Category.RENDER,
                "\u2602",
                () -> WeatherChanger.toggle(),
                () -> WeatherChanger.isEnabled()
        ).withSetting("Weather", 0, WeatherChanger.MODE_NAMES.length - 1, 1,
                () -> WeatherChanger.getMode(), v -> WeatherChanger.setMode(v))
                .withSettingNames(WeatherChanger.MODE_NAMES));

        allModules.add(new ModuleEntry(
                "Block Outline",
                "Block outline",
                "Changes the color and thickness of the outline of the block you look at. Choose a palette color, the Altair Cycle (cycles through the palette colors) or Rainbow.",
                Category.RENDER,
                "\u25A2",
                () -> BlockOutline.toggle(),
                () -> BlockOutline.isEnabled()
        ).withSetting("Color", 0, com.altairclient.client.Palette.MAX_INDEX, 1,
                () -> BlockOutline.getColorIndex(), v -> BlockOutline.setColorIndex(v))
                .withSettingNames(com.altairclient.client.Palette.NAMES)
                .withExtra("Thickness", BlockOutline.MIN_WIDTH, BlockOutline.MAX_WIDTH, 1,
                        () -> BlockOutline.getWidth(), v -> BlockOutline.setWidth(v), null));

        allModules.add(new ModuleEntry(
                "Custom Crosshair",
                "PvP crosshair",
                "Replaces the crosshair with a PvP design: dot, cross, gapped cross, circle, T, X or corners. Choose style, color (Altair palette, cycle or rainbow) and size.",
                Category.COMBAT,
                "\u2316",
                () -> CustomCrosshair.toggle(),
                () -> CustomCrosshair.isEnabled()
        ).withSetting("Style", 0, CustomCrosshair.STYLE_NAMES.length - 1, 1,
                () -> CustomCrosshair.getStyle(), v -> CustomCrosshair.setStyle(v))
                .withSettingNames(CustomCrosshair.STYLE_NAMES)
                .withExtra("Color", 0, com.altairclient.client.Palette.MAX_INDEX, 1,
                        () -> CustomCrosshair.getColorIndex(), v -> CustomCrosshair.setColorIndex(v),
                        com.altairclient.client.Palette.NAMES)
                .withExtra("Size", CustomCrosshair.MIN_SIZE, CustomCrosshair.MAX_SIZE, 1,
                        () -> CustomCrosshair.getSize(), v -> CustomCrosshair.setSize(v), null));

        allModules.add(new ModuleEntry(
                "Auto GG",
                "Automatic GG",
                "Types gg in the chat when the server announces the end of a game. It only reacts to server messages and waits 15 s between sends. Some servers don't allow automatic messages: enable it only if yours does.",
                Category.MISC,
                "\u2713",
                () -> AutoGG.toggle(),
                () -> AutoGG.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Auto Respawn",
                "Respawn on its own",
                "When you die, clicks Respawn for you automatically.",
                Category.PLAYER,
                "\u21BB",
                () -> AutoRespawn.toggle(),
                () -> AutoRespawn.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "No Hurt Camera",
                "No camera shake",
                "Removes the camera movement when you take damage.",
                Category.RENDER,
                "\u2715",
                () -> NoHurtCamera.toggle(),
                () -> NoHurtCamera.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Chat Timestamps",
                "Time in chat",
                "Adds the time [HH:mm] before every chat message.",
                Category.MISC,
                "\u231A",
                () -> ChatTimestamps.toggle(),
                () -> ChatTimestamps.isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Item Name",
                "Item name",
                "Shows the name of the item in your hand, in its rarity color. Movable in the HUD Editor.",
                Category.PLAYER,
                "\u270E",
                () -> ItemNameDisplay.getInstance().toggle(),
                () -> ItemNameDisplay.getInstance().isEnabled()
        ));

        allModules.add(new ModuleEntry(
                "Auto Tool",
                "Right tool",
                "When you mine a block, picks the fastest tool for it from your hotbar and goes back to your previous slot when you finish.",
                Category.PLAYER,
                "\u26CF",
                () -> AutoTool.toggle(),
                () -> AutoTool.isEnabled()
        ));

        // ==================== WORLD ====================
        allModules.add(new ModuleEntry(
                "World Info",
                "Surroundings info",
                "Panel with XYZ coordinates, cardinal direction with degrees, server ping and a clock with your system's local time.",
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
        restoreLastPage();
    }

    // La página donde estabas (carpeta, apartado y módulo) se recuerda al volver a abrir el menú.
    private static Category lastCategory = null;
    private static Section lastSection = null;
    private static String lastModuleName = null;

    private void restoreLastPage() {
        if (lastCategory == null) {
            return;
        }
        currentCategory = lastCategory;
        openSection = lastSection != null ? lastSection : lastCategory.section;
        List<ModuleEntry> mods = getModulesForCategory(currentCategory);
        for (ModuleEntry m : mods) {
            if (m.name.equals(lastModuleName)) {
                selectedModule = m;
                return;
            }
        }
        if (!mods.isEmpty()) {
            selectedModule = mods.get(0);
        }
    }

    private void rememberPage() {
        lastCategory = currentCategory;
        lastSection = openSection;
        lastModuleName = selectedModule != null ? selectedModule.name : null;
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
            "Notes", "Freecam", "Smooth Zoom", "Perspective Mod", "Fullbright", "Screenshot", "Screenshot Upload");

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
        com.altairclient.modules.mods.RenderHacks.applyPendingReload();
        rememberPage();
        restoreBlur();
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
        }

        String full = tex.isImplemented() || isLinkedToMod(tex)
                ? tex.description
                : tex.description + " It has no effect in the game yet: for now it only exists in the menu.";
        ModuleEntry entry = new ModuleEntry(tex.name, tex.description, full, cat, cat.icon, toggle, enabled);
        if (TextureModules.ID_ALTAIR_SKY.equals(tex.id)) {
            entry.withSetting("Stars", 0, AltairSky.DENSITY_NAMES.length - 1, 1,
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
        raiseBlur();

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
                Component.literal("Search modules")
        );
        searchBox.setBordered(false);
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.literal("Search modules..."));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(this::onSearchChanged);
        addWidget(searchBox);
    }

    // ==================== Fondo desenfocado ====================

    /** Mientras el menu esta abierto, ExtraTextures no debe quitar el desenfoque. */
    public static volatile boolean blurActive = false;
    private static Integer savedBlur = null;

    /** Sube el desenfoque del fondo del juego mientras el menu esta abierto (el menu en si se ve nitido). */
    private void raiseBlur() {
        if (minecraft == null || blurActive) {
            return;
        }
        net.minecraft.client.OptionInstance<Integer> blur = minecraft.options.menuBackgroundBlurriness();
        savedBlur = blur.get();
        blurTarget = Math.max(savedBlur, 8);
        blurActive = true;
        blur.set(0); // empieza sin desenfoque y sube con la animacion de apertura
    }

    private static int blurTarget = 8;

    /** Pone el desenfoque segun el avance de la animacion (0 = nada, 1 = completo). */
    private void applyBlur(float amount) {
        if (minecraft == null || !blurActive) {
            return;
        }
        int level = Math.round(blurTarget * Math.max(0.0F, Math.min(1.0F, amount)));
        net.minecraft.client.OptionInstance<Integer> blur = minecraft.options.menuBackgroundBlurriness();
        if (blur.get() != level) {
            blur.set(level);
        }
    }

    // Cierre animado: el menu se funde y el fondo se enfoca de nuevo antes de cerrarse del todo
    private boolean closing = false;
    private long closeStart;
    private static final float CLOSE_NANOS = 220_000_000.0F;

    @Override
    public void onClose() {
        if (!closing) {
            closing = true;
            closeStart = System.nanoTime();
        }
    }

    private void restoreBlur() {
        if (!blurActive) {
            return;
        }
        blurActive = false;
        if (minecraft != null && savedBlur != null) {
            minecraft.options.menuBackgroundBlurriness().set(savedBlur);
        }
        savedBlur = null;
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
        if (closing && (System.nanoTime() - closeStart) >= CLOSE_NANOS) {
            if (minecraft != null) {
                minecraft.gui.setScreen(null);
            }
            return;
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // 1. Fondo oscuro global
        // Animación de apertura: el fondo se oscurece y el panel crece desde el 94 %.
        float open;
        if (closing) {
            open = 1.0F - Anim.easeOut((System.nanoTime() - closeStart) / CLOSE_NANOS);
        } else {
            open = Anim.easeOut((System.nanoTime() - openStart) / 280_000_000.0F);
        }
        applyBlur(open);
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
        int sideBottom = panelY + panelHeight - 74;
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

        // Botón Configs, justo encima del HUD Editor
        {
            int cbY = panelY + panelHeight - 68;
            int cbX = panelX + 8;
            int cbW = sidebarWidth - 16;
            boolean cbHot = mouseX >= cbX && mouseX <= cbX + cbW && mouseY >= cbY && mouseY <= cbY + 24;
            int cbBorder = cbHot ? CYAN : PURPLE;
            graphics.fill(cbX, cbY, cbX + cbW, cbY + 24, cbHot ? 0xFF352B66 : 0xFF1C2242);
            graphics.fill(cbX, cbY, cbX + cbW, cbY + 1, cbBorder);
            graphics.fill(cbX, cbY + 23, cbX + cbW, cbY + 24, cbBorder);
            graphics.fill(cbX, cbY + 1, cbX + 1, cbY + 24, cbBorder);
            graphics.fill(cbX + cbW - 1, cbY, cbX + cbW, cbY + 24, cbBorder);
            String cbText = "\u2699 Configs";
            graphics.text(font, cbText, cbX + (cbW - font.width(cbText)) / 2, cbY + 8, WHITE, false);
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
            graphics.text(font, "Status:", rightPanelX + 14, panelY + panelHeight - 62, MUTED, false);
            graphics.text(font, isModEnabled ? "ENABLED" : "DISABLED", rightPanelX + 60, panelY + panelHeight - 62, isModEnabled ? GREEN : MUTED, false);

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

            String statusLabel = isModEnabled ? "Enabled" : "Disabled";
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
        String gridTitle = searching ? "Results" : currentCategory.displayName;
        graphics.text(font, gridTitle, gridStartX, gridStartY, WHITE, false);
        graphics.text(font, searching ? "\"" + searchQuery + "\"" : "Select a card to see its options",
                gridStartX + font.width(gridTitle) + 10, gridStartY + 1, MUTED, false);

        List<ModuleEntry> categoryModules = getVisibleModules();

        if (searching && categoryModules.isEmpty()) {
            graphics.text(font, "No results", gridStartX, gridStartY + 22, MUTED, false);
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
            // Borde + fondo en 2 rectángulos (antes 5)
            int borderCol = Anim.mix(Anim.mix(BORDER_COLOR, CYAN, hovA), PURPLE, selA);
            graphics.fill(cx, cy, cx + cardWidth, cy + cardHeight, borderCol);
            graphics.fill(cx + 1, cy + 1, cx + cardWidth - 1, cy + cardHeight - 1, cardBgColor);

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
        ClockHud.getInstance().render(graphics);
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
        if (closing) {
            return true;
        }
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
            // El engranaje ya no hace nada (se queda como decoración)
            return true;
        }

        // 1. Clic en botón cerrar [✕]
        int winBtnY = panelY + 10;
        int winCloseX = panelX + panelWidth - 24;
        if (mx >= winCloseX && mx <= winCloseX + 14 && my >= winBtnY && my <= winBtnY + 14) {
            if (minecraft != null) {
                onClose();
            }
            return true;
        }

        // 1b. Clic en el botón Configs
        {
            int cbY = panelY + panelHeight - 68;
            int cbX = panelX + 8;
            if (mx >= cbX && mx <= cbX + 94 && my >= cbY && my <= cbY + 24) {
                if (minecraft != null) {
                    minecraft.gui.setScreen(new ConfigsScreen(this));
                }
                return true;
            }
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
        int sideBottom = panelY + panelHeight - 74;
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
