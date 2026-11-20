package com.altairclient.modules.mods;

import com.altairclient.AltairClient;
import com.altairclient.client.Anim;
import com.altairclient.client.Palette;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraftforge.client.event.ScreenEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Dynamic Island: una píldora arriba al centro con la música que suena en tu PC (título, artista y barra de progreso).
 * Al hacer clic (con un menú abierto, p. ej. el chat) o con su tecla se expande y puedes pausar, saltar canción y ver el tiempo.
 *
 * Lee el reproductor del sistema: en Windows por los controles multimedia del sistema (PowerShell),
 * en Linux con "playerctl". Las notificaciones del PC (Discord, etc.) no se pueden leer desde un mod de Minecraft.
 */
public final class DynamicIsland {

    private static boolean enabled = false;
    private static boolean registered = false;
    private static boolean expanded = false;

    // Estado del reproductor (lo escribe el hilo lector)
    private static volatile boolean hasMedia = false;
    private static volatile String title = "";
    private static volatile String artist = "";
    private static volatile String source = "";
    private static volatile int positionSec = 0;
    private static volatile int durationSec = 0;
    private static volatile boolean playing = false;
    private static volatile long updatedAt = 0L;

    private static Thread reader = null;
    private static volatile boolean running = false;
    private static Process process = null;
    private static Path commandFile = null;

    // Animación y zonas de clic (en coordenadas de GUI, de la última vez que se dibujó)
    private static float animW = 150.0F;
    private static float animH = 18.0F;
    private static int boxX, boxY, boxW, boxH;
    private static int prevX, nextX, toggleX, buttonY;
    private static final int BUTTON_W = 26;
    private static final int BUTTON_H = 14;

    private DynamicIsland() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ScreenEvent.MouseButtonPressed.Pre.BUS.addListener(DynamicIsland::onClick);
        Runtime.getRuntime().addShutdownHook(new Thread(DynamicIsland::stop, "altair-island-stop"));
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (enabled) {
            start();
        } else {
            stop();
        }
    }

    /** Se llama al cargar la configuración guardada: arranca el lector si el módulo estaba activado. */
    public static void toggleExpanded() {
        expanded = !expanded;
    }

    public static void playPause() {
        send("toggle");
    }

    public static void next() {
        send("next");
    }

    public static void previous() {
        send("prev");
    }

    // ==================== Lector del reproductor ====================

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase().contains("linux");
    }

    private static synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        reader = new Thread(isWindows() ? DynamicIsland::readWindows : DynamicIsland::readLinux, "altair-island-reader");
        reader.setDaemon(true);
        reader.start();
    }

    private static synchronized void stop() {
        running = false;
        hasMedia = false;
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    private static void apply(String t, String a, String s, int pos, int dur, boolean isPlaying) {
        title = t;
        artist = a;
        source = s;
        positionSec = pos;
        durationSec = dur;
        playing = isPlaying;
        updatedAt = System.currentTimeMillis();
        hasMedia = !t.isBlank();
    }

    private static final String WINDOWS_SCRIPT = String.join("\n",
            "$ErrorActionPreference = 'SilentlyContinue'",
            "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8",
            "Add-Type -AssemblyName System.Runtime.WindowsRuntime",
            "$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' })[0]",
            "function Await($op, $type) { $task = $asTask.MakeGenericMethod($type).Invoke($null, @($op)); $task.Wait(-1) | Out-Null; $task.Result }",
            "$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]",
            "$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties, Windows.Media.Control, ContentType = WindowsRuntime]",
            "$mgr = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])",
            "$cmd = $args[0]",
            "$tick = 0",
            "while ($true) {",
            "  $s = $mgr.GetCurrentSession()",
            "  if (Test-Path $cmd) {",
            "    $c = (Get-Content $cmd -Raw).Trim(); Remove-Item $cmd -Force",
            "    if ($s) { switch ($c) { 'toggle' { $null = $s.TryTogglePlayPauseAsync() } 'next' { $null = $s.TrySkipNextAsync() } 'prev' { $null = $s.TrySkipPreviousAsync() } } }",
            "  }",
            "  if ($tick % 4 -eq 0) {",
            "    if ($s) {",
            "      $p = Await ($s.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])",
            "      $t = $s.GetTimelineProperties(); $i = $s.GetPlaybackInfo()",
            "      $line = @($p.Title, $p.Artist, $s.SourceAppUserModelId, [int]$t.Position.TotalSeconds, [int]($t.EndTime.TotalSeconds - $t.StartTime.TotalSeconds), [int]$i.PlaybackStatus) -join \"`t\"",
            "      [Console]::Out.WriteLine($line)",
            "    } else { [Console]::Out.WriteLine('') }",
            "    [Console]::Out.Flush()",
            "  }",
            "  $tick++",
            "  Start-Sleep -Milliseconds 250",
            "}");

    private static void readWindows() {
        try {
            Path dir = Files.createTempDirectory("altair_island");
            Path script = dir.resolve("media.ps1");
            commandFile = dir.resolve("command.txt");
            Files.writeString(script, WINDOWS_SCRIPT, StandardCharsets.UTF_8);
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive",
                    "-ExecutionPolicy", "Bypass", "-File", script.toString(), commandFile.toString());
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            process = pb.start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (running && (line = in.readLine()) != null) {
                    String[] f = line.split("\t", -1);
                    if (f.length < 6 || f[0].isBlank()) {
                        hasMedia = false;
                        continue;
                    }
                    apply(f[0], f[1], f[2], parse(f[3]), parse(f[4]), "4".equals(f[5].trim()));
                }
            }
        } catch (IOException | RuntimeException e) {
            AltairClient.LOGGER.warn("[Altair] Dynamic Island: could not read the system media player", e);
        } finally {
            hasMedia = false;
            running = false;
        }
    }

    private static void readLinux() {
        String format = "{{title}}\t{{artist}}\t{{playerName}}\t{{position}}\t{{mpris:length}}\t{{status}}";
        try {
            while (running) {
                Process p = new ProcessBuilder("playerctl", "metadata", "--format", format)
                        .redirectError(ProcessBuilder.Redirect.DISCARD).start();
                String line;
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    line = in.readLine();
                }
                p.waitFor(2, TimeUnit.SECONDS);
                if (line == null || line.isBlank()) {
                    hasMedia = false;
                } else {
                    String[] f = line.split("\t", -1);
                    if (f.length >= 6) {
                        apply(f[0], f[1], f[2], (int) (parseLong(f[3]) / 1_000_000L),
                                (int) (parseLong(f[4]) / 1_000_000L), "Playing".equalsIgnoreCase(f[5].trim()));
                    }
                }
                Thread.sleep(1000L);
            }
        } catch (IOException e) {
            AltairClient.LOGGER.warn("[Altair] Dynamic Island: 'playerctl' is not installed, cannot read the music", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            hasMedia = false;
            running = false;
        }
    }

    private static int parse(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static void send(String command) {
        if (!enabled) {
            return;
        }
        try {
            if (isWindows()) {
                if (commandFile != null) {
                    Files.writeString(commandFile, command, StandardCharsets.UTF_8);
                }
            } else if (isLinux()) {
                String arg = switch (command) {
                    case "toggle" -> "play-pause";
                    case "next" -> "next";
                    default -> "previous";
                };
                new ProcessBuilder("playerctl", arg).redirectError(ProcessBuilder.Redirect.DISCARD).start();
            }
        } catch (IOException e) {
            AltairClient.LOGGER.warn("[Altair] Dynamic Island: could not send the command", e);
        }
    }

    // ==================== Dibujo ====================

    private static int currentPosition() {
        int pos = positionSec;
        if (playing) {
            pos += (int) ((System.currentTimeMillis() - updatedAt) / 1000L);
        }
        return durationSec > 0 ? Math.min(pos, durationSec) : pos;
    }

    private static String time(int sec) {
        return (sec / 60) + ":" + String.format("%02d", sec % 60);
    }

    private static String fit(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String s = text;
        while (s.length() > 1 && font.width(s + "...") > maxWidth) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }

    public static void render(GuiGraphicsExtractor g) {
        if (!enabled || !hasMedia) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int screenW = mc.getWindow().getGuiScaledWidth();

        float targetW = expanded ? 230.0F : 150.0F;
        float targetH = expanded ? 62.0F : 18.0F;
        animW += (targetW - animW) * 0.25F;
        animH += (targetH - animH) * 0.25F;
        boxW = Math.round(animW);
        boxH = Math.round(animH);
        boxX = screenW / 2 - boxW / 2;
        boxY = 4;

        int accent = Palette.color(8);
        // Píldora con las esquinas recortadas
        int bg = 0xE6080B18;
        g.fill(boxX + 2, boxY, boxX + boxW - 2, boxY + boxH, bg);
        g.fill(boxX, boxY + 2, boxX + boxW, boxY + boxH - 2, bg);
        g.fill(boxX + 1, boxY + 1, boxX + boxW - 1, boxY + boxH - 1, bg);
        int edge = Anim.scaleAlpha(accent, 0.55F);
        g.fill(boxX + 2, boxY, boxX + boxW - 2, boxY + 1, edge);
        g.fill(boxX + 2, boxY + boxH - 1, boxX + boxW - 2, boxY + boxH, edge);
        g.fill(boxX, boxY + 2, boxX + 1, boxY + boxH - 2, edge);
        g.fill(boxX + boxW - 1, boxY + 2, boxX + boxW, boxY + boxH - 2, edge);

        int pos = currentPosition();
        float fraction = durationSec > 0 ? Math.max(0.0F, Math.min(1.0F, pos / (float) durationSec)) : 0.0F;
        String note = playing ? "♪" : "||";

        if (!expanded || boxH < 40) {
            // Compacta: nota + título, y una línea de progreso en el borde de abajo
            g.text(font, note, boxX + 8, boxY + 5, accent, true);
            String label = artist.isBlank() ? title : title + " - " + artist;
            g.text(font, fit(font, label, boxW - 30), boxX + 20, boxY + 5, 0xFFFFFFFF, true);
            if (fraction > 0.0F) {
                g.fill(boxX + 6, boxY + boxH - 2, boxX + 6 + Math.round((boxW - 12) * fraction), boxY + boxH - 1, accent);
            }
            return;
        }

        // Expandida: título, artista, barra de progreso, tiempos y botones
        int left = boxX + 10;
        int inner = boxW - 20;
        g.text(font, fit(font, title, inner), left, boxY + 7, 0xFFFFFFFF, true);
        g.text(font, fit(font, artist.isBlank() ? source : artist, inner), left, boxY + 18, 0xFF9AA3C7, true);

        int barY = boxY + 31;
        g.fill(left, barY, left + inner, barY + 3, 0xFF1A2038);
        if (fraction > 0.0F) {
            g.fill(left, barY, left + Math.round(inner * fraction), barY + 3, accent);
        }
        g.text(font, time(pos), left, barY + 5, 0xFFB8C0E0, false);
        String total = durationSec > 0 ? time(durationSec) : "--:--";
        g.text(font, total, left + inner - font.width(total), barY + 5, 0xFFB8C0E0, false);

        buttonY = boxY + boxH - BUTTON_H - 3;
        int center = boxX + boxW / 2;
        prevX = center - BUTTON_W * 3 / 2 - 6;
        toggleX = center - BUTTON_W / 2;
        nextX = center + BUTTON_W / 2 + 6;
        button(g, font, prevX, "<<", accent);
        button(g, font, toggleX, playing ? "||" : ">", accent);
        button(g, font, nextX, ">>", accent);
    }

    private static void button(GuiGraphicsExtractor g, Font font, int x, String label, int accent) {
        g.fill(x, buttonY, x + BUTTON_W, buttonY + BUTTON_H, Anim.scaleAlpha(accent, 0.22F));
        g.fill(x, buttonY, x + BUTTON_W, buttonY + 1, Anim.scaleAlpha(accent, 0.6F));
        g.text(font, label, x + (BUTTON_W - font.width(label)) / 2, buttonY + 3, 0xFFFFFFFF, true);
    }

    // ==================== Clic ====================

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Clic con un menú abierto: botones de la isla expandida o abrir/cerrar la isla. */
    private static boolean onClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!enabled || !hasMedia || Minecraft.getInstance().level == null) {
            return false;
        }
        double mx = event.getMouseX();
        double my = event.getMouseY();
        if (!inside(mx, my, boxX, boxY, boxW, boxH)) {
            return false;
        }
        if (expanded && boxH >= 40) {
            if (inside(mx, my, prevX, buttonY, BUTTON_W, BUTTON_H)) {
                previous();
                return true;
            }
            if (inside(mx, my, toggleX, buttonY, BUTTON_W, BUTTON_H)) {
                playPause();
                return true;
            }
            if (inside(mx, my, nextX, buttonY, BUTTON_W, BUTTON_H)) {
                next();
                return true;
            }
        }
        expanded = !expanded;
        return true;
    }
}
