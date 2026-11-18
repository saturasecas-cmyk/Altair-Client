package com.altairclient.modules.mods;

/** Hide Boss Bar / Hide Scoreboard: quitan esas partes del HUD (las capas se ocultan desde AltairHud). */
public final class HudHider {

    private static boolean bossBar = false;
    private static boolean scoreboard = false;

    private HudHider() {
    }

    public static boolean isBossBarHidden() {
        return bossBar;
    }

    public static void toggleBossBar() {
        bossBar = !bossBar;
    }

    public static boolean isScoreboardHidden() {
        return scoreboard;
    }

    public static void toggleScoreboard() {
        scoreboard = !scoreboard;
    }
}
