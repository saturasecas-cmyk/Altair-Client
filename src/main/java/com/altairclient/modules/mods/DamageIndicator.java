package com.altairclient.modules.mods;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Damage Indicator: cada vez que le quitas vida a una entidad, el daño sale en números
 * que flotan hacia arriba y se desvanecen, al estilo Fortnite. La posición de salida
 * (cerca de la mira por defecto) se mueve en el editor del HUD.
 *
 * Solo lee la vida de las entidades que ves; no envía nada al servidor.
 */
public class DamageIndicator extends HudElement {

    private static final DamageIndicator INSTANCE = new DamageIndicator();

    private static final long LIFETIME_MS = 1100L;
    private static final int RISE_PX = 28;
    /** Ticks desde el último clic de ataque en los que un descenso de vida cuenta como tuyo. */
    private static final int ATTACK_WINDOW_TICKS = 10;

    private record Pop(String text, long born, int dx, boolean big) {
    }

    private final List<Pop> pops = new ArrayList<>();
    private final Map<Integer, Float> lastHealth = new HashMap<>();
    private final Random random = new Random();
    private int ticksSinceAttack = 1000;

    private DamageIndicator() {
        super(0, 0); // se recoloca junto a la mira la primera vez
    }

    public static DamageIndicator getInstance() {
        return INSTANCE;
    }

    @Override
    public int getWidth() {
        return 50;
    }

    @Override
    public int getHeight() {
        return 30;
    }

    private void placeDefault(Minecraft mc) {
        if (getX() == 0 && getY() == 0 && mc.getWindow() != null) {
            setX(mc.getWindow().getGuiScaledWidth() / 2 + 20);
            setY(mc.getWindow().getGuiScaledHeight() / 2 - 30);
        }
    }

    /** Se llama una vez por tick del cliente. */
    public void tick(Minecraft mc) {
        if (!isEnabled() || mc.player == null || mc.level == null) {
            lastHealth.clear();
            return;
        }

        ticksSinceAttack = mc.options.keyAttack.isDown() ? 0 : ticksSinceAttack + 1;

        Map<Integer, Float> current = new HashMap<>();
        List<Entity> near = mc.level.getEntities(mc.player, mc.player.getBoundingBox().inflate(8.0D), e -> e instanceof LivingEntity);
        for (Entity e : near) {
            LivingEntity living = (LivingEntity) e;
            float now = living.getHealth();
            Float before = lastHealth.get(living.getId());
            if (before != null && now < before && ticksSinceAttack <= ATTACK_WINDOW_TICKS) {
                spawn(before - now);
            }
            current.put(living.getId(), now);
        }
        lastHealth.clear();
        lastHealth.putAll(current);
    }

    private void spawn(float damage) {
        String text = damage == Math.floor(damage)
                ? String.valueOf((int) damage)
                : String.format(Locale.US, "%.1f", damage);
        pops.add(new Pop(text, System.currentTimeMillis(), random.nextInt(31) - 15, damage >= 6.0F));
        if (pops.size() > 20) {
            pops.remove(0);
        }
    }

    @Override
    public void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            placeDefault(mc);
        }
        super.render(graphics);
    }

    @Override
    public void renderPreview(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            placeDefault(mc);
        }
        super.renderPreview(graphics);
    }

    @Override
    protected void draw(GuiGraphics graphics, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        int x = getX();
        int y = getY();

        if (preview) {
            graphics.fill(x - 5, y - 4, x + getWidth(), y + getHeight(), BACKGROUND);
            drawPop(graphics, mc, "7", x + 6, y + 14, 0.2F, true);
            drawPop(graphics, mc, "4", x + 28, y + 4, 0.6F, false);
            return;
        }

        long now = System.currentTimeMillis();
        pops.removeIf(p -> now - p.born() > LIFETIME_MS);
        for (Pop p : pops) {
            float t = (now - p.born()) / (float) LIFETIME_MS;
            drawPop(graphics, mc, p.text(), x + p.dx(), y + 8 - Math.round(t * RISE_PX), t, p.big());
        }
    }

    private void drawPop(GuiGraphics graphics, Minecraft mc, String text, int x, int y, float t, boolean big) {
        int alpha = (int) (255 * Math.max(0.05F, 1.0F - t * t));
        int rgb = big ? 0xFF5C3A : 0xFFE14A;
        Component component = Component.literal(text).withStyle(ChatFormatting.BOLD);
        graphics.drawString(mc.font, component, x, y, (alpha << 24) | rgb, true);
    }
}
