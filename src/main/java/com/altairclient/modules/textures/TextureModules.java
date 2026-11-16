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

        private Entry(String group, String name, String description, boolean hasColor, int colorIndex) {
            this.group = group;
            this.name = name;
            this.description = description;
            this.id = group + "/" + name.toLowerCase(Locale.ROOT).replace(' ', '-');
            this.hasColor = hasColor;
            this.colorIndex = colorIndex;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void toggle() {
            enabled = !enabled;
        }

        public boolean hasColor() {
            return hasColor;
        }

        public int getColorIndex() {
            return colorIndex;
        }

        public void setColorIndex(int index) {
            colorIndex = Math.max(0, Math.min(Palette.MAX_INDEX, index));
        }
    }

    private static final List<Entry> ALL = new ArrayList<>();

    static {
        // ==================== 1. PvP Textures ====================
        add(PVP, "Short Swords", "Espadas visualmente más cortas.");
        add(PVP, "Low Fire", "Reduce la altura del fuego.");
        add(PVP, "Transparent Fire", "Fuego semitransparente.");
        add(PVP, "Low Shield", "Reduce la presencia visual del escudo.");
        add(PVP, "Low Particles", "Reduce la cantidad de partículas.");
        add(PVP, "Clear Water", "Agua más transparente.");
        add(PVP, "Clear Lava", "Lava más transparente.");
        add(PVP, "Low Armor Overlay", "Armadura menos invasiva visualmente.");
        add(PVP, "Simple Hit Particles", "Partículas de impacto sencillas.");
        add(PVP, "Critical Particles", "Partículas de crítico discretas.");
        add(PVP, "Minimal Enchantment Glint", "Reduce el brillo de objetos encantados.");
        add(PVP, "Minimal Totem", "Tótem simplificado.");
        add(PVP, "Transparent Pumpkin Overlay", "Máscara de calabaza transparente.");
        add(PVP, "Clean Glass", "Cristal más limpio.");
        add(PVP, "Simple XP Bar", "Barra de experiencia minimalista.");
        add(PVP, "Minimal Hotbar", "Barra de objetos simplificada.");

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

    private TextureModules() {
    }

    private static void add(String group, String name, String description) {
        ALL.add(new Entry(group, name, description, false, DEFAULT_COLOR));
    }

    private static void addColor(String group, String name, String description, int colorIndex) {
        ALL.add(new Entry(group, name, description, true, colorIndex));
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
