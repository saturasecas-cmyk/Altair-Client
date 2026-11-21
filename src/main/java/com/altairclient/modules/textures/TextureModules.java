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
    public static final String EXTRA = "extra";
    public static final String MISC = "misc";

    /** Entradas que comparten estado con un módulo que ya existe en la carpeta Mods. */
    public static final String ID_CLEAR_WATER = PVP + "/clear-water";
    public static final String ID_ALTAIR_RAIN = MISC + "/altair-rain";
    public static final String ID_NO_ANIMATION = MISC + "/no-animation";
    public static final String ID_ALTAIR_SKY = MISC + "/altair-sky";

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
            BREAKING + "/clean-block-crack",
            BREAKING + "/thin-block-crack",
            BREAKING + "/dark-block-crack",
            BREAKING + "/white-block-crack",
            BREAKING + "/colored-block-crack",
            BREAKING + "/clean-breaking-overlay",
            BREAKING + "/smooth-breaking-animation",
            BREAKING + "/static-block-crack",
            BREAKING + "/minimal-breaking-particles",
            BREAKING + "/small-block-debris",
            BREAKING + "/reduced-block-dust",
            BREAKING + "/breaking-progress-bar",
            MOVEMENT + "/sprint-particles",
            MOVEMENT + "/jump-particles",
            MOVEMENT + "/landing-particles",
            MOVEMENT + "/footstep-particles",
            MOVEMENT + "/small-speed-lines",
            MOVEMENT + "/minimal-motion-blur",
            MOVEMENT + "/reduced-camera-bobbing",
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
            MISC + "/altair-sky",
            MISC + "/altair-rain",
            MISC + "/no-animation",
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
            PVP + "/low-armor-overlay",
            EXTRA + "/minimal-nametag",
            EXTRA + "/minimal-tablist",
            EXTRA + "/clean-chat",
            EXTRA + "/clean-inventory",
            EXTRA + "/smooth-gui-transitions",
            EXTRA + "/hotbar-highlight",
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
        add(PVP, "Short Swords", "Visually shorter swords.");
        addColor(PVP, "Low Fire", "Lowers the fire height and turns it the client's purple tone.", DEFAULT_COLOR);
        addColor(PVP, "Transparent Fire", "Semi-transparent fire using the client palette.", DEFAULT_COLOR);
        add(PVP, "Low Shield", "Reduces the visual presence of the shield.");
        addColor(PVP, "Low Particles", "Reduces the number of particles and tints them with the client palette.", DEFAULT_COLOR);
        add(PVP, "Clear Water", "More transparent water.");
        add(PVP, "Clear Lava", "More transparent lava.");
        add(PVP, "Low Armor Overlay", "Less visually intrusive armor.");
        addColor(PVP, "Simple Hit Particles", "Simple hit particles using the client palette.", DEFAULT_COLOR);
        add(PVP, "Critical Particles", "Subtle critical hit particles (fewer and less flashy).");
        add(PVP, "Minimal Enchantment Glint", "Removes the glint from enchanted items (the glint can't be tinted with the palette).");
        addColor(PVP, "Minimal Totem", "Simplified totem using the client palette.", DEFAULT_COLOR);
        add(PVP, "Transparent Pumpkin Overlay", "Transparent pumpkin overlay.");
        addColor(PVP, "Simple XP Bar", "Minimalist experience bar using the client palette.", DEFAULT_COLOR);
        addColor(PVP, "Minimal Hotbar", "Simplified hotbar using the client palette.", DEFAULT_COLOR);

        // ==================== 2. Build Textures ====================
        add(BUILD, "Clean Wool", "Wool with uniform colors.");
        add(BUILD, "Smooth Concrete", "Concrete with a simplified look.");
        add(BUILD, "Clean Glass", "Glass without annoying borders.");
        add(BUILD, "Borderless Glass", "Visually connected glass.");
        add(BUILD, "Clean Planks", "Wood with simple patterns.");
        add(BUILD, "Simple Stone", "Simplified stone.");
        add(BUILD, "Clean Bricks", "Minimalist bricks.");
        add(BUILD, "Smooth Quartz", "More uniform quartz.");
        add(BUILD, "Clean Terracotta", "Simplified terracotta.");
        add(BUILD, "Minimal Logs", "Logs with fewer details.");
        add(BUILD, "Clean Leaves", "Cleaner leaves.");
        add(BUILD, "Simple Doors", "Simple doors.");
        add(BUILD, "Simple Trapdoors", "Minimalist trapdoors.");
        add(BUILD, "Clean Slabs", "Uniform slabs.");
        add(BUILD, "Clean Stairs", "Simplified stairs.");
        add(BUILD, "Simple Bed Textures", "Simple beds.");
        add(BUILD, "Clean Ores", "Clearer ores.");
        add(BUILD, "Minimal Redstone", "Simplified redstone.");
        add(BUILD, "Clean Buttons", "Simplified buttons.");

        // ==================== 3. Breaking Effects ====================
        add(BREAKING, "Clean Block Crack", "Simple cracks.");
        add(BREAKING, "Thin Block Crack", "Cracks with thin lines.");
        add(BREAKING, "Dark Block Crack", "Dark cracks.");
        add(BREAKING, "White Block Crack", "White cracks.");
        addColor(BREAKING, "Colored Block Crack", "Colored cracks.", ALTAIR_CYCLE);
        add(BREAKING, "Minimal Breaking Particles", "Fewer particles when breaking.");
        add(BREAKING, "Small Block Debris", "Small fragments.");
        add(BREAKING, "Reduced Block Dust", "Less dust.");
        add(BREAKING, "Clean Breaking Overlay", "Simplified breaking overlay.");
        add(BREAKING, "Smooth Breaking Animation", "Smoother breaking.");
        addColor(BREAKING, "Breaking Progress Bar", "Breaking progress bar.", DEFAULT_COLOR);
        add(BREAKING, "Static Block Crack", "Cracks without extra animation.");

        // ==================== 4. Movement Effects ====================
        add(MOVEMENT, "Sprint Particles", "Particles when sprinting.");
        add(MOVEMENT, "Jump Particles", "Particles when jumping.");
        add(MOVEMENT, "Landing Particles", "Particles when landing.");
        add(MOVEMENT, "Footstep Particles", "Particles on footsteps.");
        addColor(MOVEMENT, "Small Speed Lines", "Subtle speed lines.", DEFAULT_COLOR);
        add(MOVEMENT, "Minimal Motion Blur", "Light motion blur.");
        add(MOVEMENT, "Reduced Camera Bobbing", "Less camera movement.");
        add(MOVEMENT, "Water Splash", "Splashes when entering water.");
        add(MOVEMENT, "Snow Footprints", "Footprints on snow.");
        add(MOVEMENT, "Sand Footprints", "Footprints on sand.");

        // ==================== 5. Walk Effects ====================
        add(WALK, "Walk Dust", "Dust when walking.");
        add(WALK, "Walk Smoke", "Small puffs of smoke.");
        addColor(WALK, "Walk Sparks", "Subtle sparks.", DEFAULT_COLOR);
        add(WALK, "Walk Hearts", "Hearts when walking.");
        addColor(WALK, "Walk Stars", "Small stars.", DEFAULT_COLOR);
        add(WALK, "Walk Snow", "Snow particles.");
        add(WALK, "Walk Leaves", "Leaves when walking.");
        add(WALK, "Walk Bubbles", "Small bubbles.");
        add(WALK, "Walk Footprints", "Temporary footprints.");
        addColor(WALK, "Walk Circles", "Circles under your feet.", DEFAULT_COLOR);
        add(WALK, "Walk Shadow", "Shadow when walking.");
        addColor(WALK, "Walk Aura", "Subtle aura.", DEFAULT_COLOR);
        addColor(WALK, "Walk Altair", "Particles using the Altair palette.", ALTAIR_CYCLE);


        // ==================== 7. Extra Visual Effects ====================
        add(EXTRA, "Minimal Nametag", "Simple player nametag.");
        add(EXTRA, "Minimal Tablist", "Simplified player list.");
        add(EXTRA, "Clean Chat", "Tidier chat.");
        add(EXTRA, "Clean Inventory", "Cleaner inventory.");
        add(EXTRA, "Smooth GUI Transitions", "Smooth transitions between menus.");
        addColor(EXTRA, "Hotbar Highlight", "Highlight of the selected item.", DEFAULT_COLOR);

        // ==================== 8. Misc ====================
        add(MISC, "Altair Sky", "Navy blue sky with a purple horizon and tons of stars, even during the day. Overworld only. No sun or moon: only the light they give remains.");
        addColor(MISC, "Altair Rain", "When it rains, drops and splashes use the client palette, plus colored drops around you and starlight sparks on the ground. It adds to the normal rain.", 8);
        add(MISC, "No Animation", "Reduces animations: no fire on screen, no totem animation, no tilt when taking damage, no screen distortion or FOV changes, and a still enchantment glint.");
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
        throw new IllegalArgumentException("Unknown texture: " + id);
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
