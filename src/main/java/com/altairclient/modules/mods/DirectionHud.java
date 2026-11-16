package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

/** Direction HUD: brújula con las direcciones cardinales y los grados hacia donde miras. */
public class DirectionHud extends HudElement {

    private static final DirectionHud INSTANCE = new DirectionHud();

    private static final int WIDTH = 150;
    private static final String[] LABELS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    private DirectionHud() {
        super(100, 40);
    }

    public static DirectionHud getInstance() {
        return INSTANCE;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return 28;
    }

    @Override
    protected void draw(GuiGraphicsExtractor graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        float bearing;
        if (preview || mc.player == null) {
            bearing = (System.currentTimeMillis() / 40L) % 360L;
        } else {
            // En Minecraft yaw 0 mira al sur; el rumbo de la brújula es 0 = norte.
            bearing = Mth.wrapDegrees(mc.player.getYRot() + 180.0F);
            if (bearing < 0.0F) {
                bearing += 360.0F;
            }
        }

        int x = getX();
        int y = getY();
        int cx = x + WIDTH / 2;
        box(graphics, WIDTH, getHeight(), CYAN);

        for (int i = 0; i < LABELS.length; i++) {
            float delta = Mth.wrapDegrees(i * 45.0F - bearing);
            if (Math.abs(delta) > 70.0F) {
                continue;
            }
            float fade = 1.0F - Math.abs(delta) / 70.0F;
            int alpha = Math.round(70.0F + 185.0F * fade);
            int rgb = LABELS[i].equals("N") ? 0xFF5C77 : (LABELS[i].length() == 1 ? 0xFFFFFF : 0x929DBD);
            String label = LABELS[i];
            int lx = cx + Math.round(delta) - mc.font.width(label) / 2;
            graphics.text(mc.font, label, lx, y, (alpha << 24) | rgb, false);
        }

        // Marcas pequeñas cada 15 grados y marcador central
        for (int d = -60; d <= 60; d += 15) {
            float shifted = Mth.wrapDegrees(bearing + d);
            float off = shifted - bearing;
            int tx = cx + Math.round(Mth.wrapDegrees(Math.round(bearing / 15.0F) * 15.0F + d - bearing));
            if (off != 0 || d != 0) {
                graphics.fill(tx, y + 11, tx + 1, y + 13, 0x66929DBD);
            }
        }
        graphics.fill(cx, y + 9, cx + 1, y + 15, CYAN);

        String deg = Math.round(bearing) % 360 + "\u00B0";
        graphics.text(mc.font, deg, cx - mc.font.width(deg) / 2, y + 16, GOLD, false);
    }
}
