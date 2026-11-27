package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.ComboCounter;
import com.altairclient.modules.mods.ArrowCounter;
import com.altairclient.modules.mods.DamageIndicator;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.ItemNameDisplay;
import com.altairclient.modules.mods.ClockHud;
import com.altairclient.modules.mods.DirectionHud;
import com.altairclient.modules.mods.ServerAddress;
import com.altairclient.modules.mods.MemoryDisplay;
import com.altairclient.modules.mods.TotemCounter;
import com.altairclient.modules.mods.HealthDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PingDisplay;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.ReachDisplay;
import com.altairclient.modules.mods.WorldInfo;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class AltairHudEditorScreen extends Screen {

    private static final int TOP_BAR_BG = 0xEE11172F;
    private static final int PURPLE = 0xFF9B7BFF;
    private static final int CYAN = 0xFF62C8FF;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFF929DBD;
    private static final int GREEN = 0xFF5DFFA8;

    private interface HudTarget {
        String getName();
        boolean isEnabled();
        void setEnabled(boolean enabled);
        int getX();
        void setX(int x);
        int getY();
        void setY(int y);
        int getWidth();
        int getHeight();
        void render(GuiGraphics graphics);
    }

    private static AltairHudEditorScreen model;

    private static synchronized AltairHudEditorScreen model() {
        if (model == null) {
            model = new AltairHudEditorScreen(null);
        }
        return model;
    }

    /** Positions of every HUD element, for the saved config. */
    public static com.google.gson.JsonObject exportPositions() {
        com.google.gson.JsonObject out = new com.google.gson.JsonObject();
        for (HudTarget t : model().targets) {
            com.google.gson.JsonObject o = new com.google.gson.JsonObject();
            o.addProperty("x", t.getX());
            o.addProperty("y", t.getY());
            out.add(t.getName(), o);
        }
        return out;
    }

    public static void importPositions(com.google.gson.JsonObject saved) {
        for (HudTarget t : model().targets) {
            com.google.gson.JsonElement el = saved.get(t.getName());
            if (el != null && el.isJsonObject()) {
                com.google.gson.JsonObject o = el.getAsJsonObject();
                if (o.has("x") && o.has("y")) {
                    t.setX(o.get("x").getAsInt());
                    t.setY(o.get("y").getAsInt());
                }
            }
        }
    }

    private final Screen parentScreen;
    private final List<HudTarget> targets = new ArrayList<>();

    private HudTarget draggedTarget = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public AltairHudEditorScreen(Screen parentScreen) {
        super(Component.literal("Altair HUD Editor"));
        this.parentScreen = parentScreen;

        // 1. FPS Display
        targets.add(new HudTarget() {
            @Override public String getName() { return "FPS Display"; }
            @Override public boolean isEnabled() { return FPSDisplay.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { FPSDisplay.getInstance().setEnabled(e); }
            @Override public int getX() { return FPSDisplay.getInstance().getX(); }
            @Override public void setX(int x) { FPSDisplay.getInstance().setX(x); }
            @Override public int getY() { return FPSDisplay.getInstance().getY(); }
            @Override public void setY(int y) { FPSDisplay.getInstance().setY(y); }
            @Override public int getWidth() { return FPSDisplay.getInstance().getWidth(); }
            @Override public int getHeight() { return FPSDisplay.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { FPSDisplay.getInstance().render(g); }
        });

        // 2. CPS Counter
        targets.add(new HudTarget() {
            @Override public String getName() { return "CPS Counter"; }
            @Override public boolean isEnabled() { return CPSCounter.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { CPSCounter.getInstance().setEnabled(e); }
            @Override public int getX() { return CPSCounter.getInstance().getX(); }
            @Override public void setX(int x) { CPSCounter.getInstance().setX(x); }
            @Override public int getY() { return CPSCounter.getInstance().getY(); }
            @Override public void setY(int y) { CPSCounter.getInstance().setY(y); }
            @Override public int getWidth() { return CPSCounter.getInstance().getWidth(); }
            @Override public int getHeight() { return CPSCounter.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { CPSCounter.getInstance().render(g); }
        });

        // 3. Combo Counter
        targets.add(new HudTarget() {
            @Override public String getName() { return "Combo Counter"; }
            @Override public boolean isEnabled() { return ComboCounter.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ComboCounter.getInstance().setEnabled(e); }
            @Override public int getX() { return ComboCounter.getInstance().getX(); }
            @Override public void setX(int x) { ComboCounter.getInstance().setX(x); }
            @Override public int getY() { return ComboCounter.getInstance().getY(); }
            @Override public void setY(int y) { ComboCounter.getInstance().setY(y); }
            @Override public int getWidth() { return ComboCounter.getInstance().getWidth(); }
            @Override public int getHeight() { return ComboCounter.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ComboCounter.getInstance().renderPreview(g); }
        });

        // 4. Reach Display
        targets.add(new HudTarget() {
            @Override public String getName() { return "Reach Display"; }
            @Override public boolean isEnabled() { return ReachDisplay.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ReachDisplay.getInstance().setEnabled(e); }
            @Override public int getX() { return ReachDisplay.getInstance().getX(); }
            @Override public void setX(int x) { ReachDisplay.getInstance().setX(x); }
            @Override public int getY() { return ReachDisplay.getInstance().getY(); }
            @Override public void setY(int y) { ReachDisplay.getInstance().setY(y); }
            @Override public int getWidth() { return ReachDisplay.getInstance().getWidth(); }
            @Override public int getHeight() { return ReachDisplay.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ReachDisplay.getInstance().renderPreview(g); }
        });

        // 5. Armor HUD
        targets.add(new HudTarget() {
            @Override public String getName() { return "Armor HUD"; }
            @Override public boolean isEnabled() { return ArmorHUD.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ArmorHUD.getInstance().setEnabled(e); }
            @Override public int getX() { return ArmorHUD.getInstance().getX(); }
            @Override public void setX(int x) { ArmorHUD.getInstance().setX(x); }
            @Override public int getY() { return ArmorHUD.getInstance().getY(); }
            @Override public void setY(int y) { ArmorHUD.getInstance().setY(y); }
            @Override public int getWidth() { return ArmorHUD.getInstance().getWidth(); }
            @Override public int getHeight() { return ArmorHUD.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ArmorHUD.getInstance().render(g); }
        });

        // 6. Keystrokes
        targets.add(new HudTarget() {
            @Override public String getName() { return "Keystrokes"; }
            @Override public boolean isEnabled() { return Keystrokes.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { Keystrokes.getInstance().setEnabled(e); }
            @Override public int getX() { return Keystrokes.getInstance().getX(); }
            @Override public void setX(int x) { Keystrokes.getInstance().setX(x); }
            @Override public int getY() { return Keystrokes.getInstance().getY(); }
            @Override public void setY(int y) { Keystrokes.getInstance().setY(y); }
            @Override public int getWidth() { return Keystrokes.getInstance().getWidth(); }
            @Override public int getHeight() { return Keystrokes.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { Keystrokes.getInstance().render(g); }
        });

        // 7. Potion Status
        targets.add(new HudTarget() {
            @Override public String getName() { return "Potion Status"; }
            @Override public boolean isEnabled() { return PotionStatus.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { PotionStatus.getInstance().setEnabled(e); }
            @Override public int getX() { return PotionStatus.getInstance().getX(); }
            @Override public void setX(int x) { PotionStatus.getInstance().setX(x); }
            @Override public int getY() { return PotionStatus.getInstance().getY(); }
            @Override public void setY(int y) { PotionStatus.getInstance().setY(y); }
            @Override public int getWidth() { return PotionStatus.getInstance().getWidth(); }
            @Override public int getHeight() { return PotionStatus.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { PotionStatus.getInstance().render(g); }
        });

        // 8. World Info
        targets.add(new HudTarget() {
            @Override public String getName() { return "World Info"; }
            @Override public boolean isEnabled() { return WorldInfo.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { WorldInfo.getInstance().setEnabled(e); }
            @Override public int getX() { return WorldInfo.getInstance().getX(); }
            @Override public void setX(int x) { WorldInfo.getInstance().setX(x); }
            @Override public int getY() { return WorldInfo.getInstance().getY(); }
            @Override public void setY(int y) { WorldInfo.getInstance().setY(y); }
            @Override public int getWidth() { return WorldInfo.getInstance().getWidth(); }
            @Override public int getHeight() { return WorldInfo.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { WorldInfo.getInstance().render(g); }
        });

        // Ping Display
        targets.add(new HudTarget() {
            @Override public String getName() { return "Ping Display"; }
            @Override public boolean isEnabled() { return PingDisplay.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { PingDisplay.getInstance().setEnabled(e); }
            @Override public int getX() { return PingDisplay.getInstance().getX(); }
            @Override public void setX(int x) { PingDisplay.getInstance().setX(x); }
            @Override public int getY() { return PingDisplay.getInstance().getY(); }
            @Override public void setY(int y) { PingDisplay.getInstance().setY(y); }
            @Override public int getWidth() { return PingDisplay.getInstance().getWidth(); }
            @Override public int getHeight() { return PingDisplay.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { PingDisplay.getInstance().renderPreview(g); }
        });

        // Health Display
        targets.add(new HudTarget() {
            @Override public String getName() { return "Health Display"; }
            @Override public boolean isEnabled() { return HealthDisplay.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { HealthDisplay.getInstance().setEnabled(e); }
            @Override public int getX() { return HealthDisplay.getInstance().getX(); }
            @Override public void setX(int x) { HealthDisplay.getInstance().setX(x); }
            @Override public int getY() { return HealthDisplay.getInstance().getY(); }
            @Override public void setY(int y) { HealthDisplay.getInstance().setY(y); }
            @Override public int getWidth() { return HealthDisplay.getInstance().getWidth(); }
            @Override public int getHeight() { return HealthDisplay.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { HealthDisplay.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Totem Counter"; }
            @Override public boolean isEnabled() { return TotemCounter.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { TotemCounter.getInstance().setEnabled(e); }
            @Override public int getX() { return TotemCounter.getInstance().getX(); }
            @Override public void setX(int x) { TotemCounter.getInstance().setX(x); }
            @Override public int getY() { return TotemCounter.getInstance().getY(); }
            @Override public void setY(int y) { TotemCounter.getInstance().setY(y); }
            @Override public int getWidth() { return TotemCounter.getInstance().getWidth(); }
            @Override public int getHeight() { return TotemCounter.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { TotemCounter.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Arrow Counter"; }
            @Override public boolean isEnabled() { return ArrowCounter.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ArrowCounter.getInstance().setEnabled(e); }
            @Override public int getX() { return ArrowCounter.getInstance().getX(); }
            @Override public void setX(int x) { ArrowCounter.getInstance().setX(x); }
            @Override public int getY() { return ArrowCounter.getInstance().getY(); }
            @Override public void setY(int y) { ArrowCounter.getInstance().setY(y); }
            @Override public int getWidth() { return ArrowCounter.getInstance().getWidth(); }
            @Override public int getHeight() { return ArrowCounter.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ArrowCounter.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Memory Display"; }
            @Override public boolean isEnabled() { return MemoryDisplay.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { MemoryDisplay.getInstance().setEnabled(e); }
            @Override public int getX() { return MemoryDisplay.getInstance().getX(); }
            @Override public void setX(int x) { MemoryDisplay.getInstance().setX(x); }
            @Override public int getY() { return MemoryDisplay.getInstance().getY(); }
            @Override public void setY(int y) { MemoryDisplay.getInstance().setY(y); }
            @Override public int getWidth() { return MemoryDisplay.getInstance().getWidth(); }
            @Override public int getHeight() { return MemoryDisplay.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { MemoryDisplay.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Damage Indicator"; }
            @Override public boolean isEnabled() { return DamageIndicator.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { DamageIndicator.getInstance().setEnabled(e); }
            @Override public int getX() { return DamageIndicator.getInstance().getX(); }
            @Override public void setX(int x) { DamageIndicator.getInstance().setX(x); }
            @Override public int getY() { return DamageIndicator.getInstance().getY(); }
            @Override public void setY(int y) { DamageIndicator.getInstance().setY(y); }
            @Override public int getWidth() { return DamageIndicator.getInstance().getWidth(); }
            @Override public int getHeight() { return DamageIndicator.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { DamageIndicator.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Item Name"; }
            @Override public boolean isEnabled() { return ItemNameDisplay.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ItemNameDisplay.getInstance().setEnabled(e); }
            @Override public int getX() { return ItemNameDisplay.getInstance().getX(); }
            @Override public void setX(int x) { ItemNameDisplay.getInstance().setX(x); }
            @Override public int getY() { return ItemNameDisplay.getInstance().getY(); }
            @Override public void setY(int y) { ItemNameDisplay.getInstance().setY(y); }
            @Override public int getWidth() { return ItemNameDisplay.getInstance().getWidth(); }
            @Override public int getHeight() { return ItemNameDisplay.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ItemNameDisplay.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Server Address"; }
            @Override public boolean isEnabled() { return ServerAddress.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ServerAddress.getInstance().setEnabled(e); }
            @Override public int getX() { return ServerAddress.getInstance().getX(); }
            @Override public void setX(int x) { ServerAddress.getInstance().setX(x); }
            @Override public int getY() { return ServerAddress.getInstance().getY(); }
            @Override public void setY(int y) { ServerAddress.getInstance().setY(y); }
            @Override public int getWidth() { return ServerAddress.getInstance().getWidth(); }
            @Override public int getHeight() { return ServerAddress.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ServerAddress.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Clock"; }
            @Override public boolean isEnabled() { return ClockHud.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { ClockHud.getInstance().setEnabled(e); }
            @Override public int getX() { return ClockHud.getInstance().getX(); }
            @Override public void setX(int x) { ClockHud.getInstance().setX(x); }
            @Override public int getY() { return ClockHud.getInstance().getY(); }
            @Override public void setY(int y) { ClockHud.getInstance().setY(y); }
            @Override public int getWidth() { return ClockHud.getInstance().getWidth(); }
            @Override public int getHeight() { return ClockHud.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { ClockHud.getInstance().renderPreview(g); }
        });

        targets.add(new HudTarget() {
            @Override public String getName() { return "Direction HUD"; }
            @Override public boolean isEnabled() { return DirectionHud.getInstance().isEnabled(); }
            @Override public void setEnabled(boolean e) { DirectionHud.getInstance().setEnabled(e); }
            @Override public int getX() { return DirectionHud.getInstance().getX(); }
            @Override public void setX(int x) { DirectionHud.getInstance().setX(x); }
            @Override public int getY() { return DirectionHud.getInstance().getY(); }
            @Override public void setY(int y) { DirectionHud.getInstance().setY(y); }
            @Override public int getWidth() { return DirectionHud.getInstance().getWidth(); }
            @Override public int getHeight() { return DirectionHud.getInstance().getHeight(); }
            @Override public void render(GuiGraphics g) { DirectionHud.getInstance().renderPreview(g); }
        });
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // Fondo tenue para enfocar la edición
        graphics.fill(0, 0, width, height, 0x44000000);

        // Barra superior
        graphics.fill(0, 0, width, 26, TOP_BAR_BG);
        graphics.fill(0, 26, width, 28, PURPLE);

        String title = "\u2726 A L T A I R   H U D   E D I T O R \u2726";
        int titleWidth = font.width(title);
        graphics.drawString(font, title, (width - titleWidth) / 2, 6, WHITE, false);

        String hint = "Drag the active modules to position them • ESC to save and go back";
        int hintWidth = font.width(hint);
        graphics.drawString(font, hint, (width - hintWidth) / 2, 17, MUTED, false);

        int activeCount = 0;
        for (HudTarget target : targets) {
            if (!target.isEnabled()) {
                continue;
            }
            activeCount++;
            int tx = target.getX();
            int ty = target.getY();
            int tw = target.getWidth();
            int th = target.getHeight();

            boolean isHovered = mouseX >= tx - 6 && mouseX <= tx + tw + 6
                    && mouseY >= ty - 5 && mouseY <= ty + th + 5;
            boolean isDragged = (draggedTarget == target);
            int borderColor = isDragged ? CYAN : (isHovered ? 0xFF8BE9FD : PURPLE);

            int bx = tx - 6;
            int by = ty - 5;
            int bw = tw + 12;
            int bh = th + 10;

            graphics.fill(bx, by, bx + bw, by + 1, borderColor);
            graphics.fill(bx, by + bh - 1, bx + bw, by + bh, borderColor);
            graphics.fill(bx, by + 1, bx + 1, by + bh, borderColor);
            graphics.fill(bx + bw - 1, by, bx + bw, by + bh, borderColor);

            graphics.drawString(font, target.getName(), bx + 2, by - 9, GREEN, false);

            target.render(graphics);
        }

        if (activeCount == 0) {
            String empty = "No HUD modules are active. Enable them in the modules menu.";
            graphics.drawString(font, empty, (width - font.width(empty)) / 2, height / 2 - 4, MUTED, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        boolean doubleClick = false;
        if (event.button() != 0) {
            return super.mouseClicked(event, isDouble);
        }

        double mouseX = event.x();
        double mouseY = event.y();

        // Se recorre al revés: si dos módulos se solapan, se agarra el que está dibujado encima.
        for (int i = targets.size() - 1; i >= 0; i--) {
            HudTarget target = targets.get(i);
            if (!target.isEnabled()) {
                continue;
            }
            int tx = target.getX();
            int ty = target.getY();
            int tw = target.getWidth();
            int th = target.getHeight();

            if (mouseX >= tx - 6 && mouseX <= tx + tw + 6
                    && mouseY >= ty - 5 && mouseY <= ty + th + 5) {
                draggedTarget = target;
                dragOffsetX = (int) mouseX - tx;
                dragOffsetY = (int) mouseY - ty;
                return true;
            }
        }

        return super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (draggedTarget != null) {
            int newX = (int) event.x() - dragOffsetX;
            int newY = (int) event.y() - dragOffsetY;

            newX = Math.max(5, Math.min(width - draggedTarget.getWidth() - 5, newX));
            newY = Math.max(32, Math.min(height - draggedTarget.getHeight() - 5, newY));

            draggedTarget.setX(newX);
            draggedTarget.setY(newY);
            return true;
        }

        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (draggedTarget != null) {
            draggedTarget = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
