package com.altairclient.client;

import com.altairclient.AltairClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Named configurations (profiles) the player can save and apply, stored in config/altairclient-configs.json. */
public final class ConfigProfiles {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "altairclient-configs.json";
    private static final Map<String, JsonObject> PROFILES = new LinkedHashMap<>();
    private static boolean loaded = false;

    private ConfigProfiles() {
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path p = file();
        if (!Files.exists(p)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(p, StandardCharsets.UTF_8)).getAsJsonObject();
            for (Map.Entry<String, com.google.gson.JsonElement> e : root.entrySet()) {
                if (e.getValue().isJsonObject()) {
                    PROFILES.put(e.getKey(), e.getValue().getAsJsonObject());
                }
            }
        } catch (IOException | RuntimeException e) {
            AltairClient.LOGGER.error("[Altair] Could not read the saved configs", e);
        }
    }

    private static void write() {
        try {
            JsonObject root = new JsonObject();
            PROFILES.forEach(root::add);
            Path p = file();
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(tmp, GSON.toJson(root), StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException e) {
            AltairClient.LOGGER.error("[Altair] Could not save the configs", e);
        }
    }

    public static synchronized List<String> names() {
        ensureLoaded();
        return new ArrayList<>(PROFILES.keySet());
    }

    /** Next free default name: "Config 1", "Config 2"... */
    public static synchronized String suggestName() {
        ensureLoaded();
        int i = 1;
        while (PROFILES.containsKey("Config " + i)) {
            i++;
        }
        return "Config " + i;
    }

    /** Saves the current modules and settings under this name (replaces a config with the same name). */
    public static synchronized void save(String name) {
        ensureLoaded();
        PROFILES.put(name, AltairScreen.exportState());
        write();
    }

    /** Applies a saved config. Returns how many modules changed, or -1 if it does not exist. */
    public static synchronized int apply(String name) {
        ensureLoaded();
        JsonObject state = PROFILES.get(name);
        if (state == null) {
            return -1;
        }
        AltairScreen.disableAll();
        int n = AltairScreen.importState(state);
        AltairConfig.autoSaveNow();
        return n;
    }

    public static synchronized void delete(String name) {
        ensureLoaded();
        if (PROFILES.remove(name) != null) {
            write();
        }
    }
}
