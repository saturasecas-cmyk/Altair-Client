package com.altairclient.modules.textures;

import com.altairclient.client.Palette;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.event.TickEvent;

import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Build Textures. Al activar una, Altair genera un resource pack (carpeta "altair_build_textures" dentro de
 * resourcepacks) a partir de los assets de vanilla de tu propio Minecraft, lo activa y recarga los recursos.
 * No se copia ningún asset de Minecraft al mod: las texturas se simplifican en tu ordenador al activarlas.
 *
 * Los cambios se aplican un segundo después de dejar de tocar los interruptores, porque cada cambio
 * recarga los recursos del juego.
 */
public final class BuildTextures {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String PACK_FOLDER = "altair_build_textures";
    private static final String MC = "minecraft";
    private static final String B = TextureModules.BUILD;

    private static boolean registered = false;
    /** null = todavía no se ha comprobado nada desde que arrancó el juego. */
    private static String appliedSignature = null;
    private static String pendingSignature = null;
    private static int stableTicks = 0;
    private static volatile boolean busy = false;

    private BuildTextures() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(BuildTextures::onTick);
        }
    }

    // ==================== Reglas ====================

    private interface Op {
        void apply(BufferedImage image);
    }

    private static final class Rule {
        final String entryId;
        final Predicate<String> match;
        final Op op;

        Rule(String entryId, Predicate<String> match, Op op) {
            this.entryId = entryId;
            this.match = match;
            this.op = op;
        }
    }

    private static Set<String> setOf(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    private static final Set<String> STONES = setOf("stone", "cobblestone", "mossy_cobblestone", "andesite", "diorite",
            "granite", "polished_andesite", "polished_diorite", "polished_granite", "smooth_stone", "deepslate",
            "cobbled_deepslate", "polished_deepslate", "tuff", "polished_tuff", "blackstone", "polished_blackstone");

    private static final Set<String> REDSTONE = setOf("redstone_block", "redstone_dust_dot", "redstone_dust_line0",
            "redstone_dust_line1", "redstone_dust_overlay", "redstone_lamp", "redstone_lamp_on", "redstone_torch",
            "redstone_torch_off", "repeater", "repeater_on", "comparator", "comparator_on");

    private static boolean isGlass(String n) {
        return n.equals("glass") || n.equals("tinted_glass") || n.endsWith("_stained_glass")
                || n.equals("glass_pane_top") || n.endsWith("_stained_glass_pane_top");
    }

    /** Reglas de texturas de bloque, en el orden del menú (si dos tocan la misma textura, gana la última). */
    private static final List<Rule> BLOCK_RULES = new ArrayList<>();

    static {
        BLOCK_RULES.add(new Rule(B + "/clean-wool", n -> n.endsWith("_wool"), img -> soften(img, 0.0F)));
        BLOCK_RULES.add(new Rule(B + "/smooth-concrete", n -> n.endsWith("_concrete"), img -> soften(img, 0.0F)));
        BLOCK_RULES.add(new Rule(B + "/smooth-concrete", n -> n.endsWith("_concrete_powder"), img -> soften(img, 0.3F)));
        BLOCK_RULES.add(new Rule(B + "/clean-glass", BuildTextures::isGlass, BuildTextures::fadeBorder));
        BLOCK_RULES.add(new Rule(B + "/borderless-glass", BuildTextures::isGlass, BuildTextures::removeBorder));
        BLOCK_RULES.add(new Rule(B + "/clean-planks", n -> n.endsWith("_planks"), img -> soften(img, 0.35F)));
        BLOCK_RULES.add(new Rule(B + "/simple-stone", STONES::contains, img -> soften(img, 0.3F)));
        BLOCK_RULES.add(new Rule(B + "/clean-bricks", n -> n.endsWith("bricks") && !n.startsWith("chiseled"), img -> soften(img, 0.55F)));
        BLOCK_RULES.add(new Rule(B + "/smooth-quartz",
                n -> n.startsWith("quartz_block") || n.startsWith("quartz_pillar") || n.startsWith("chiseled_quartz")
                        || n.equals("quartz_bricks") || n.equals("smooth_quartz"),
                img -> soften(img, 0.2F)));
        BLOCK_RULES.add(new Rule(B + "/clean-terracotta",
                n -> n.equals("terracotta") || (n.endsWith("_terracotta") && !n.endsWith("_glazed_terracotta")),
                img -> soften(img, 0.0F)));
        BLOCK_RULES.add(new Rule(B + "/clean-terracotta", n -> n.endsWith("_glazed_terracotta"), img -> soften(img, 0.5F)));
        BLOCK_RULES.add(new Rule(B + "/minimal-logs",
                n -> !n.startsWith("campfire") && (n.endsWith("_log") || n.endsWith("_log_top")
                        || n.endsWith("_stem") || n.endsWith("_stem_top")),
                img -> soften(img, 0.35F)));
        BLOCK_RULES.add(new Rule(B + "/clean-leaves", n -> n.endsWith("_leaves"), img -> soften(img, 0.35F)));
        BLOCK_RULES.add(new Rule(B + "/simple-doors", n -> n.endsWith("_door_top") || n.endsWith("_door_bottom"), img -> soften(img, 0.4F)));
        BLOCK_RULES.add(new Rule(B + "/simple-trapdoors", n -> n.endsWith("_trapdoor"), img -> soften(img, 0.4F)));
        BLOCK_RULES.add(new Rule(B + "/clean-ores", n -> n.endsWith("_ore"), BuildTextures::brightenOre));
        BLOCK_RULES.add(new Rule(B + "/minimal-redstone", REDSTONE::contains, img -> soften(img, 0.8F)));
    }

    // ==================== Tick ====================

    private static final String K = TextureModules.BREAKING;
    private static final String[] CRACK_IDS = {K + "/clean-block-crack", K + "/thin-block-crack", K + "/dark-block-crack",
            K + "/white-block-crack", K + "/colored-block-crack", K + "/clean-breaking-overlay",
            K + "/smooth-breaking-animation", K + "/static-block-crack"};

    private static String signature() {
        StringBuilder sb = new StringBuilder();
        for (TextureModules.Entry e : TextureModules.group(B)) {
            if (e.isEnabled()) {
                sb.append(e.id).append(';');
            }
        }
        for (String id : CRACK_IDS) {
            TextureModules.Entry e = TextureModules.byId(id);
            if (e != null && e.isEnabled()) {
                sb.append(id).append(';');
                if (e.hasColor()) {
                    sb.append('#').append(e.getColorIndex()).append(';');
                }
            }
        }
        return sb.toString();
    }

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (busy || !mc.isGameLoadFinished()) {
            return;
        }
        // Los cambios se aplican al salir del menú de Altair (recargar recursos con el menú abierto lo congela).
        if (mc.screen instanceof com.altairclient.client.AltairScreen) {
            return;
        }
        String sig = signature();
        if (sig.equals(appliedSignature)) {
            pendingSignature = null;
            return;
        }
        if (!sig.equals(pendingSignature)) {
            pendingSignature = sig;
            stableTicks = 0;
            return;
        }
        if (++stableTicks < 3) {
            return;
        }
        apply(mc, sig);
    }

    // ==================== Aplicar ====================

    private static void apply(Minecraft mc, String sig) {
        busy = true;
        appliedSignature = sig;
        pendingSignature = null;
        try {
            if (sig.isEmpty()) {
                deactivate(mc);
                return;
            }
            Set<String> enabled = new HashSet<>(Arrays.asList(sig.split(";")));
            Path dir = mc.getResourcePackDirectory().resolve(PACK_FOLDER);
            VanillaPackResources vanilla = mc.getVanillaPackResources();
            CompletableFuture.runAsync(() -> generate(vanilla, dir, enabled))
                    .whenComplete((v, error) -> mc.execute(() -> {
                        if (error != null) {
                            LOGGER.error("[Altair] Could not generate the Build Textures pack", error);
                            busy = false;
                            return;
                        }
                        activate(mc);
                    }));
        } catch (RuntimeException e) {
            LOGGER.error("[Altair] Error applying Build Textures", e);
            busy = false;
        }
    }

    private static String findPackId(PackRepository repo) {
        for (String id : repo.getAvailableIds()) {
            if (id.endsWith(PACK_FOLDER)) {
                return id;
            }
        }
        return null;
    }

    private static void activate(Minecraft mc) {
        try {
            PackRepository repo = mc.getResourcePackRepository();
            repo.reload();
            String id = findPackId(repo);
            if (id == null) {
                LOGGER.warn("[Altair] The Build Textures pack is not in the pack list");
                busy = false;
                return;
            }
            List<String> selected = new ArrayList<>(repo.getSelectedIds());
            if (!selected.contains(id)) {
                selected.add(id);
            }
            repo.setSelected(selected);
            mc.options.updateResourcePacks(repo);
            mc.reloadResourcePacks().whenComplete((v, t) -> busy = false);
        } catch (RuntimeException e) {
            LOGGER.error("[Altair] Error enabling the Build Textures pack", e);
            busy = false;
        }
    }

    private static void deactivate(Minecraft mc) {
        PackRepository repo = mc.getResourcePackRepository();
        String id = findPackId(repo);
        if (id != null && repo.getSelectedIds().contains(id)) {
            List<String> selected = new ArrayList<>(repo.getSelectedIds());
            selected.remove(id);
            repo.setSelected(selected);
            mc.options.updateResourcePacks(repo);
            mc.reloadResourcePacks().whenComplete((v, t) -> busy = false);
        } else {
            busy = false;
        }
    }

    // ==================== Generación del pack ====================

    private static void generate(VanillaPackResources vanilla, Path dir, Set<String> enabled) {
        try {
            cleanDir(dir);
            Files.createDirectories(dir);
            writePackMeta(vanilla, dir);

            Map<String, IoSupplier<InputStream>> blockTextures = list(vanilla, "textures/block");

            // Texturas de bloque
            for (Rule rule : BLOCK_RULES) {
                if (!enabled.contains(rule.entryId)) {
                    continue;
                }
                for (Map.Entry<String, IoSupplier<InputStream>> e : blockTextures.entrySet()) {
                    String path = e.getKey();
                    if (!path.endsWith(".png")) {
                        continue;
                    }
                    String name = path.substring("textures/block/".length(), path.length() - 4);
                    if (rule.match.test(name)) {
                        transform(e.getValue(), rule.op, dir.resolve("assets/" + MC + "/" + path));
                        copyMeta(blockTextures, path, dir.resolve("assets/" + MC + "/" + path + ".mcmeta"));
                    }
                }
            }

            generateCracks(blockTextures, dir, enabled);

            // Camas (si esta versión las trae como textura suelta)
            if (enabled.contains(B + "/simple-bed-textures")) {
                for (Map.Entry<String, IoSupplier<InputStream>> e : list(vanilla, "textures/entity/bed").entrySet()) {
                    if (e.getKey().endsWith(".png")) {
                        transform(e.getValue(), img -> soften(img, 0.4F), dir.resolve("assets/" + MC + "/" + e.getKey()));
                    }
                }
            }

            // Losas, escaleras y botones: se redirigen sus modelos a texturas planas propias
            Set<String> cleanNeeded = new HashSet<>();
            Map<String, IoSupplier<InputStream>> models = list(vanilla, "models/block");
            for (Map.Entry<String, IoSupplier<InputStream>> e : models.entrySet()) {
                String file = e.getKey().substring("models/block/".length());
                if (!file.endsWith(".json")) {
                    continue;
                }
                String name = file.substring(0, file.length() - 5);
                boolean wanted = (enabled.contains(B + "/clean-slabs") && (isSpecific(name, "slab") || isSpecific(name, "slab_top")))
                        || (enabled.contains(B + "/clean-stairs")
                        && (isSpecific(name, "stairs") || isSpecific(name, "stairs_inner") || isSpecific(name, "stairs_outer")))
                        || (enabled.contains(B + "/clean-buttons")
                        && (isSpecific(name, "button") || isSpecific(name, "button_pressed") || isSpecific(name, "button_inventory")));
                if (wanted) {
                    redirectModel(e.getValue(), blockTextures, cleanNeeded, dir.resolve("assets/" + MC + "/" + e.getKey()));
                }
            }
            for (String name : cleanNeeded) {
                IoSupplier<InputStream> src = blockTextures.get("textures/block/" + name + ".png");
                if (src != null) {
                    transform(src, img -> soften(img, 0.15F), dir.resolve("assets/altairclient/textures/block/clean/" + name + ".png"));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Grietas al romper (Breaking Effects) ====================

    private static void generateCracks(Map<String, IoSupplier<InputStream>> blockTextures, Path dir, Set<String> enabled)
            throws IOException {
        boolean any = false;
        for (String id : CRACK_IDS) {
            any |= enabled.contains(id);
        }
        if (!any) {
            return;
        }
        int color = Palette.color(TextureModules.byId(K + "/colored-block-crack").getColorIndex()) & 0x00FFFFFF;
        for (int stage = 0; stage < 10; stage++) {
            // Static Block Crack: todas las etapas usan la misma grieta (la de la mitad)
            int srcStage = enabled.contains(K + "/static-block-crack") ? 4 : stage;
            IoSupplier<InputStream> src = blockTextures.get("textures/block/destroy_stage_" + srcStage + ".png");
            if (src == null) {
                continue;
            }
            final int s = stage;
            Op op = img -> {
                if (enabled.contains(K + "/clean-block-crack")) {
                    crisp(img);
                }
                if (enabled.contains(K + "/thin-block-crack")) {
                    thin(img);
                }
                if (enabled.contains(K + "/clean-breaking-overlay")) {
                    scaleAlpha(img, 0.6F);
                }
                if (enabled.contains(K + "/smooth-breaking-animation")) {
                    scaleAlpha(img, 0.3F + 0.7F * (s / 9.0F));
                }
                if (enabled.contains(K + "/dark-block-crack")) {
                    tintCrack(img, 0x000000);
                }
                if (enabled.contains(K + "/white-block-crack")) {
                    tintCrack(img, 0xFFFFFF);
                }
                if (enabled.contains(K + "/colored-block-crack")) {
                    tintCrack(img, color);
                }
            };
            transform(src, op, dir.resolve("assets/" + MC + "/textures/block/destroy_stage_" + stage + ".png"));
        }
    }

    /** Alpha a todo o nada: grietas nítidas, sin semitransparencias. */
    private static void crisp(BufferedImage img) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) >= 100 ? 255 : 0;
                img.setRGB(x, y, (a << 24) | (argb & 0x00FFFFFF));
            }
        }
    }

    /** Deja solo el contorno de las grietas: los píxeles rodeados por otros opacos se borran. */
    private static void thin(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] alpha = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                alpha[y * w + x] = img.getRGB(x, y) >>> 24;
            }
        }
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (alpha[y * w + x] > 0 && alpha[y * w + x - 1] > 0 && alpha[y * w + x + 1] > 0
                        && alpha[(y - 1) * w + x] > 0 && alpha[(y + 1) * w + x] > 0) {
                    img.setRGB(x, y, img.getRGB(x, y) & 0x00FFFFFF);
                }
            }
        }
    }

    private static void scaleAlpha(BufferedImage img, float f) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = Math.min(255, Math.round((argb >>> 24) * f));
                img.setRGB(x, y, (a << 24) | (argb & 0x00FFFFFF));
            }
        }
    }

    /** Cambia el color de la grieta conservando su transparencia. */
    private static void tintCrack(BufferedImage img, int rgb) {
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int a = img.getRGB(x, y) >>> 24;
                if (a > 0) {
                    img.setRGB(x, y, (a << 24) | (rgb & 0x00FFFFFF));
                }
            }
        }
    }

    /** true si el modelo es de un bloque concreto (oak_slab) y no la plantilla genérica (slab). */
    private static boolean isSpecific(String name, String suffix) {
        return name.endsWith("_" + suffix) && !name.equals(suffix) && !name.equals("inner_" + suffix) && !name.equals("outer_" + suffix);
    }

    private static void redirectModel(IoSupplier<InputStream> source, Map<String, IoSupplier<InputStream>> blockTextures,
                                      Set<String> cleanNeeded, Path out) throws IOException {
        JsonObject model;
        try (InputStream in = source.get()) {
            model = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
        JsonElement texturesEl = model.get("textures");
        if (texturesEl == null || !texturesEl.isJsonObject()) {
            return;
        }
        JsonObject textures = texturesEl.getAsJsonObject();
        boolean changed = false;
        for (String key : new ArrayList<>(textures.keySet())) {
            JsonElement value = textures.get(key);
            if (!value.isJsonPrimitive()) {
                continue;
            }
            String ref = value.getAsString();
            if (ref.startsWith("#")) {
                continue;
            }
            if (ref.startsWith(MC + ":")) {
                ref = ref.substring(MC.length() + 1);
            }
            if (ref.indexOf(':') >= 0 || !ref.startsWith("block/")) {
                continue;
            }
            String name = ref.substring("block/".length());
            if (blockTextures.containsKey("textures/block/" + name + ".png")) {
                cleanNeeded.add(name);
                textures.addProperty(key, "altairclient:block/clean/" + name);
                changed = true;
            }
        }
        if (changed) {
            Files.createDirectories(out.getParent());
            Files.writeString(out, model.toString(), StandardCharsets.UTF_8);
        }
    }

    private static void writePackMeta(VanillaPackResources vanilla, Path dir) throws IOException {
        JsonObject root = null;
        try {
            IoSupplier<InputStream> meta = vanilla.getRootResource("pack.mcmeta");
            if (meta != null) {
                try (InputStream in = meta.get()) {
                    root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                }
            }
        } catch (RuntimeException | IOException ignored) {
            root = null;
        }
        if (root == null || !root.has("pack") || !root.get("pack").isJsonObject()) {
            root = new JsonObject();
            JsonObject pack = new JsonObject();
            JsonArray min = new JsonArray();
            min.add(SharedConstants.RESOURCE_PACK_FORMAT_MAJOR);
            min.add(SharedConstants.RESOURCE_PACK_FORMAT_MINOR);
            JsonArray max = new JsonArray();
            max.add(SharedConstants.RESOURCE_PACK_FORMAT_MAJOR);
            max.add(SharedConstants.RESOURCE_PACK_FORMAT_MINOR);
            pack.add("min_format", min);
            pack.add("max_format", max);
            root.add("pack", pack);
        }
        root.getAsJsonObject("pack").addProperty("description", "Altair Build Textures (auto-generated)");
        Files.writeString(dir.resolve("pack.mcmeta"), root.toString(), StandardCharsets.UTF_8);
    }

    private static Map<String, IoSupplier<InputStream>> list(VanillaPackResources vanilla, String prefix) {
        Map<String, IoSupplier<InputStream>> out = new LinkedHashMap<>();
        vanilla.listResources(PackType.CLIENT_RESOURCES, MC, prefix, (id, supplier) -> out.put(id.getPath(), supplier));
        return out;
    }

    private static void copyMeta(Map<String, IoSupplier<InputStream>> all, String pngPath, Path out) throws IOException {
        IoSupplier<InputStream> meta = all.get(pngPath + ".mcmeta");
        if (meta != null) {
            Files.createDirectories(out.getParent());
            try (InputStream in = meta.get()) {
                Files.write(out, in.readAllBytes());
            }
        }
    }

    private static void transform(IoSupplier<InputStream> source, Op op, Path out) throws IOException {
        BufferedImage read;
        try (InputStream in = source.get()) {
            read = ImageIO.read(in);
        }
        if (read == null) {
            return;
        }
        BufferedImage img = new BufferedImage(read.getWidth(), read.getHeight(), BufferedImage.TYPE_INT_ARGB);
        img.getGraphics().drawImage(read, 0, 0, null);
        op.apply(img);
        Files.createDirectories(out.getParent());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bytes);
        Files.write(out, bytes.toByteArray());
    }

    private static void cleanDir(Path dir) throws IOException {
        // Solo se borra dentro de la carpeta propia del pack
        if (!Files.exists(dir) || dir.getFileName() == null || !PACK_FOLDER.equals(dir.getFileName().toString())) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) walk.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        }
    }

    // ==================== Operaciones sobre imágenes ====================

    /** Acerca los colores de cada píxel a la media de la textura: k=0 la deja lisa, k=1 no cambia nada. */
    private static void soften(BufferedImage img, float k) {
        int[] avg = average(img);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) {
                    continue;
                }
                int r = mixToward(avg[0], (argb >> 16) & 0xFF, k);
                int g = mixToward(avg[1], (argb >> 8) & 0xFF, k);
                int b = mixToward(avg[2], argb & 0xFF, k);
                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    private static int mixToward(int avg, int value, float k) {
        return Math.max(0, Math.min(255, Math.round(avg + (value - avg) * k)));
    }

    /** Color medio de los píxeles visibles (alpha > 0). */
    private static int[] average(BufferedImage img) {
        long r = 0;
        long g = 0;
        long b = 0;
        long n = 0;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                r += (argb >> 16) & 0xFF;
                g += (argb >> 8) & 0xFF;
                b += argb & 0xFF;
                n++;
            }
        }
        if (n == 0) {
            return new int[]{128, 128, 128};
        }
        return new int[]{(int) (r / n), (int) (g / n), (int) (b / n)};
    }

    private static boolean isBorder(int x, int y, int tile) {
        int ty = y % tile;
        return x == 0 || ty == 0 || x == tile - 1 || ty == tile - 1;
    }

    /** Clean Glass: el borde del cristal se queda, pero mucho más tenue. */
    private static void fadeBorder(BufferedImage img) {
        int tile = img.getWidth();
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (isBorder(x, y, tile)) {
                    int argb = img.getRGB(x, y);
                    int a = Math.round((argb >>> 24) * 0.35F);
                    img.setRGB(x, y, (a << 24) | (argb & 0x00FFFFFF));
                }
            }
        }
    }

    /** Borderless Glass: el borde copia el píxel de al lado, así los cristales contiguos se ven como uno solo. */
    private static void removeBorder(BufferedImage img) {
        int tile = img.getWidth();
        if (tile < 3) {
            return;
        }
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if (isBorder(x, y, tile)) {
                    int ty = y % tile;
                    int sx = Math.max(1, Math.min(tile - 2, x));
                    int sy = y - ty + Math.max(1, Math.min(tile - 2, ty));
                    img.setRGB(x, y, img.getRGB(sx, sy));
                }
            }
        }
    }

    /** Clean Ores: la roca de fondo se aplana y los minerales ganan color y brillo. */
    private static void brightenOre(BufferedImage img) {
        int[] avg = average(img);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int argb = img.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) {
                    continue;
                }
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                double dist = Math.sqrt(sq(r - avg[0]) + sq(g - avg[1]) + sq(b - avg[2]));
                if (dist > 55.0) {
                    float[] hsb = Color.RGBtoHSB(r, g, b, null);
                    int rgb = Color.HSBtoRGB(hsb[0], Math.min(1.0F, hsb[1] * 1.3F), Math.min(1.0F, hsb[2] * 1.15F));
                    img.setRGB(x, y, (a << 24) | (rgb & 0x00FFFFFF));
                } else {
                    int nr = mixToward(avg[0], r, 0.3F);
                    int ng = mixToward(avg[1], g, 0.3F);
                    int nb = mixToward(avg[2], b, 0.3F);
                    img.setRGB(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
                }
            }
        }
    }

    private static double sq(double v) {
        return v * v;
    }
}
