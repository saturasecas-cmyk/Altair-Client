
package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Direction;
public class WorldInfo {

    private static final WorldInfo INSTANCE = new WorldInfo();

    private static final int BACKGROUND = 0xAA10152A;
    private static final int PURPLE = 0xFF9B7BFF;
    private static final int CYAN = 0xFF62C8FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFF929DBD;

    private boolean enabled = false;
    private int x = -1;
    private int y = -1;

    private WorldInfo() {
    }

    public static WorldInfo getInstance() {
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
        if (x < 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                return mc.getWindow().getGuiScaledWidth() - getWidth() - 10;
            }
            return 240;
        }
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        if (y < 0) {
            return 10;
        }
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return 130;
    }

    public int getHeight() {
        return 58; // 4 líneas x 12px + padding
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) {
            return;
        }

        int curX = getX();
        int curY = getY();
        int boxW = getWidth();
        int boxH = getHeight();

        // Coordenadas XYZ
        int bx = mc.player.getBlockX();
        int by = mc.player.getBlockY();
        int bz = mc.player.getBlockZ();
        String coordsText = bx + ", " + by + ", " + bz;

        // Ping
        int ping = -1;
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                ping = info.getLatency();
            }
        }
        String pingText = (ping >= 0) ? (ping + " ms") : "0 ms";

        // Hora del mundo
        long dayTime = (mc.level != null)
                ? mc.level.getDefaultClockTime() % 24000
                : 0;

        long hours = (dayTime / 1000 + 6) % 24;
        long minutes = (dayTime % 1000) * 60 / 1000;
        String timeText = String.format("%02d:%02d", hours, minutes);

        // Dirección cardinal
        Direction dir = mc.player.getDirection();
        float yaw = (mc.player.getYRot() % 360 + 360) % 360;
        String dirText = formatDirection(dir) + " (" + (int) yaw + "\u00B0)";

        // Fondo y acento cósmico (barra derecha)
        graphics.fill(curX - 4, curY - 3, curX + boxW, curY + boxH - 3, BACKGROUND);
        graphics.fill(curX + boxW - 2, curY - 3, curX + boxW, curY + boxH - 3, PURPLE);

        // Línea 1: XYZ
        graphics.text(mc.font, "XYZ:", curX + 2, curY + 2, CYAN, false);
        graphics.text(mc.font, coordsText, curX + 30, curY + 2, WHITE, false);

        // Línea 2: Dirección
        graphics.text(mc.font, "Dir:", curX + 2, curY + 15, CYAN, false);
        graphics.text(mc.font, dirText, curX + 30, curY + 15, MUTED, false);

        // Línea 3: Ping
        graphics.text(mc.font, "Ping:", curX + 2, curY + 28, CYAN, false);
        graphics.text(mc.font, pingText, curX + 30, curY + 28, WHITE, false);

        // Línea 4: Hora
        graphics.text(mc.font, "Hora:", curX + 2, curY + 41, CYAN, false);
        graphics.text(mc.font, timeText, curX + 30, curY + 41, MUTED, false);
    }

    private String formatDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> "Norte (-Z)";
            case SOUTH -> "Sur (+Z)";
            case WEST -> "Oeste (-X)";
            case EAST -> "Este (+X)";
            default -> dir.getName();
        };
    }

}
