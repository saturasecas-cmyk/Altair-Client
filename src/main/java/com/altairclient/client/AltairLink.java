package com.altairclient.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.TickEvent;

/**
 * Altair Link: los clientes con Altair Client se reconocen entre si por el chat del servidor.
 * Al entrar se envia un saludo (flor de lis + marca invisible); los demas clientes lo ocultan, te
 * marcan como usuario y contestan con una confirmacion tambien oculta. Quien no lleva el mod
 * solo ve un mensaje con una flor de lis.
 */
public final class AltairLink {

    /** Marca de saludo: flor de lis + caracteres de ancho cero. */
    static final String HELLO = "⚜​‌‍⁠";
    /** Marca de respuesta: no provoca nuevas respuestas. */
    static final String ACK = "⚜​‍‌⁠";

    private static boolean registered;
    private static long ticks;
    private static Object lastPlayer;
    private static long helloAt = -1;
    private static long ackAt = -1;
    private static long lastSent = -1000;

    private AltairLink() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AltairLink::onTick);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(AltairLink::onChat);
    }

    // ==================== Recepcion ====================

    private static void onChat(ClientChatReceivedEvent event) {
        String text = event.getMessage().getString();
        if (!isMarker(text)) {
            return;
        }
        UUID sender = event.getSender();
        if (sender == null || (sender.getMostSignificantBits() == 0L && sender.getLeastSignificantBits() == 0L)) {
            sender = findSender(text);
        }
        handle(sender, text.contains(HELLO));
        event.setCanceled(true);
    }

    private static boolean isMarker(String text) {
        return text != null && (text.contains(HELLO) || text.contains(ACK));
    }

    private static void handle(UUID sender, boolean hello) {
        if (sender != null) {
            AltairUsers.mark(sender);
        }
        if (hello && sender != null && !isSelf(sender) && ackAt < 0) {
            // Contestamos con un pequeno retraso aleatorio para no saturar el chat
            ackAt = ticks + 40 + (long) (Math.random() * 100);
        }
    }

    private static boolean isSelf(UUID id) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && id.equals(mc.player.getUUID());
    }

    /** Para servidores que reescriben el chat como mensaje de sistema: busca el nick mas largo que aparece en el texto. */
    private static UUID findSender(String text) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) {
            return null;
        }
        int cut = text.indexOf('⚜');
        String head = cut >= 0 ? text.substring(0, cut) : text;
        UUID best = null;
        int bestLen = 0;
        for (PlayerInfo info : connection.getListedOnlinePlayers()) {
            String name = info.getProfile().getName();
            if (name != null && name.length() > bestLen && head.contains(name)) {
                best = info.getProfile().getId();
                bestLen = name.length();
            }
        }
        return best;
    }

    // ==================== Envio ====================

    private static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ticks++;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            lastPlayer = null;
            return;
        }
        if (mc.player != lastPlayer) {
            // Entrada a un mundo o servidor nuevo
            lastPlayer = mc.player;
            AltairUsers.clear();
            ackAt = -1;
            helloAt = mc.getCurrentServer() != null ? ticks + 100 : -1; // 5 s despues de entrar
        }
        if (!AltairConfig.isLinkChat() || mc.getCurrentServer() == null) {
            return;
        }
        if (ticks - lastSent < 100) {
            return; // maximo un mensaje de marca cada 5 s
        }
        if (helloAt >= 0 && ticks >= helloAt) {
            helloAt = -1;
            if (mc.getConnection().getListedOnlinePlayers().size() > 1) {
                send(mc, HELLO);
            }
        } else if (ackAt >= 0 && ticks >= ackAt) {
            ackAt = -1;
            send(mc, ACK);
        }
    }

    private static void send(Minecraft mc, String marker) {
        lastSent = ticks;
        mc.getConnection().sendChat(marker);
    }
}
