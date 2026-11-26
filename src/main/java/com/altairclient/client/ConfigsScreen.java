package com.altairclient.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import com.altairclient.compat.CharacterEvent;
import com.altairclient.compat.KeyEvent;
import com.altairclient.compat.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Small Windows-style window to manage saved configurations: "+" adds one (you type its name and Save),
 * every saved config has Apply and Delete. Only the X works; the other two title buttons are decoration.
 */
public class ConfigsScreen extends Screen {

    private static final int W = 230;
    private static final int TITLE_H = 16;
    private static final int ROW_H = 22;
    private static final int WIN_BG = 0xF2101428;
    private static final int TITLE_BG = 0xFF181F3B;
    private static final int BORDER = 0xFF2E3A78;
    private static final int BTN = 0xFF1B2347;
    private static final int BTN_HOT = 0xFF2A2F66;
    private static final int TEXT = 0xFFE6E9FF;
    private static final int MUTED = 0xFF8C93B8;
    private static final int GREEN = 0xFF55FF88;
    private static final int RED = 0xFFFF5555;
    private static final int MAX_NAME = 24;

    private final Screen parent;
    private int wx;
    private int wy;
    private int wh;
    private boolean dragging;
    private int dragX;
    private int dragY;
    private boolean placed;

    private boolean adding;
    private StringBuilder nameInput = new StringBuilder();
    private String status = "";
    private List<String> names;

    public ConfigsScreen(Screen parent) {
        super(Component.literal("Altair Configs"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        names = ConfigProfiles.names();
        if (!placed) {
            placed = true;
            wx = (width - W) / 2;
            wy = Math.max(4, height / 2 - 90);
        }
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        int mouseX = 0;
        int mouseY = 0;
        float partialTick = 0.0F;
        graphics.fill(0, 0, width, height, 0x40000000);
    }

    private int bodyHeight() {
        int rows = Math.max(1, names.size());
        int extra = adding ? 28 : 0;
        return 34 + extra + rows * ROW_H + 18;
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String label, boolean hot, int color) {
        g.fill(x, y, x + w, y + h, hot ? BTN_HOT : BTN);
        g.drawString(font, label, x + (w - font.width(label)) / 2, y + (h - 8) / 2, color, false);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        wh = TITLE_H + bodyHeight();
        wx = Math.max(0, Math.min(width - W, wx));
        wy = Math.max(0, Math.min(height - wh, wy));

        g.fill(wx - 1, wy - 1, wx + W + 1, wy + wh + 1, BORDER);
        g.fill(wx, wy, wx + W, wy + wh, WIN_BG);
        g.fill(wx, wy, wx + W, wy + TITLE_H, TITLE_BG);
        g.drawString(font, "⚙ Configs", wx + 6, wy + 4, TEXT, false);
        // Title buttons: minimize, maximize (decoration) and close
        g.drawString(font, "–", wx + W - 40, wy + 4, MUTED, false);
        g.drawString(font, "□", wx + W - 28, wy + 4, MUTED, false);
        boolean closeHot = in(mouseX, mouseY, wx + W - 16, wy, 16, TITLE_H);
        g.drawString(font, "✕", wx + W - 12, wy + 4, closeHot ? RED : MUTED, false);

        int y = wy + TITLE_H + 6;
        boolean addHot = in(mouseX, mouseY, wx + 8, y, 60, 16);
        button(g, wx + 8, y, 60, 16, "+ Add", addHot, GREEN);
        g.drawString(font, status, wx + 76, y + 4, MUTED, false);
        y += 22;

        if (adding) {
            g.fill(wx + 8, y, wx + W - 76, y + 16, 0xFF0C1022);
            g.fill(wx + 8, y, wx + W - 76, y + 1, BORDER);
            boolean blink = (System.currentTimeMillis() / 500) % 2 == 0;
            g.drawString(font, nameInput + (blink ? "_" : ""), wx + 12, y + 4, TEXT, false);
            button(g, wx + W - 70, y, 36, 16, "Save", in(mouseX, mouseY, wx + W - 70, y, 36, 16), GREEN);
            button(g, wx + W - 30, y, 22, 16, "✕", in(mouseX, mouseY, wx + W - 30, y, 22, 16), RED);
            y += 28;
        }

        if (names.isEmpty()) {
            g.drawString(font, "No saved configs yet. Press + Add.", wx + 8, y + 6, MUTED, false);
        }
        for (String n : names) {
            g.fill(wx + 8, y, wx + W - 8, y + ROW_H - 3, 0xFF141A36);
            g.drawString(font, n, wx + 12, y + 5, TEXT, false);
            int ax = wx + W - 8 - 50 - 44 - 4;
            button(g, ax, y + 2, 44, 14, "Apply", in(mouseX, mouseY, ax, y + 2, 44, 14), GREEN);
            int dx = wx + W - 8 - 50;
            button(g, dx, y + 2, 46, 14, "Delete", in(mouseX, mouseY, dx, y + 2, 46, 14), RED);
            y += ROW_H;
        }
    }

    @Override
    public boolean mouseClicked(double mx0, double my0, int btn0) {
        MouseButtonEvent event = new MouseButtonEvent(mx0, my0, btn0);
        boolean doubleClick = false;
        if (event.button() != 0) {
            return super.mouseClicked(event.x(), event.y(), event.button());
        }
        double mx = event.x();
        double my = event.y();

        if (in(mx, my, wx + W - 16, wy, 16, TITLE_H)) {
            onClose();
            return true;
        }
        if (in(mx, my, wx, wy, W - 44, TITLE_H)) {
            dragging = true;
            dragX = (int) mx - wx;
            dragY = (int) my - wy;
            return true;
        }

        int y = wy + TITLE_H + 6;
        if (in(mx, my, wx + 8, y, 60, 16)) {
            adding = true;
            nameInput = new StringBuilder(ConfigProfiles.suggestName());
            return true;
        }
        y += 22;

        if (adding) {
            if (in(mx, my, wx + W - 70, y, 36, 16)) {
                commit();
                return true;
            }
            if (in(mx, my, wx + W - 30, y, 22, 16)) {
                adding = false;
                return true;
            }
            y += 28;
        }

        for (String n : names) {
            int ax = wx + W - 8 - 50 - 44 - 4;
            int dx = wx + W - 8 - 50;
            if (in(mx, my, ax, y + 2, 44, 14)) {
                int changed = ConfigProfiles.apply(n);
                status = changed < 0 ? "Not found" : "Applied \"" + n + "\"";
                return true;
            }
            if (in(mx, my, dx, y + 2, 46, 14)) {
                ConfigProfiles.delete(n);
                names = ConfigProfiles.names();
                status = "Deleted";
                return true;
            }
            y += ROW_H;
        }
        return true;
    }

    private void commit() {
        String n = nameInput.toString().trim();
        if (n.isEmpty()) {
            n = ConfigProfiles.suggestName();
        }
        ConfigProfiles.save(n);
        names = ConfigProfiles.names();
        adding = false;
        status = "Saved \"" + n + "\"";
    }

    @Override
    public boolean mouseDragged(double mx0, double my0, int btn0, double dx, double dy) {
        MouseButtonEvent event = new MouseButtonEvent(mx0, my0, btn0);
        if (dragging) {
            wx = (int) event.x() - dragX;
            wy = (int) event.y() - dragY;
            return true;
        }
        return super.mouseDragged(event.x(), event.y(), event.button(), dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx0, double my0, int btn0) {
        MouseButtonEvent event = new MouseButtonEvent(mx0, my0, btn0);
        dragging = false;
        return super.mouseReleased(event.x(), event.y(), event.button());
    }

    @Override
    public boolean charTyped(char chr0, int mods0) {
        CharacterEvent event = new CharacterEvent(chr0);
        if (adding && event.isAllowedChatCharacter() && nameInput.length() < MAX_NAME) {
            nameInput.append(event.codepointAsString());
            return true;
        }
        return super.charTyped(event.chr(), 0);
    }

    @Override
    public boolean keyPressed(int key0, int scan0, int mods0) {
        KeyEvent event = new KeyEvent(key0, scan0, mods0);
        int key = event.key();
        if (key == 256) { // Esc: cancel the name box first, then close
            if (adding) {
                adding = false;
            } else {
                onClose();
            }
            return true;
        }
        if (adding) {
            if (key == 259 && nameInput.length() > 0) { // Backspace
                nameInput.setLength(nameInput.length() - 1);
                return true;
            }
            if (key == 257 || key == 335) { // Enter
                commit();
                return true;
            }
            return true;
        }
        return super.keyPressed(event.key(), event.scan(), event.modifiers());
    }
}
