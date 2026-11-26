package com.altairclient.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;

/**
 * Protección contra módulos que rompen el juego: si la sesión anterior terminó en crash,
 * o si los FPS caen muy por debajo de lo jugable, se desactivan todos los módulos.
 */
public final class SafetyGuard {

    private static final String MARKER = "altairclient-session.txt";
    private static final int LOW_FPS = 8;
    private static final int LOW_FPS_TICKS = 100;       // 5 s seguidos
    private static final int GRACE_TICKS = 400;         // 20 s tras entrar a un mundo
    private static boolean registered = false;
    private static boolean startupChecked = false;
    private static String pendingNotice = null;
    private static int lowTicks = 0;
    private static int worldTicks = 0;
    private static long lastTrigger = 0L;

    private SafetyGuard() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        TickEvent.ClientTickEvent.Post.BUS.addListener(SafetyGuard::onTick);
        // Sale limpio: se marca la sesión como cerrada con normalidad.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> writeMarker("clean"), "altair-safety-marker"));
    }

    private static Path marker() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(MARKER);
    }

    private static void writeMarker(String state) {
        try {
            Path p = marker();
            Files.createDirectories(p.getParent());
            Files.writeString(p, state + " " + System.currentTimeMillis(), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException ignored) {
            // sin permisos: la protección simplemente no actúa
        }
    }

    private static boolean previousSessionCrashed() {
        try {
            Path p = marker();
            if (!Files.exists(p)) {
                return false;
            }
            String text = Files.readString(p, StandardCharsets.UTF_8).trim();
            String[] parts = text.split(" ");
            long start = parts.length > 1 ? Long.parseLong(parts[1]) : 0L;
            if (parts[0].equals("running")) {
                return true; // se cortó sin cerrar con normalidad
            }
            // Cierre "limpio" pero con un informe de crash posterior al arranque anterior
            Path crashes = Minecraft.getInstance().gameDirectory.toPath().resolve("crash-reports");
            if (Files.isDirectory(crashes)) {
                try (var list = Files.list(crashes)) {
                    return list.anyMatch(f -> {
                        try {
                            return Files.getLastModifiedTime(f).toMillis() > start && start > 0L;
                        } catch (IOException e) {
                            return false;
                        }
                    });
                }
            }
        } catch (IOException | RuntimeException ignored) {
            // marcador ilegible: se ignora
        }
        return false;
    }

    private static void disableEverything(String reason) {
        int n = AltairScreen.disableAll();
        AltairConfig.autoSaveNow();
        pendingNotice = "[Altair] " + reason + " " + n + " modules were turned off.";
    }

    private static void onTick(TickEvent.ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!AltairConfig.isLoaded()) {
            return;
        }
        if (!startupChecked) {
            startupChecked = true;
            boolean crashed = previousSessionCrashed();
            writeMarker("running");
            if (crashed) {
                disableEverything("The last session ended in a crash.");
            }
            return;
        }
        if (pendingNotice != null && mc.player != null) {
            mc.player.displayClientMessage(Component.literal(pendingNotice), false);
            pendingNotice = null;
        }
        // Vigilante de FPS: solo dentro de un mundo, con la ventana activa y sin menús
        if (mc.level == null || mc.player == null || !mc.isWindowActive() || mc.screen != null) {
            lowTicks = 0;
            worldTicks = mc.level == null ? 0 : worldTicks;
            return;
        }
        if (++worldTicks < GRACE_TICKS) {
            return;
        }
        if (mc.getFps() < LOW_FPS) {
            lowTicks++;
        } else {
            lowTicks = 0;
        }
        long now = System.currentTimeMillis();
        if (lowTicks >= LOW_FPS_TICKS && now - lastTrigger > 60_000L) {
            lastTrigger = now;
            lowTicks = 0;
            disableEverything("FPS dropped below " + LOW_FPS + " for several seconds.");
        }
    }
}
