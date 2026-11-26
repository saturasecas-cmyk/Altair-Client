package com.altairclient.modules.mods;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Chat Timestamps: añade la hora [HH:mm] delante de cada mensaje del chat. */
public final class ChatTimestamps {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static boolean enabled = false;
    private static boolean registered = false;

    private ChatTimestamps() {
    }

    public static void register() {
        if (!registered) {
            registered = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ChatTimestamps::onChat);
                }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
    }

    private static Component stamp(Component message) {
        // Evita ponerla dos veces si el mismo mensaje llega por dos eventos.
        if (message.getString().matches("^\\[\\d\\d:\\d\\d] .*")) {
            return message;
        }
        return Component.literal("[" + LocalTime.now().format(FORMAT) + "] ")
                .withStyle(ChatFormatting.GRAY)
                .append(message);
    }

    private static void onChat(ClientChatReceivedEvent event) {
        if (enabled) {
            event.setMessage(stamp(event.getMessage()));
        }
    }
}
