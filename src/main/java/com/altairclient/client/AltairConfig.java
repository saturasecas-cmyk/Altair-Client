package com.altairclient.client;

import com.altairclient.AltairClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Saves and loads Altair's state (enabled modules, their settings, texture toggles and HUD positions)
 * in {@code <game folder>/config/altairclient-modules.json}.
 * It is loaded once when the game finishes starting and auto-saved while playing.
 */
public final class AltairConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "altairclient-modules.json";
    private static final int AUTOSAVE_TICKS = 200;

    private static boolean registered = false;
    private static boolean loaded = false;

    public static boolean isLoaded() {
        return loaded;
    }
    private static boolean autoSave = true;
    private static boolean titleBackground = true;
    private static boolean tabFleur = true;
    private static boolean roundButtons = true;
    private static boolean linkChat = true;
    private static String lastWritten = "";
    private static int ticks = 0;

    private AltairConfig() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AltairConfig::onTick);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                autoSaveNow();
            } catch (RuntimeException ignored) {
                // nothing else to do while the game is closing
            }
        }, "altair-config-save"));
    }

    public static boolean isAutoSave() {
        return autoSave;
    }

    public static boolean isTitleBackground() {
        return titleBackground;
    }

    public static boolean isLinkChat() {
        return linkChat;
    }

    public static void toggleLinkChat() {
        linkChat = !linkChat;
        autoSaveNow();
    }

    public static boolean isRoundButtons() {
        return roundButtons;
    }

    public static void toggleRoundButtons() {
        roundButtons = !roundButtons;
        autoSaveNow();
    }

    public static boolean isTabFleur() {
        return tabFleur;
    }

    public static void toggleTabFleur() {
        tabFleur = !tabFleur;
        autoSaveNow();
    }

    public static void toggleTitleBackground() {
        titleBackground = !titleBackground;
        autoSaveNow();
    }

    public static void toggleAutoSave() {
        autoSave = !autoSave;
        if (autoSave) {
            autoSaveNow();
        }
    }

    /**
     * The state lives in one place shared by every Minecraft version: the launcher passes -Daltair.configDir.
     * Without the launcher it falls back to {@code <game folder>/config}.
     */
    private static Path file() {
        String dir = System.getProperty("altair.configDir");
        if (dir != null && !dir.isBlank()) {
            return Path.of(dir).resolve(FILE_NAME);
        }
        return legacyFile();
    }

    private static Path legacyFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static boolean wasInWorld = false;

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean inWorld = mc.level != null;
        if (loaded && wasInWorld && !inWorld) {
            autoSaveNow(); // the world was just closed
        }
        wasInWorld = inWorld;
        if (!loaded) {
            if ((mc.getOverlay() == null) && mc.options != null) {
                loaded = true;
                load();
            }
            return;
        }
        if (autoSave && ++ticks >= AUTOSAVE_TICKS) {
            ticks = 0;
            write(false);
        }
    }

    /** Saves only if auto-save is on and the saved state has already been loaded. */
    public static void autoSaveNow() {
        if (loaded && autoSave) {
            write(false);
        }
    }

    // ==================== Buttons of the Settings page ====================

    public static String saveNow() {
        return write(true) ? "Saved to config/" + FILE_NAME : "Could not save (see the game log)";
    }

    public static String loadNow() {
        if (!Files.exists(file())) {
            return "Nothing saved yet";
        }
        int changed = load();
        return changed < 0 ? "Could not read the file" : "Loaded (" + changed + " changes)";
    }

    // ==================== File ====================

    private static boolean write(boolean force) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);
            root.addProperty("autoSave", autoSave);
            root.addProperty("settingsVersion", 2);
            root.addProperty("titleBackground", titleBackground);
            root.addProperty("tabFleur", tabFleur);
            root.addProperty("roundButtons", roundButtons);
            root.addProperty("linkChat", linkChat);
            root.add("modules", com.altairclient.client.AltairScreen.exportState());
            root.add("hud", AltairHudEditorScreen.exportPositions());
            String text = GSON.toJson(root);
            if (!force && text.equals(lastWritten)) {
                return true;
            }
            Path path = file();
            Files.createDirectories(path.getParent());
            Path tmp = path.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, text, StandardCharsets.UTF_8);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            lastWritten = text;
            return true;
        } catch (IOException | RuntimeException e) {
            AltairClient.LOGGER.error("[Altair] Could not save the config", e);
            return false;
        }
    }

    /** @return number of modules that changed, or -1 if the file could not be read. */
    private static int load() {
        Path path = file();
        if (!Files.exists(path) && Files.exists(legacyFile())) {
            path = legacyFile(); // first run with the shared config: import the per-version one
        }
        if (!Files.exists(path)) {
            return 0;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonElement auto = root.get("autoSave");
            if (auto != null && auto.isJsonPrimitive()) {
                autoSave = auto.getAsBoolean();
            }
            JsonElement title = root.get("titleBackground");
            if (title != null && title.isJsonPrimitive()) {
                titleBackground = title.getAsBoolean();
            }
            JsonElement ver = root.get("settingsVersion");
            if (ver == null || !ver.isJsonPrimitive() || ver.getAsInt() < 2) {
                // Versiones anteriores: el fondo y los botones de Altair pasan a ir activados por defecto
                titleBackground = true;
                roundButtons = true;
            }
            JsonElement lc = root.get("linkChat");
            if (lc != null && lc.isJsonPrimitive()) {
                linkChat = lc.getAsBoolean();
            }
            JsonElement rb = root.get("roundButtons");
            if (rb != null && rb.isJsonPrimitive()) {
                roundButtons = rb.getAsBoolean();
            }
            if (ver == null || !ver.isJsonPrimitive() || ver.getAsInt() < 2) {
                titleBackground = true;
                roundButtons = true;
            }
            JsonElement fleur = root.get("tabFleur");
            if (fleur != null && fleur.isJsonPrimitive()) {
                tabFleur = fleur.getAsBoolean();
            }
            int changed = 0;
            JsonElement modules = root.get("modules");
            if (modules != null && modules.isJsonObject()) {
                changed = com.altairclient.client.AltairScreen.importState(modules.getAsJsonObject());
            }
            JsonElement hud = root.get("hud");
            if (hud != null && hud.isJsonObject()) {
                AltairHudEditorScreen.importPositions(hud.getAsJsonObject());
            }
            lastWritten = GSON.toJson(root);
            return changed;
        } catch (IOException | RuntimeException e) {
            AltairClient.LOGGER.error("[Altair] Could not read the config; keeping a backup copy", e);
            try {
                Files.move(path, path.resolveSibling(FILE_NAME + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // the corrupt file stays where it is
            }
            return -1;
        }
    }
}
