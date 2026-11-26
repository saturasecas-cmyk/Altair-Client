package com.altairclient.modules.mods;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.event.RenderNameTagEvent;

/**
 * Custom Nametags: cambia el aspecto de los nombres que se ven sobre las entidades.
 * Solo modifica el texto que se dibuja en tu pantalla; no cambia el nombre real de nadie.
 */
public final class NametagHandler {

    public static final String[] STYLE_NAMES = {"Altair", "Gold", "Neon", "Marked"};
    public static final int STYLE_COUNT = STYLE_NAMES.length;

    private static boolean enabled = false;
    private static int style = 0;
    private static boolean registered = false;

    private NametagHandler() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(NametagHandler::onNameTag);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static int getStyle() {
        return style;
    }

    public static void setStyle(int value) {
        style = Math.floorMod(value, STYLE_COUNT);
    }

    /** Devuelve false para no cancelar el dibujado del nombre. */
    private static boolean onNameTag(RenderNameTagEvent event) {
        if (!enabled) {
            return false;
        }
        Component original = event.getContent();
        if (original == null) {
            return false;
        }

        MutableComponent styled;
        switch (style) {
            case 1 -> styled = original.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            case 2 -> styled = original.copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
            case 3 -> styled = Component.literal("[ ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(original.copy().withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" ]").withStyle(ChatFormatting.DARK_GRAY));
            default -> styled = Component.literal("◆ ").withStyle(ChatFormatting.AQUA)
                    .append(original.copy().withStyle(ChatFormatting.AQUA));
        }
        event.setContent(styled);
        return false;
    }
}
