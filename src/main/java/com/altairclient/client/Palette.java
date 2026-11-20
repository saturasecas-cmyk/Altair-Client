package com.altairclient.client;

/** Paleta de colores de Altair y colores animados (ciclo de la paleta y arcoíris). */
public final class Palette {

    public static final String[] NAMES = {
            "Purple", "Cyan", "Green", "Yellow", "Red", "Gold", "Pink", "White", "Altair Cycle", "Rainbow"
    };

    private static final int[] COLORS = {
            0xFF9B7BFF, 0xFF62C8FF, 0xFF5DFFA8, 0xFFFFD75D, 0xFFFF5C77, 0xFFFFC857, 0xFFFF7AD9, 0xFFFFFFFF
    };

    /** Colores por los que pasa "Ciclo Altair". */
    private static final int[] CYCLE = {0xFF9B7BFF, 0xFF62C8FF, 0xFF5DFFA8, 0xFFFF7AD9};

    public static final int MAX_INDEX = NAMES.length - 1;

    private Palette() {
    }

    public static int color(int index) {
        if (index >= 0 && index < COLORS.length) {
            return COLORS[index];
        }
        float t = (System.currentTimeMillis() % 100_000L) / 1000.0F;
        if (index == COLORS.length) {
            float pos = (t / 1.2F) % CYCLE.length;
            int i = (int) pos;
            return Anim.mix(CYCLE[i], CYCLE[(i + 1) % CYCLE.length], pos - i);
        }
        return hsv((t / 6.0F) % 1.0F);
    }

    private static int hsv(float h) {
        float r = Math.abs(h * 6.0F - 3.0F) - 1.0F;
        float g = 2.0F - Math.abs(h * 6.0F - 2.0F);
        float b = 2.0F - Math.abs(h * 6.0F - 4.0F);
        int ri = Math.round(Anim.clamp(r, 0.0F, 1.0F) * 255.0F);
        int gi = Math.round(Anim.clamp(g, 0.0F, 1.0F) * 255.0F);
        int bi = Math.round(Anim.clamp(b, 0.0F, 1.0F) * 255.0F);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }
}
