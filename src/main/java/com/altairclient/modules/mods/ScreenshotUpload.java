package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

/**
 * Screenshot Upload: envía tu última captura a un webhook de Discord tuyo.
 *
 * La URL del webhook se guarda en el archivo "altairclient-webhook.txt" dentro de la
 * carpeta de Minecraft (una sola línea). Así las capturas van solo a tu servidor y no a
 * un alojamiento público.
 */
public final class ScreenshotUpload {

    private static final String CONFIG_FILE = "altairclient-webhook.txt";
    private static boolean uploading = false;

    private ScreenshotUpload() {
    }

    public static void upload() {
        Minecraft mc = Minecraft.getInstance();
        if (uploading) {
            say(mc, "Ya se está subiendo una captura.");
            return;
        }

        File webhookFile = new File(mc.gameDirectory, CONFIG_FILE);
        String webhook = readWebhook(webhookFile);
        if (webhook == null) {
            say(mc, "Pon la URL de tu webhook de Discord en " + CONFIG_FILE + " (carpeta de Minecraft) y vuelve a intentarlo.");
            return;
        }

        File[] shots = new File(mc.gameDirectory, "screenshots").listFiles((d, n) -> n.toLowerCase().endsWith(".png"));
        if (shots == null || shots.length == 0) {
            say(mc, "No hay capturas en la carpeta screenshots.");
            return;
        }
        File latest = Arrays.stream(shots).max(Comparator.comparingLong(File::lastModified)).get();

        uploading = true;
        say(mc, "Subiendo " + latest.getName() + "...");
        Thread t = new Thread(() -> {
            String result;
            try {
                result = send(webhook, latest);
            } catch (Exception e) {
                result = "Error al subir la captura: " + e.getMessage();
            }
            String msg = result;
            uploading = false;
            mc.execute(() -> say(mc, msg));
        }, "Altair-ScreenshotUpload");
        t.setDaemon(true);
        t.start();
    }

    private static String readWebhook(File file) {
        try {
            if (!file.isFile()) {
                return null;
            }
            String line = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            return line.startsWith("https://") ? line : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String send(String webhook, File image) throws Exception {
        String boundary = "----Altair" + UUID.randomUUID();
        byte[] data = Files.readAllBytes(Path.of(image.getPath()));
        String head = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"file\"; filename=\"" + image.getName() + "\"\r\n"
                + "Content-Type: image/png\r\n\r\n";
        String tail = "\r\n--" + boundary + "--\r\n";

        byte[] h = head.getBytes(StandardCharsets.UTF_8);
        byte[] t = tail.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[h.length + data.length + t.length];
        System.arraycopy(h, 0, body, 0, h.length);
        System.arraycopy(data, 0, body, h.length, data.length);
        System.arraycopy(t, 0, body, h.length + data.length, t.length);

        HttpRequest request = HttpRequest.newBuilder(URI.create(webhook))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        return code >= 200 && code < 300 ? "Captura subida a Discord." : "Discord rechazó la subida (código " + code + ").";
    }

    private static void say(Minecraft mc, String text) {
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal("[Altair] " + text), false);
        }
    }
}
