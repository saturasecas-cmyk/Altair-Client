package com.altairclient.modules.mods;

import com.altairclient.client.Anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Locale;

/**
 * Muestra el número de corazones de la entidad o jugador al que miras
 * (y los tuyos cuando no apuntas a nadie). 1 corazón = 2 puntos de vida.
 */
public class HealthDisplay {

    private static final HealthDisplay INSTANCE = new HealthDisplay();

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BACKGROUND = 0xAA10152A;
    private static final int RED = 0xFFFF5C77;
    private static final int MUTED = 0xFF929DBD;

    /** Tiempo que se sigue mostrando el objetivo después de dejar de mirarlo. */
    private static final long TARGET_HOLD_MS = 1500L;

    private boolean enabled = false;
    private int x = 200;
    private int y = 10;

    private LivingEntity lastTarget = null;
    private long lastTargetTime = 0L;

    private HealthDisplay() {
    }

    public static HealthDisplay getInstance() {
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
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.font != null) {
            return mc.font.width("Player: ♥ 10.0 / 10.0") + 13;
        }
        return 130;
    }

    public int getHeight() {
        return 18;
    }

    private static String formatHearts(float points) {
        return String.format(Locale.US, "%.1f", points / 2.0F);
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.font == null) {
            return;
        }

        // Entidad apuntada con la mira
        HitResult hit = mc.hitResult;
        long now = System.currentTimeMillis();
        if (hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
            lastTarget = living;
            lastTargetTime = now;
        }

        LivingEntity shown;
        if (lastTarget != null && lastTarget.isAlive() && now - lastTargetTime < TARGET_HOLD_MS) {
            shown = lastTarget;
        } else {
            shown = mc.player;
        }

        Entity entity = shown;
        String name = entity == mc.player ? "You" : entity.getName().getString();
        renderContent(graphics, name, shown.getHealth(), shown.getMaxHealth());
    }

    public void renderPreview(GuiGraphicsExtractor graphics) {
        renderContent(graphics, "Player", 15.0F, 20.0F);
    }

    private final Anim.Counter healthAnim = new Anim.Counter(0.5F);
    private final Anim.Smooth widthAnim = new Anim.Smooth();

    private void renderContent(GuiGraphicsExtractor graphics, String name, float health, float maxHealth) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return;
        }

        String label = name + ": ";
        String heart = "♥ ";
        // La vida sube o baja contando, y destella en rojo al recibir daño o en verde al curarse.
        float shownHealth = healthAnim.update(health, 9.0F);
        String value = formatHearts(shownHealth) + " / " + formatHearts(maxHealth);

        int width = Math.round(widthAnim.update(mc.font.width(label + heart + value), 20.0F));

        graphics.fill(x - 5, y - 4, x + width + 8, y + 14, BACKGROUND);
        graphics.fill(x - 5, y - 4, x - 2, y + 14, RED);

        int cx = x;
        graphics.text(mc.font, label, cx, y, MUTED, false);
        cx += mc.font.width(label);
        graphics.text(mc.font, heart, cx, y, RED, false);
        cx += mc.font.width(heart);
        graphics.text(mc.font, value, cx, y, healthAnim.color(WHITE), false);
    }
}
