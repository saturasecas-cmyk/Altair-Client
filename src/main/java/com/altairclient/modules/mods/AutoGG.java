package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.List;
import java.util.Locale;

/**
 * Auto GG: escribe "gg" en el chat cuando el servidor anuncia el final de una partida.
 * Solo reacciona a mensajes del servidor (nunca a lo que escriben otros jugadores) y
 * espera 15 segundos entre envíos. Algunos servidores no permiten mensajes automáticos:
 * actívalo solo si el servidor lo admite.
 */
public final class AutoGG {

    private static final List<String> TRIGGERS = List.of(
            "has ganado", "ha ganado la partida", "ganador", "victoria", "fin de la partida",
            "you won", "has won the game", "winner", "victory", "game over", "game ended");
    private static final long COOLDOWN_MS = 15_000L;

    private static boolean enabled = false;
    private static boolean registered = false;
    private static long lastSent = 0L;

    private AutoGG() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            ClientChatReceivedEvent.BUS.addListener(AutoGG::onChat);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    private static void onChat(ClientChatReceivedEvent event) {
        if (event.isSystem()) {
            check(event.getMessage().getString());
        }
    }

    private static void check(String message) {
        if (!enabled || message == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastSent < COOLDOWN_MS) {
            return;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        for (String trigger : TRIGGERS) {
            if (lower.contains(trigger)) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getConnection() != null) {
                    lastSent = now;
                    mc.getConnection().sendChat("gg");
                }
                return;
            }
        }
    }
}
