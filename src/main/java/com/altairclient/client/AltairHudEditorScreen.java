package com.altairclient.client;

import com.altairclient.modules.mods.ArmorHUD;
import com.altairclient.modules.mods.CPSCounter;
import com.altairclient.modules.mods.ComboCounter;
import com.altairclient.modules.mods.FPSDisplay;
import com.altairclient.modules.mods.Keystrokes;
import com.altairclient.modules.mods.PotionStatus;
import com.altairclient.modules.mods.ReachDisplay;
import com.altairclient.modules.mods.WorldInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        void render(GuiGraphicsExtractor graphics);
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
            @Override public void render(GuiGraphicsExtractor g) { FPSDisplay.getInstance().render(g); }
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
            @Override public void render(GuiGraphicsExtractor g) { CPSCounter.getInstance().render(g); }
        });

        // 3. Combo Counter (NUEVO)
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
            @Override public void render(GuiGraphicsExtractor g) { ComboCounter.getInstance().renderPreview(g); }
        });

        // 4. Reach Display (NUEVO)
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
            @Override public void render(GuiGraphicsExtractor g) { ReachDisplay.getInstance().renderPreview(g); }
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
            @Override public void render(GuiGraphicsExtractor g) { ArmorHUD.getInstance().render(g); }
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
            @Override public void render(GuiGraphicsExtractor g) { Keystrokes.getInstance().render(g); }
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
            @Override public void render(GuiGraphicsExtractor g) { PotionStatus.getInstance().render(g); }
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
            @Override public void render(GuiGraphicsExtractor g) { WorldInfo.getInstance().render(g); }
        });
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // Fondo tenue para enfocar la edición
        graphics.fill(0, 0, width, height, 0x44000000);

        // Barra superior
        graphics.fill(0, 0, width, 26, TOP_BAR_BG);
        graphics.fill(0, 26, width, 28, PURPLE);

        String title = "\u2726 A L T A I R   H U D   E D I T O R \u2726";
        int titleWidth = font.width(title);
        graphics.text(font, title, (width - titleWidth) / 2, 6, WHITE, false);

        String hint = "Arrastra cualquier módulo para posicionarlo • ESC para guardar y volver";
        int hintWidth = font.width(hint);
        graphics.text(font, hint, (width - hintWidth) / 2, 17, MUTED, false);

        // Renderizar todos los elementos para que siempre puedan ser posicionados
        for (HudTarget target : targets) {
            int tx = target.getX();
            int ty = target.getY();
            int tw = target.getWidth();
            int th = target.getHeight();

            boolean isHovered = mouseX >= tx - 6 && mouseX <= tx + tw + 6
                    && mouseY >= ty - 5 && mouseY <= ty + th + 5;
            boolean isDragged = (draggedTarget == target);
            boolean isEnabled = target.isEnabled();

            int borderColor = isDragged ? CYAN : (isHovered ? 0xFF8BE9FD : (isEnabled ? PURPLE : 0xFF434E73));

            int bx = tx - 6;
            int by = ty - 5;
            int bw = tw + 12;
            int bh = th + 10;

            graphics.fill(bx, by, bx + bw, by + 1, borderColor);
            graphics.fill(bx, by + bh - 1, bx + bw, by + bh, borderColor);
            graphics.fill(bx, by + 1, by + bh, borderColor);
            graphics.fill(bx + bw - 1, by, bx + bw, by + bh, borderColor);

            String statusTag = isEnabled ? " [ON]" : " [OFF]";
            int tagColor = isEnabled ? GREEN : MUTED;
            graphics.text(font, target.getName() + statusTag, bx + 2, by - 9, tagColor, false);

            target.render(graphics);
        }
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        double mouseX = event.x();
        double mouseY = event.y();

        for (HudTarget target : targets) {
            int tx = target.getX();
            int ty = target.getY();
            int tw = target.getWidth();
            int th = target.getHeight();

            if (mouseX >= tx - 6 && mouseX <= tx + tw + 6
                    && mouseY >= ty - 5 && mouseY <= ty + th + 5) {
                draggedTarget = target;
                dragOffsetX = (int) mouseX - tx;
                dragOffsetY = (int) mouseY - ty;

                // Si estaba apagado, encenderlo al arrastrar para facilitar su uso
                if (!target.isEnabled()) {
                    target.setEnabled(true);
                }
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if (draggedTarget != null) {
            int newX = (int) event.x() - dragOffsetX;
            int newY = (int) event.y() - dragOffsetY;

            // Restringir a los bordes de la pantalla
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
            minecraft.gui.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
