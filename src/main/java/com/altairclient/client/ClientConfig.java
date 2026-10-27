
package com.altairclient.client;

public final class ClientConfig {

    private ClientConfig() {
        // Evita crear instancias de esta clase.
    }

    // Preferencias generales
    private static boolean menuEnabled = true;
    private static boolean animationsEnabled = true;
    private static boolean showModuleDescriptions = true;

    // Tema visual de Altair
    private static int primaryColor = 0xFF9B7BFF;
    private static int accentColor = 0xFF62C8FF;
    private static int backgroundColor = 0xF20A0D20;
    private static int panelColor = 0xFF11172F;

    // Tecla predeterminada: Right Shift.
    // El código de Minecraft deberá registrar la tecla por separado.
    private static final String MENU_KEY_NAME = "RIGHT_SHIFT";

    public static boolean isMenuEnabled() {
        return menuEnabled;
    }

    public static void setMenuEnabled(boolean enabled) {
        menuEnabled = enabled;
    }

    public static boolean areAnimationsEnabled() {
        return animationsEnabled;
    }

    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    public static boolean areModuleDescriptionsEnabled() {
        return showModuleDescriptions;
    }

    public static void setModuleDescriptionsEnabled(boolean enabled) {
        showModuleDescriptions = enabled;
    }

    public static int getPrimaryColor() {
        return primaryColor;
    }

    public static void setPrimaryColor(int color) {
        primaryColor = color;
    }

    public static int getAccentColor() {
        return accentColor;
    }

    public static void setAccentColor(int color) {
        accentColor = color;
    }

    public static int getBackgroundColor() {
        return backgroundColor;
    }

    public static void setBackgroundColor(int color) {
        backgroundColor = color;
    }

    public static int getPanelColor() {
        return panelColor;
    }

    public static void setPanelColor(int color) {
        panelColor = color;
    }

    public static String getMenuKeyName() {
        return MENU_KEY_NAME;
    }

    /**
     * Restaura los valores predeterminados de la sesión.
     */
    public static void reset() {
        menuEnabled = true;
        animationsEnabled = true;
        showModuleDescriptions = true;

        primaryColor = 0xFF9B7BFF;
        accentColor = 0xFF62C8FF;
        backgroundColor = 0xF20A0D20;
        panelColor = 0xFF11172F;
    }
}
