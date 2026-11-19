package com.altairclient.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

/** Documento de la app Notas: lineas con estilo por caracter, listas y posicion de la ventana. Se guarda en JSON. */
public final class NotesData {

    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int UNDERLINE = 4;
    public static final int STRIKE = 8;
    public static final int COLOR_SHIFT = 8;

    public static final int LIST_NONE = 0;
    public static final int LIST_BULLET = 1;
    public static final int LIST_NUMBER = 2;
    public static final int LIST_CHECK = 3;

    /** Indice 0 = color por defecto. */
    public static final int[] COLORS = {
        0xFFE6E9FF, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55, 0xFF55FF55, 0xFF55FFFF, 0xFF5599FF, 0xFFC084FC, 0xFFFF79C6
    };

    public static final class Line {
        public final StringBuilder text = new StringBuilder();
        public final List<Integer> styles = new ArrayList<>();
        public int list = LIST_NONE;
        public boolean checked = false;

        public int length() {
            return text.length();
        }

        public void insert(int at, String s, int style) {
            text.insert(at, s);
            for (int i = 0; i < s.length(); i++) {
                styles.add(at + i, style);
            }
        }

        public void delete(int from, int to) {
            text.delete(from, to);
            for (int i = to - 1; i >= from; i--) {
                styles.remove(i);
            }
        }
    }

    public final List<Line> lines = new ArrayList<>();
    public int winX = -1;
    public int winY = -1;
    public int winW = 340;
    public int winH = 270;

    private static NotesData instance;

    public static synchronized NotesData get() {
        if (instance == null) {
            instance = new NotesData();
            instance.load();
        }
        return instance;
    }

    private NotesData() {
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("altairclient-notes.json");
    }

    public void ensureLine() {
        if (lines.isEmpty()) {
            lines.add(new Line());
        }
    }

    public synchronized void save() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("x", winX);
            root.addProperty("y", winY);
            root.addProperty("w", winW);
            root.addProperty("h", winH);
            JsonArray arr = new JsonArray();
            for (Line line : lines) {
                JsonObject o = new JsonObject();
                o.addProperty("l", line.list);
                o.addProperty("c", line.checked);
                JsonArray runs = new JsonArray();
                int i = 0;
                while (i < line.length()) {
                    int style = line.styles.get(i);
                    int j = i;
                    while (j < line.length() && line.styles.get(j) == style) {
                        j++;
                    }
                    JsonObject r = new JsonObject();
                    r.addProperty("t", line.text.substring(i, j));
                    r.addProperty("s", style);
                    runs.add(r);
                    i = j;
                }
                o.add("runs", runs);
                arr.add(o);
            }
            root.add("lines", arr);
            Path path = file();
            Files.createDirectories(path.getParent());
            Path tmp = path.resolveSibling("altairclient-notes.json.tmp");
            Files.writeString(tmp, new Gson().toJson(root), StandardCharsets.UTF_8);
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ignored) {
            // las notas son best-effort: si falla el guardado no se rompe el juego
        }
    }

    private void load() {
        try {
            Path path = file();
            if (Files.exists(path)) {
                JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
                winX = intOr(root, "x", -1);
                winY = intOr(root, "y", -1);
                winW = intOr(root, "w", 340);
                winH = intOr(root, "h", 270);
                JsonElement arr = root.get("lines");
                if (arr != null && arr.isJsonArray()) {
                    for (JsonElement e : arr.getAsJsonArray()) {
                        JsonObject o = e.getAsJsonObject();
                        Line line = new Line();
                        line.list = intOr(o, "l", 0);
                        JsonElement c = o.get("c");
                        line.checked = c != null && c.isJsonPrimitive() && c.getAsBoolean();
                        JsonElement runs = o.get("runs");
                        if (runs != null && runs.isJsonArray()) {
                            for (JsonElement re : runs.getAsJsonArray()) {
                                JsonObject r = re.getAsJsonObject();
                                line.insert(line.length(), r.get("t").getAsString(), intOr(r, "s", 0));
                            }
                        }
                        lines.add(line);
                    }
                }
            }
        } catch (Exception ignored) {
            lines.clear();
        }
        ensureLine();
    }

    private static int intOr(JsonObject o, String key, int def) {
        JsonElement e = o.get(key);
        return e != null && e.isJsonPrimitive() ? e.getAsInt() : def;
    }
}
