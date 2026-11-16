package com.altairclient.modules.mods;

import com.altairclient.client.Anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

/** Muestra la latencia (ping) al servidor en ms. */
public class PingDisplay {

    private static final PingDisplay INSTANCE = new PingDisplay();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int CYAN = 0xFF62C8FF;
    private static final int GREEN = 0xFF5DFFA8;
    private static final int YELLOW = 0xFFFFD75D;
    private static final int RED = 0xFFFF5C77;

    private boolean enabled = false;
    private int x = 95;
    private int y = 10;

    private PingDisplay() {
    }

    public static PingDisplay getInstance() {
        return INSTANCE;
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.font != null) {
            return minecraft.font.width("Ping: 000 ms") + 13;
        }
        return 75;
    }

    public int getHeight() {
        return 18;
    }

    /** Latencia actual en ms, o -1 si no hay conexión (por ejemplo, un mundo de un jugador sin datos). */
    private int currentPing(Minecraft mc) {
        if (mc.player != null && mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                return info.getLatency();
            }
        }
        return -1;
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.font == null) {
            return;
        }

        renderContent(graphics, currentPing(mc));
    }

    public void renderPreview(GuiGraphicsExtractor graphics) {
        renderContent(graphics, 42);
    }

    private final Anim.Counter pingAnim = new Anim.Counter(8.0F);
    private final Anim.Smooth widthAnim = new Anim.Smooth();

    private void renderContent(GuiGraphicsExtractor graphics, int ping) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }

        String label = "Ping: ";
        float smoothPing = ping >= 0 ? pingAnim.update(ping, 10.0F) : -1.0F;
        int shownPing = Math.round(smoothPing);
        String value = shownPing >= 0 ? shownPing + " ms" : "-- ms";
        // Color en degradado continuo: verde hasta 60 ms, amarillo hacia 120 ms y rojo desde 180 ms.
        int baseColor = smoothPing < 0 ? WHITE
                : (smoothPing < 120.0F
                        ? Anim.mix(GREEN, YELLOW, (smoothPing - 60.0F) / 60.0F)
                        : Anim.mix(YELLOW, RED, (smoothPing - 120.0F) / 60.0F));
        int valueColor = pingAnim.color(baseColor);

        int width = Math.round(widthAnim.update(mc.font.width(label + value), 20.0F));

        graphics.fill(x - 5, y - 4, x + width + 8, y + 14, BACKGROUND);
        graphics.fill(x - 5, y - 4, x - 2, y + 14, CYAN);

        graphics.text(mc.font, label, x, y, WHITE, false);
        graphics.text(mc.font, value, x + mc.font.width(label), y, valueColor, false);
    }
}
