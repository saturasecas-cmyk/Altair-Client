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
    private static boolean autoSave = true;
    private static boolean titleBackground = true;
    private static String lastWritten = "";
    private static int ticks = 0;

    private AltairConfig() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        TickEvent.ClientTickEvent.Post.BUS.addListener(AltairConfig::onTick);
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

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!loaded) {
            if (mc.isGameLoadFinished() && mc.options != null) {
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
            root.addProperty("titleBackground", titleBackground);
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
