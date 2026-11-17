package com.altairclient.modules.textures;

import com.altairclient.client.Palette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Registro de las texturas y efectos visuales de Altair, agrupados en siete apartados.
 * Cada entrada guarda si está activada y, si tiene color, el color de la paleta del cliente
 * ({@link Palette}). El menú (carpeta "Textures") lee y cambia este estado.
 */
public final class TextureModules {

    public static final String PVP = "pvp";
    public static final String BUILD = "build";
    public static final String BREAKING = "breaking";
    public static final String MOVEMENT = "movement";
    public static final String WALK = "walk";
    public static final String ANIMATIONS = "animations";
    public static final String EXTRA = "extra";

    /** Entradas que comparten estado con un módulo que ya existe en la carpeta Mods. */
    public static final String ID_CLEAR_WATER = PVP + "/clear-water";
    public static final String ID_MINIMAL_OUTLINE = BREAKING + "/minimal-block-outline";

    /** Texturas PvP que ya tienen efecto en el juego (el resto de entradas solo están en el menú por ahora). */
    private static final java.util.Set<String> IMPLEMENTED = new java.util.HashSet<>(java.util.Arrays.asList(
            PVP + "/short-swords",
            PVP + "/low-fire",
            PVP + "/transparent-fire",
            PVP + "/low-shield",
            PVP + "/low-particles",
            PVP + "/clear-water",
            PVP + "/clear-lava",
            PVP + "/simple-hit-particles",
            PVP + "/critical-particles",
            PVP + "/minimal-totem",
            PVP + "/transparent-pumpkin-overlay",
            PVP + "/minimal-enchantment-glint",
            PVP + "/simple-xp-bar",
            PVP + "/minimal-hotbar",
            BREAKING + "/minimal-breaking-particles",
            BREAKING + "/small-block-debris",
            BREAKING + "/reduced-block-dust",
            BREAKING + "/breaking-progress-bar",
            BREAKING + "/minimal-block-outline",
            MOVEMENT + "/sprint-particles",
            MOVEMENT + "/jump-particles",
            MOVEMENT + "/landing-particles",
            MOVEMENT + "/running-dust",
            MOVEMENT + "/walking-dust",
            MOVEMENT + "/footstep-particles",
            MOVEMENT + "/small-speed-lines",
            MOVEMENT + "/reduced-camera-bobbing",
            MOVEMENT + "/landing-dust",
            MOVEMENT + "/water-splash",
            MOVEMENT + "/snow-footprints",
            MOVEMENT + "/sand-footprints",
            WALK + "/walk-dust",
            WALK + "/walk-smoke",
            WALK + "/walk-sparks",
            WALK + "/walk-hearts",
            WALK + "/walk-stars",
            WALK + "/walk-snow",
            WALK + "/walk-leaves",
            WALK + "/walk-bubbles",
            WALK + "/walk-footprints",
            WALK + "/walk-circles",
            WALK + "/walk-shadow",
            WALK + "/walk-aura",
            WALK + "/walk-altair",
            ANIMATIONS + "/smooth-walking",
            ANIMATIONS + "/smooth-sprinting",
            ANIMATIONS + "/smooth-sneaking",
            ANIMATIONS + "/smooth-jumping",
            ANIMATIONS + "/smooth-falling",
            ANIMATIONS + "/smooth-landing",
            ANIMATIONS + "/smooth-swimming",
            ANIMATIONS + "/smooth-climbing",
            ANIMATIONS + "/smooth-elytra",
            ANIMATIONS + "/idle-animation",
            ANIMATIONS + "/hand-swing",
            ANIMATIONS + "/item-swing",
            ANIMATIONS + "/eating-animation",
            ANIMATIONS + "/bow-animation",
            ANIMATIONS + "/rod-animation",
            ANIMATIONS + "/block-animation",
            ANIMATIONS + "/sneak-camera",
            ANIMATIONS + "/sprint-camera",
            PVP + "/clean-glass",
            BUILD + "/clean-wool",
            BUILD + "/smooth-concrete",
            BUILD + "/clean-glass",
            BUILD + "/borderless-glass",
            BUILD + "/clean-planks",
            BUILD + "/simple-stone",
            BUILD + "/clean-bricks",
            BUILD + "/smooth-quartz",
            BUILD + "/clean-terracotta",
            BUILD + "/minimal-logs",
            BUILD + "/clean-leaves",
            BUILD + "/simple-doors",
            BUILD + "/simple-trapdoors",
            BUILD + "/clean-slabs",
            BUILD + "/clean-stairs",
            BUILD + "/simple-bed-textures",
            BUILD + "/clean-ores",
            BUILD + "/minimal-redstone",
            BUILD + "/clean-buttons",
            EXTRA + "/hotbar-highlight",
            EXTRA + "/low-fire-overlay",
            EXTRA + "/clear-pumpkin-overlay",
            EXTRA + "/clean-scoreboard",
            EXTRA + "/minimal-bossbar",
            "__none__"
    ));

    /** Color por defecto de las entradas que llevan color: el primer color de la paleta (morado). */
    private static final int DEFAULT_COLOR = 0;
    /** Índice de "Ciclo Altair" dentro de la paleta. */
    private static final int ALTAIR_CYCLE = 8;

    public static final class Entry {
        public final String id;
        public final String group;
        public final String name;
        public final String description;
        private final boolean hasColor;
        private boolean enabled;
        private int colorIndex;
        /** Si no es null, esta entrada comparte interruptor y color con otra (por ejemplo Low Fire Overlay con Low Fire). */
        private Entry link;

        private Entry(String group, String name, String description, boolean hasColor, int colorIndex) {
            this.group = group;
            this.name = name;
            this.description = description;
            this.id = group + "/" + name.toLowerCase(Locale.ROOT).replace(' ', '-');
            this.hasColor = hasColor;
            this.colorIndex = colorIndex;
        }

        public boolean isEnabled() {
            return link != null ? link.isEnabled() : enabled;
        }

        /** true si la textura ya hace algo en el juego; si no, solo existe en el menú. */
        public boolean isImplemented() {
            return IMPLEMENTED.contains(id);
        }

        public void toggle() {
            if (link != null) {
                link.toggle();
            } else {
                enabled = !enabled;
            }
        }

        public boolean hasColor() {
            return hasColor;
        }

        public int getColorIndex() {
            return link != null ? link.getColorIndex() : colorIndex;
        }

        public void setColorIndex(int index) {
            if (link != null) {
                link.setColorIndex(index);
            } else {
                colorIndex = Math.max(0, Math.min(Palette.MAX_INDEX, index));
            }
        }
    }

    private static final List<Entry> ALL = new ArrayList<>();

    static {
        // ==================== 1. PvP Textures ====================
        add(PVP, "Short Swords", "Espadas visualmente más cortas.");
        addColor(PVP, "Low Fire", "Reduce la altura del fuego y lo pone morado del tono del cliente.", DEFAULT_COLOR);
        addColor(PVP, "Transparent Fire", "Fuego semitransparente con la paleta del cliente.", DEFAULT_COLOR);
        add(PVP, "Low Shield", "Reduce la presencia visual del escudo.");
        addColor(PVP, "Low Particles", "Reduce la cantidad de partículas y las tiñe con la paleta del cliente.", DEFAULT_COLOR);
        add(PVP, "Clear Water", "Agua más transparente.");
        add(PVP, "Clear Lava", "Lava más transparente.");
        add(PVP, "Low Armor Overlay", "Armadura menos invasiva visualmente.");
        addColor(PVP, "Simple Hit Particles", "Partículas de impacto sencillas con la paleta del cliente.", DEFAULT_COLOR);
        add(PVP, "Critical Particles", "Partículas de crítico discretas (menos y más sutiles).");
        add(PVP, "Minimal Enchantment Glint", "Quita el brillo de los objetos encantados (el brillo no se puede teñir con la paleta).");
        addColor(PVP, "Minimal Totem", "Tótem simplificado con la paleta del cliente.", DEFAULT_COLOR);
        add(PVP, "Transparent Pumpkin Overlay", "Máscara de calabaza transparente.");
        add(PVP, "Clean Glass", "Cristal más limpio.");
        addColor(PVP, "Simple XP Bar", "Barra de experiencia minimalista con la paleta del cliente.", DEFAULT_COLOR);
        addColor(PVP, "Minimal Hotbar", "Barra de objetos simplificada con la paleta del cliente.", DEFAULT_COLOR);

        // ==================== 2. Build Textures ====================
        add(BUILD, "Clean Wool", "Lana con colores uniformes.");
        add(BUILD, "Smooth Concrete", "Hormigón con apariencia simplificada.");
        add(BUILD, "Clean Glass", "Cristal sin bordes molestos.");
        add(BUILD, "Borderless Glass", "Cristales conectados visualmente.");
        add(BUILD, "Clean Planks", "Madera con patrones sencillos.");
        add(BUILD, "Simple Stone", "Piedra simplificada.");
        add(BUILD, "Clean Bricks", "Ladrillos minimalistas.");
        add(BUILD, "Smooth Quartz", "Cuarzo más uniforme.");
        add(BUILD, "Clean Terracotta", "Terracota simplificada.");
        add(BUILD, "Minimal Logs", "Troncos con menos detalles.");
        add(BUILD, "Clean Leaves", "Hojas más limpias.");
        add(BUILD, "Simple Doors", "Puertas sencillas.");
        add(BUILD, "Simple Trapdoors", "Trampillas minimalistas.");
        add(BUILD, "Clean Slabs", "Losas uniformes.");
        add(BUILD, "Clean Stairs", "Escaleras simplificadas.");
        add(BUILD, "Simple Bed Textures", "Camas sencillas.");
        add(BUILD, "Clean Ores", "Minerales más claros.");
        add(BUILD, "Minimal Redstone", "Redstone simplificada.");
        add(BUILD, "Clean Buttons", "Botones simplificados.");

        // ==================== 3. Breaking Effects ====================
        add(BREAKING, "Clean Block Crack", "Grietas sencillas.");
        add(BREAKING, "Thin Block Crack", "Grietas con líneas finas.");
        add(BREAKING, "Dark Block Crack", "Grietas oscuras.");
        add(BREAKING, "White Block Crack", "Grietas blancas.");
        addColor(BREAKING, "Colored Block Crack", "Grietas de colores.", ALTAIR_CYCLE);
        add(BREAKING, "Minimal Breaking Particles", "Menos partículas al romper.");
        add(BREAKING, "Small Block Debris", "Fragmentos pequeños.");
        add(BREAKING, "Reduced Block Dust", "Menos polvo.");
        add(BREAKING, "Clean Breaking Overlay", "Superposición de rotura simplificada.");
        add(BREAKING, "Smooth Breaking Animation", "Rotura más fluida.");
        addColor(BREAKING, "Breaking Progress Bar", "Barra de progreso de rotura.", DEFAULT_COLOR);
        addColor(BREAKING, "Minimal Block Outline", "Contorno fino del bloque.", DEFAULT_COLOR);
        add(BREAKING, "Static Block Crack", "Grietas sin animación adicional.");

        // ==================== 4. Movement Effects ====================
        add(MOVEMENT, "Sprint Particles", "Partículas al correr.");
        add(MOVEMENT, "Jump Particles", "Partículas al saltar.");
        add(MOVEMENT, "Landing Particles", "Partículas al aterrizar.");
        add(MOVEMENT, "Running Dust", "Polvo al correr.");
        add(MOVEMENT, "Walking Dust", "Polvo al caminar.");
        add(MOVEMENT, "Footstep Particles", "Partículas en las pisadas.");
        addColor(MOVEMENT, "Small Speed Lines", "Líneas de velocidad discretas.", DEFAULT_COLOR);
        add(MOVEMENT, "Minimal Motion Blur", "Desenfoque ligero de movimiento.");
        add(MOVEMENT, "Reduced Camera Bobbing", "Menos movimiento de cámara.");
        add(MOVEMENT, "Landing Dust", "Polvo al tocar el suelo.");
        add(MOVEMENT, "Water Splash", "Salpicaduras al entrar en agua.");
        add(MOVEMENT, "Snow Footprints", "Huellas sobre nieve.");
        add(MOVEMENT, "Sand Footprints", "Huellas sobre arena.");

        // ==================== 5. Walk Effects ====================
        add(WALK, "Walk Dust", "Polvo al caminar.");
        add(WALK, "Walk Smoke", "Pequeñas nubes de humo.");
        addColor(WALK, "Walk Sparks", "Chispas discretas.", DEFAULT_COLOR);
        add(WALK, "Walk Hearts", "Corazones al caminar.");
        addColor(WALK, "Walk Stars", "Estrellas pequeñas.", DEFAULT_COLOR);
        add(WALK, "Walk Snow", "Partículas de nieve.");
        add(WALK, "Walk Leaves", "Hojas al caminar.");
        add(WALK, "Walk Bubbles", "Burbujas pequeñas.");
        add(WALK, "Walk Footprints", "Huellas temporales.");
        addColor(WALK, "Walk Circles", "Círculos bajo los pies.", DEFAULT_COLOR);
        add(WALK, "Walk Shadow", "Sombra al caminar.");
        addColor(WALK, "Walk Aura", "Aura sutil.", DEFAULT_COLOR);
        addColor(WALK, "Walk Altair", "Partículas con la paleta de Altair.", ALTAIR_CYCLE);

        // ==================== 6. Movement Animations ====================
        add(ANIMATIONS, "Smooth Walking", "Caminar más fluido.");
        add(ANIMATIONS, "Smooth Sprinting", "Correr más suavemente.");
        add(ANIMATIONS, "Smooth Sneaking", "Agacharse con una animación fluida.");
        add(ANIMATIONS, "Smooth Jumping", "Animación de salto mejorada.");
        add(ANIMATIONS, "Smooth Falling", "Caída más fluida.");
        add(ANIMATIONS, "Smooth Landing", "Transición al aterrizar.");
        add(ANIMATIONS, "Smooth Swimming", "Animación de natación.");
        add(ANIMATIONS, "Smooth Climbing", "Animación al subir escaleras.");
        add(ANIMATIONS, "Smooth Elytra", "Movimiento de élitros.");
        add(ANIMATIONS, "Idle Animation", "Pequeños movimientos estando quieto.");
        add(ANIMATIONS, "Hand Swing", "Movimiento de manos.");
        add(ANIMATIONS, "Item Swing", "Animación al utilizar objetos.");
        add(ANIMATIONS, "Eating Animation", "Animación al comer.");
        add(ANIMATIONS, "Bow Animation", "Animación al tensar el arco.");
        add(ANIMATIONS, "Rod Animation", "Animación de caña de pescar.");
        add(ANIMATIONS, "Block Animation", "Animación al colocar bloques.");
        add(ANIMATIONS, "Sneak Camera", "Movimiento de cámara al agacharse.");
        add(ANIMATIONS, "Sprint Camera", "Movimiento de cámara al correr.");

        // ==================== 7. Extra Visual Effects ====================
        add(EXTRA, "Minimal Nametag", "Nombre de jugador sencillo.");
        add(EXTRA, "Clean Scoreboard", "Marcador más limpio.");
        add(EXTRA, "Minimal Tablist", "Lista de jugadores simplificada.");
        add(EXTRA, "Clean Chat", "Chat más ordenado.");
        add(EXTRA, "Minimal Bossbar", "Barra de jefe simplificada.");
        add(EXTRA, "Clean Inventory", "Inventario más limpio.");
        add(EXTRA, "Smooth GUI Transitions", "Transiciones suaves entre menús.");
        addColor(EXTRA, "Hotbar Highlight", "Resaltado del objeto seleccionado.", DEFAULT_COLOR);
        add(EXTRA, "Low Fire Overlay", "Superposición de fuego reducida.");
        add(EXTRA, "Clear Pumpkin Overlay", "Máscara de calabaza simplificada.");
    }

    static {
        byId(PVP + "/clean-glass").link = byId(BUILD + "/clean-glass");
        byId(EXTRA + "/low-fire-overlay").link = byId(PVP + "/low-fire");
        byId(EXTRA + "/clear-pumpkin-overlay").link = byId(PVP + "/transparent-pumpkin-overlay");
    }

    private TextureModules() {
    }

    private static void add(String group, String name, String description) {
        ALL.add(new Entry(group, name, description, false, DEFAULT_COLOR));
    }

    private static void addColor(String group, String name, String description, int colorIndex) {
        ALL.add(new Entry(group, name, description, true, colorIndex));
    }

    /** Busca una entrada por su id (por ejemplo "pvp/low-fire"). */
    public static Entry byId(String id) {
        for (Entry e : ALL) {
            if (e.id.equals(id)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Textura desconocida: " + id);
    }

    /** Todas las entradas, en el orden del menú. */
    public static List<Entry> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** Las entradas de un apartado (por ejemplo {@link #PVP}), en el orden del menú. */
    public static List<Entry> group(String group) {
        List<Entry> list = new ArrayList<>();
        for (Entry e : ALL) {
            if (e.group.equals(group)) {
                list.add(e);
            }
        }
        return list;
    }
}
