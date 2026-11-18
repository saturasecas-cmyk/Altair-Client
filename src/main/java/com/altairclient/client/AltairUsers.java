package com.altairclient.client;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;

/**
 * Registro de jugadores que llevan Altair Client. El propio jugador siempre cuenta;
 * el resto se anaden con {@link #mark(UUID)} cuando un canal de deteccion los identifica.
 */
public final class AltairUsers {

    private static final Set<UUID> KNOWN = ConcurrentHashMap.newKeySet();

    private AltairUsers() {
    }

    public static void mark(UUID id) {
        if (id != null) {
            KNOWN.add(id);
        }
    }

    public static void clear() {
        KNOWN.clear();
    }

    public static boolean isUser(UUID id) {
        if (id == null) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && id.equals(mc.player.getUUID())) {
            return true;
        }
        return KNOWN.contains(id);
    }
}
