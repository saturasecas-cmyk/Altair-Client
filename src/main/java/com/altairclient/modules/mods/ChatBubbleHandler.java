package com.altairclient.modules.mods;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Better Chat Bubbles: lo que escribe un jugador aparece unos segundos sobre su cabeza,
 * en una línea bajo su nombre. Solo cambia lo que se dibuja en tu pantalla.
 */
public final class ChatBubbleHandler {

    private static final long DURATION_MS = 6000L;
    private static final int MAX_CHARS = 60;

    private record Bubble(String text, long expiresAt) {
    }

    private static final Map<UUID, Bubble> BUBBLES = new ConcurrentHashMap<>();

    private static boolean enabled = false;
    private static boolean registered = false;

    private ChatBubbleHandler() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ChatBubbleHandler::onChat);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ChatBubbleHandler::onNameTag);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            BUBBLES.clear();
        }
    }

    /** Guarda el mensaje del jugador. Devuelve false para no cancelar el chat. */
    private static void onChat(ClientChatReceivedEvent event) {
        if (!enabled || event.isSystem() || event.getSender() == null) {
            return;
        }
        String text = event.getMessage().getString();
        int gt = text.indexOf("> ");
        if (text.startsWith("<") && gt > 0) {
            text = text.substring(gt + 2);
        }
        if (text == null || text.isBlank()) {
            return;
        }
        text = text.strip();
        if (text.length() > MAX_CHARS) {
            text = text.substring(0, MAX_CHARS - 1) + "…";
        }
        BUBBLES.put(event.getSender(), new Bubble(text, System.currentTimeMillis() + DURATION_MS));
    }

    /** Pone el mensaje como segunda línea del nombre del jugador que lo escribió. */
    private static void onNameTag(RenderNameTagEvent event) {
        if (!enabled || BUBBLES.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Component name = event.getContent();
        if (mc.level == null || name == null) {
            return;
        }

        long now = System.currentTimeMillis();
        String shown = name.getString();

        Iterator<Map.Entry<UUID, Bubble>> it = BUBBLES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Bubble> entry = it.next();
            if (entry.getValue().expiresAt() < now) {
                it.remove();
                continue;
            }
            Player player = mc.level.getPlayerByUUID(entry.getKey());
            if (player != null && shown.contains(player.getName().getString())) {
                event.setContent(name.copy().append(Component.literal("  💬 " + entry.getValue().text())
                        .withStyle(ChatFormatting.YELLOW)));
                break;
            }
        }
        return;
    }
}
