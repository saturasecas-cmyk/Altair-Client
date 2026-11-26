package com.altairclient.client;

import com.altairclient.client.NotesData.Line;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import com.altairclient.compat.CharacterEvent;
import com.altairclient.compat.KeyEvent;
import com.altairclient.compat.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * App de notas estilo Word en una ventanita: negrita, cursiva, subrayado, tachado, colores,
 * listas (puntos, numeradas y tareas), coordenadas y guardado automatico.
 */
public class NotesScreen extends Screen {

    private static final int TITLE_H = 16;
    private static final int TOOL_H = 40;
    private static final int PAD = 6;
    private static final int LINE_H = 11;
    private static final int INDENT = 14;
    private static final int MIN_W = 290;
    private static final int MIN_H = 190;

    private static final int WIN_BG = 0xF2101428;
    private static final int TITLE_BG = 0xFF181F3B;
    private static final int BORDER = 0xFF2E3A78;
    private static final int PAPER = 0xFF0C1022;
    private static final int BTN = 0xFF1B2347;
    private static final int BTN_HOT = 0xFF2A2F66;
    private static final int BTN_ON = 0xFF4B3FA8;
    private static final int TEXT = 0xFFE6E9FF;
    private static final int MUTED = 0xFF8C93B8;
    private static final int CYAN = 0xFF55E6FF;
    private static final int RED = 0xFFFF5555;

    private static final int B_BOLD = 1;
    private static final int B_ITALIC = 2;
    private static final int B_UNDER = 3;
    private static final int B_STRIKE = 4;
    private static final int B_COLOR = 10; // 10..18
    private static final int B_BULLET = 30;
    private static final int B_NUMBER = 31;
    private static final int B_CHECK = 32;
    private static final int B_COORDS = 33;
    private static final int B_UNDO = 34;
    private static final int B_REDO = 35;

    private static final int DRAG_NONE = 0;
    private static final int DRAG_MOVE = 1;
    private static final int DRAG_RESIZE = 2;
    private static final int DRAG_SELECT = 3;

    private static final Map<Integer, Integer> WIDTHS = new HashMap<>();
    private static final Style[] STYLES = new Style[16];

    private final Screen parent;
    private final NotesData data = NotesData.get();

    private int cl;
    private int cc;
    private int al;
    private int ac;
    private boolean anchorSet;
    private int curStyle;

    private int wx;
    private int wy;
    private int ww;
    private int wh;

    private int dragMode = DRAG_NONE;
    private int dragOffX;
    private int dragOffY;

    private int scroll;
    private boolean ensureVisible = true;
    private boolean dirty = true;
    private boolean edited;
    private long lastSave = System.currentTimeMillis();
    private int lastTextW = -1;
    private int wantedX = -1;

    private static final class Row {
        final int line;
        final int start;
        final int end;
        final int indent;

        Row(int line, int start, int end, int indent) {
            this.line = line;
            this.start = start;
            this.end = end;
            this.indent = indent;
        }
    }

    // Deshacer / rehacer: copias del documento; escribir seguido se agrupa en un solo paso
    private static final class Snap {
        final List<Line> lines = new ArrayList<>();
        int cl;
        int cc;
    }

    private final ArrayDeque<Snap> undoStack = new ArrayDeque<>();
    private final ArrayDeque<Snap> redoStack = new ArrayDeque<>();
    private long lastKindTime;
    private int lastKind = -1;

    private Snap capture() {
        Snap snap = new Snap();
        for (Line l : data.lines) {
            Line c = new Line();
            c.text.append(l.text);
            c.styles.addAll(l.styles);
            c.list = l.list;
            c.checked = l.checked;
            snap.lines.add(c);
        }
        snap.cl = cl;
        snap.cc = cc;
        return snap;
    }

    private void restore(Snap snap) {
        data.lines.clear();
        data.lines.addAll(snap.lines);
        data.ensureLine();
        cl = Math.min(snap.cl, data.lines.size() - 1);
        cc = Math.min(snap.cc, data.lines.get(cl).length());
        anchorSet = false;
        dirty = true;
        edited = true;
        ensureVisible = true;
        lastKind = -1;
    }

    /** Guarda el estado antes de un cambio. kind: 1 = escribir, 2 = borrar, 0 = cualquier otro (siempre un paso nuevo). */
    private void checkpoint(int kind) {
        long now = System.currentTimeMillis();
        boolean merge = kind != 0 && kind == lastKind && now - lastKindTime < 1000 && selRange() == null;
        lastKind = kind;
        lastKindTime = now;
        if (merge) {
            return;
        }
        undoStack.push(capture());
        while (undoStack.size() > 100) {
            undoStack.removeLast();
        }
        redoStack.clear();
    }

    private void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        redoStack.push(capture());
        restore(undoStack.pop());
    }

    private void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        undoStack.push(capture());
        restore(redoStack.pop());
    }

    private final List<Row> rows = new ArrayList<>();
    private final List<int[]> charWidths = new ArrayList<>();

    private static final class Btn {
        final int id;
        final int x;
        final int y;
        final int w;
        final int h;

        Btn(int id, int x, int y, int w, int h) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        boolean hit(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    public NotesScreen(Screen parent) {
        super(Component.literal("Altair Notes"));
        this.parent = parent;
        data.ensureLine();
    }

    // ==================== Ciclo de vida ====================

    @Override
    protected void init() {
        super.init();
        ww = Math.max(MIN_W, Math.min(width - 4, data.winW));
        wh = Math.max(MIN_H, Math.min(height - 4, data.winH));
        wx = data.winX < 0 ? (width - ww) / 2 : data.winX;
        wy = data.winY < 0 ? (height - wh) / 2 : data.winY;
        clampWindow();
        cl = data.lines.size() - 1;
        cc = data.lines.get(cl).length();
        anchorSet = false;
        dirty = true;
        ensureVisible = true;
    }

    private void clampWindow() {
        wx = Math.max(0, Math.min(width - ww, wx));
        wy = Math.max(0, Math.min(height - wh, wy));
    }

    private void storeGeometry() {
        data.winX = wx;
        data.winY = wy;
        data.winW = ww;
        data.winH = wh;
    }

    @Override
    public void onClose() {
        saveNow();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void removed() {
        super.removed();
        saveNow();
    }

    private void saveNow() {
        storeGeometry();
        data.save();
        edited = false;
        lastSave = System.currentTimeMillis();
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
        // Sin desenfoque: solo un velo suave para que se lea la ventana
        graphics.fill(0, 0, width, height, 0x40000000);
    }

    // ==================== Estilos ====================

    private static Style styleOf(int flags) {
        int f = flags & 15;
        Style s = STYLES[f];
        if (s == null) {
            s = Style.EMPTY
                    .withBold(Boolean.valueOf((f & NotesData.BOLD) != 0))
                    .withItalic(Boolean.valueOf((f & NotesData.ITALIC) != 0))
                    .withUnderlined(Boolean.valueOf((f & NotesData.UNDERLINE) != 0))
                    .withStrikethrough(Boolean.valueOf((f & NotesData.STRIKE) != 0));
            STYLES[f] = s;
        }
        return s;
    }

    private static FormattedCharSequence styled(String text, int flags) {
        return Component.literal(text).withStyle(styleOf(flags)).getVisualOrderText();
    }

    private int charW(char ch, int style) {
        int key = (ch << 4) | (style & 15);
        Integer cached = WIDTHS.get(key);
        if (cached == null) {
            cached = font.width(styled(String.valueOf(ch), style));
            WIDTHS.put(key, cached);
        }
        return cached;
    }

    private static int colorOf(int style) {
        int idx = (style >> NotesData.COLOR_SHIFT) & 0xF;
        return NotesData.COLORS[Math.min(idx, NotesData.COLORS.length - 1)];
    }

    // ==================== Maquetacion ====================

    private int contentX() {
        return wx + PAD;
    }

    private int contentY() {
        return wy + TITLE_H + TOOL_H + 2;
    }

    private int contentW() {
        return ww - PAD * 2;
    }

    private int contentH() {
        return wh - TITLE_H - TOOL_H - 2 - PAD - 2;
    }

    private void rebuild() {
        rows.clear();
        charWidths.clear();
        int textW = contentW() - 8;
        for (int li = 0; li < data.lines.size(); li++) {
            Line line = data.lines.get(li);
            int len = line.length();
            int[] w = new int[len];
            for (int i = 0; i < len; i++) {
                w[i] = charW(line.text.charAt(i), line.styles.get(i));
            }
            charWidths.add(w);
            int indent = line.list == NotesData.LIST_NONE ? 0 : INDENT;
            int avail = Math.max(20, textW - indent);
            if (len == 0) {
                rows.add(new Row(li, 0, 0, indent));
                continue;
            }
            int start = 0;
            while (start < len) {
                int x = 0;
                int end = start;
                int lastSpace = -1;
                while (end < len) {
                    if (x + w[end] > avail && end > start) {
                        break;
                    }
                    x += w[end];
                    if (line.text.charAt(end) == ' ') {
                        lastSpace = end;
                    }
                    end++;
                }
                if (end < len && lastSpace >= start) {
                    end = lastSpace + 1;
                }
                rows.add(new Row(li, start, end, indent));
                start = end;
            }
        }
        dirty = false;
        lastTextW = textW;
    }

    private int rowOf(int line, int col) {
        int last = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            if (r.line == line) {
                if (col < r.end) {
                    return i;
                }
                last = i;
            } else if (r.line > line) {
                break;
            }
        }
        return last;
    }

    private int xOf(Row r, int col) {
        int[] w = charWidths.get(r.line);
        int x = r.indent;
        for (int i = r.start; i < col && i < w.length; i++) {
            x += w[i];
        }
        return x;
    }

    private int colAt(Row r, int px) {
        int[] w = charWidths.get(r.line);
        Line line = data.lines.get(r.line);
        int rel = px - r.indent;
        int max = r.end;
        if (r.end < line.length() && line.text.charAt(r.end - 1) == ' ') {
            max = r.end - 1;
        }
        for (int i = r.start; i < r.end; i++) {
            if (rel < w[i] / 2) {
                return Math.min(i, max);
            }
            rel -= w[i];
        }
        return max;
    }

    private int rowAtY(double my) {
        int idx = (int) Math.floor((my - contentY() + scroll) / (double) LINE_H);
        return Math.max(0, Math.min(rows.size() - 1, idx));
    }

    // ==================== Seleccion y edicion ====================

    private int[] selRange() {
        if (!anchorSet || (al == cl && ac == cc)) {
            return null;
        }
        if (al < cl || (al == cl && ac < cc)) {
            return new int[] {al, ac, cl, cc};
        }
        return new int[] {cl, cc, al, ac};
    }

    private void moveCursor(int line, int col, boolean extend) {
        if (extend) {
            if (!anchorSet) {
                al = cl;
                ac = cc;
                anchorSet = true;
            }
        } else {
            anchorSet = false;
        }
        cl = line;
        cc = col;
        Line l = data.lines.get(cl);
        if (cc > 0) {
            curStyle = l.styles.get(cc - 1);
        } else if (l.length() > 0) {
            curStyle = l.styles.get(0);
        }
        ensureVisible = true;
    }

    private boolean deleteSelection() {
        int[] s = selRange();
        if (s == null) {
            anchorSet = false;
            return false;
        }
        Line first = data.lines.get(s[0]);
        if (s[0] == s[2]) {
            first.delete(s[1], s[3]);
        } else {
            Line last = data.lines.get(s[2]);
            first.delete(s[1], first.length());
            String tail = last.text.substring(s[3]);
            List<Integer> tailStyles = new ArrayList<>(last.styles.subList(s[3], last.length()));
            for (int i = s[2]; i > s[0]; i--) {
                data.lines.remove(i);
            }
            int at = first.length();
            for (int i = 0; i < tail.length(); i++) {
                first.text.append(tail.charAt(i));
                first.styles.add(tailStyles.get(i));
            }
            if (at < 0) {
                at = 0;
            }
        }
        cl = s[0];
        cc = s[1];
        anchorSet = false;
        touch();
        return true;
    }

    private String selectedText() {
        int[] s = selRange();
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int l = s[0]; l <= s[2]; l++) {
            Line line = data.lines.get(l);
            int from = l == s[0] ? s[1] : 0;
            int to = l == s[2] ? s[3] : line.length();
            sb.append(line.text, from, to);
            if (l < s[2]) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private void insertText(String text) {
        checkpoint(text.length() == 1 && text.charAt(0) != '\n' ? 1 : 0);
        deleteSelection();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\r') {
                continue;
            }
            if (ch == '\n') {
                newLine();
            } else if (ch >= 32 && ch != 127) {
                data.lines.get(cl).insert(cc, String.valueOf(ch), curStyle);
                cc++;
            }
        }
        touch();
        ensureVisible = true;
    }

    private void newLine() {
        Line line = data.lines.get(cl);
        if (line.list != NotesData.LIST_NONE && line.length() == 0) {
            line.list = NotesData.LIST_NONE; // Enter en un punto vacio sale de la lista
            return;
        }
        Line next = new Line();
        next.list = line.list;
        String tail = line.text.substring(cc);
        List<Integer> tailStyles = new ArrayList<>(line.styles.subList(cc, line.length()));
        line.delete(cc, line.length());
        for (int i = 0; i < tail.length(); i++) {
            next.text.append(tail.charAt(i));
            next.styles.add(tailStyles.get(i));
        }
        data.lines.add(cl + 1, next);
        cl++;
        cc = 0;
    }

    private void backspace() {
        checkpoint(2);
        if (deleteSelection()) {
            return;
        }
        Line line = data.lines.get(cl);
        if (cc > 0) {
            line.delete(cc - 1, cc);
            cc--;
        } else if (line.list != NotesData.LIST_NONE) {
            line.list = NotesData.LIST_NONE;
        } else if (cl > 0) {
            Line prev = data.lines.get(cl - 1);
            int at = prev.length();
            for (int i = 0; i < line.length(); i++) {
                prev.text.append(line.text.charAt(i));
                prev.styles.add(line.styles.get(i));
            }
            data.lines.remove(cl);
            cl--;
            cc = at;
        }
        touch();
        ensureVisible = true;
    }

    private void deleteForward() {
        checkpoint(2);
        if (deleteSelection()) {
            return;
        }
        Line line = data.lines.get(cl);
        if (cc < line.length()) {
            line.delete(cc, cc + 1);
        } else if (cl + 1 < data.lines.size()) {
            Line next = data.lines.remove(cl + 1);
            for (int i = 0; i < next.length(); i++) {
                line.text.append(next.text.charAt(i));
                line.styles.add(next.styles.get(i));
            }
        }
        touch();
    }

    private void touch() {
        dirty = true;
        edited = true;
        data.ensureLine();
        if (cl >= data.lines.size()) {
            cl = data.lines.size() - 1;
        }
        cc = Math.min(cc, data.lines.get(cl).length());
    }

    private boolean selectionAll(int flag) {
        int[] s = selRange();
        if (s == null) {
            return (curStyle & flag) != 0;
        }
        boolean any = false;
        for (int l = s[0]; l <= s[2]; l++) {
            Line line = data.lines.get(l);
            int from = l == s[0] ? s[1] : 0;
            int to = l == s[2] ? s[3] : line.length();
            for (int i = from; i < to; i++) {
                any = true;
                if ((line.styles.get(i) & flag) == 0) {
                    return false;
                }
            }
        }
        return any;
    }

    private void toggleFlag(int flag) {
        int[] s = selRange();
        if (s == null) {
            curStyle ^= flag;
            return;
        }
        checkpoint(0);
        boolean remove = selectionAll(flag);
        for (int l = s[0]; l <= s[2]; l++) {
            Line line = data.lines.get(l);
            int from = l == s[0] ? s[1] : 0;
            int to = l == s[2] ? s[3] : line.length();
            for (int i = from; i < to; i++) {
                int st = line.styles.get(i);
                line.styles.set(i, remove ? (st & ~flag) : (st | flag));
            }
        }
        curStyle = remove ? (curStyle & ~flag) : (curStyle | flag);
        touch();
    }

    private void applyColor(int idx) {
        int[] s = selRange();
        curStyle = (curStyle & 0xFF) | (idx << NotesData.COLOR_SHIFT);
        if (s == null) {
            return;
        }
        checkpoint(0);
        for (int l = s[0]; l <= s[2]; l++) {
            Line line = data.lines.get(l);
            int from = l == s[0] ? s[1] : 0;
            int to = l == s[2] ? s[3] : line.length();
            for (int i = from; i < to; i++) {
                line.styles.set(i, (line.styles.get(i) & 0xFF) | (idx << NotesData.COLOR_SHIFT));
            }
        }
        touch();
    }

    private void toggleList(int type) {
        int[] s = selRange();
        int from = s == null ? cl : s[0];
        int to = s == null ? cl : s[2];
        checkpoint(0);
        boolean allSame = true;
        for (int l = from; l <= to; l++) {
            if (data.lines.get(l).list != type) {
                allSame = false;
            }
        }
        for (int l = from; l <= to; l++) {
            Line line = data.lines.get(l);
            line.list = allSame ? NotesData.LIST_NONE : type;
            line.checked = false;
        }
        touch();
    }

    private void insertCoords() {
        String text;
        if (minecraft != null && minecraft.player != null) {
            text = "X: " + minecraft.player.getBlockX() + "  Y: " + minecraft.player.getBlockY()
                    + "  Z: " + minecraft.player.getBlockZ();
        } else {
            text = "X: ?  Y: ?  Z: ?";
        }
        insertText(text);
    }

    // ==================== Barra de herramientas ====================

    private List<Btn> toolbar() {
        List<Btn> list = new ArrayList<>();
        int x = wx + PAD;
        int y = wy + TITLE_H + 3;
        for (int id = B_BOLD; id <= B_STRIKE; id++) {
            list.add(new Btn(id, x, y, 16, 15));
            x += 18;
        }
        x += 6;
        for (int i = 0; i < NotesData.COLORS.length; i++) {
            list.add(new Btn(B_COLOR + i, x, y + 1, 13, 13));
            x += 15;
        }
        x = wx + PAD;
        y += 18;
        list.add(new Btn(B_BULLET, x, y, 46, 15));
        x += 48;
        list.add(new Btn(B_NUMBER, x, y, 52, 15));
        x += 54;
        list.add(new Btn(B_CHECK, x, y, 40, 15));
        x += 46;
        list.add(new Btn(B_COORDS, x, y, 44, 15));
        x += 46;
        list.add(new Btn(B_UNDO, x, y, 34, 15));
        x += 36;
        list.add(new Btn(B_REDO, x, y, 34, 15));
        return list;
    }

    private void press(int id) {
        if (id == B_BOLD) {
            toggleFlag(NotesData.BOLD);
        } else if (id == B_ITALIC) {
            toggleFlag(NotesData.ITALIC);
        } else if (id == B_UNDER) {
            toggleFlag(NotesData.UNDERLINE);
        } else if (id == B_STRIKE) {
            toggleFlag(NotesData.STRIKE);
        } else if (id >= B_COLOR && id < B_COLOR + NotesData.COLORS.length) {
            applyColor(id - B_COLOR);
        } else if (id == B_BULLET) {
            toggleList(NotesData.LIST_BULLET);
        } else if (id == B_NUMBER) {
            toggleList(NotesData.LIST_NUMBER);
        } else if (id == B_CHECK) {
            toggleList(NotesData.LIST_CHECK);
        } else if (id == B_COORDS) {
            insertCoords();
        } else if (id == B_UNDO) {
            undo();
        } else if (id == B_REDO) {
            redo();
        }
        ensureVisible = true;
    }

    private boolean btnOn(int id) {
        if (id == B_BOLD) {
            return selectionAll(NotesData.BOLD);
        }
        if (id == B_ITALIC) {
            return selectionAll(NotesData.ITALIC);
        }
        if (id == B_UNDER) {
            return selectionAll(NotesData.UNDERLINE);
        }
        if (id == B_STRIKE) {
            return selectionAll(NotesData.STRIKE);
        }
        if (id >= B_COLOR && id < B_COLOR + NotesData.COLORS.length) {
            return ((curStyle >> NotesData.COLOR_SHIFT) & 0xF) == id - B_COLOR;
        }
        int type = id == B_BULLET ? NotesData.LIST_BULLET : id == B_NUMBER ? NotesData.LIST_NUMBER
                : id == B_CHECK ? NotesData.LIST_CHECK : -1;
        return type >= 0 && data.lines.get(cl).list == type;
    }

    // ==================== Dibujo ====================

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        clampWindow();
        if (dirty || lastTextW != contentW() - 8) {
            rebuild();
        }
        long now = System.currentTimeMillis();
        if (edited && now - lastSave > 5000) {
            saveNow();
        }

        // Ventana
        g.fill(wx - 1, wy - 1, wx + ww + 1, wy + wh + 1, BORDER);
        g.fill(wx, wy, wx + ww, wy + wh, WIN_BG);
        g.fill(wx, wy, wx + ww, wy + TITLE_H, TITLE_BG);
        g.drawString(font, "✎ Altair Notes", wx + 6, wy + 4, TEXT, false);
        boolean closeHot = mouseX >= wx + ww - 16 && mouseX < wx + ww && mouseY >= wy && mouseY < wy + TITLE_H;
        g.drawString(font, "✕", wx + ww - 12, wy + 4, closeHot ? RED : MUTED, false);

        // Barra de herramientas
        for (Btn b : toolbar()) {
            boolean hot = b.hit(mouseX, mouseY);
            boolean on = btnOn(b.id);
            if (b.id >= B_COLOR && b.id < B_COLOR + NotesData.COLORS.length) {
                int col = NotesData.COLORS[b.id - B_COLOR];
                g.fill(b.x - (on ? 1 : 0), b.y - (on ? 1 : 0), b.x + b.w + (on ? 1 : 0), b.y + b.h + (on ? 1 : 0),
                        on ? 0xFFFFFFFF : (hot ? 0xFF9AA3D6 : 0xFF2E3A78));
                g.fill(b.x + 1, b.y + 1, b.x + b.w - 1, b.y + b.h - 1, col);
                continue;
            }
            g.fill(b.x, b.y, b.x + b.w, b.y + b.h, on ? BTN_ON : (hot ? BTN_HOT : BTN));
            String label;
            int flags = 0;
            switch (b.id) {
                case B_BOLD:
                    label = "B";
                    flags = NotesData.BOLD;
                    break;
                case B_ITALIC:
                    label = "I";
                    flags = NotesData.ITALIC;
                    break;
                case B_UNDER:
                    label = "U";
                    flags = NotesData.UNDERLINE;
                    break;
                case B_STRIKE:
                    label = "S";
                    flags = NotesData.STRIKE;
                    break;
                case B_BULLET:
                    label = "• List";
                    break;
                case B_NUMBER:
                    label = "1. List";
                    break;
                case B_CHECK:
                    label = "Tasks";
                    break;
                case B_UNDO:
                    label = "Undo";
                    break;
                case B_REDO:
                    label = "Redo";
                    break;
                default:
                    label = "Coords";
                    break;
            }
            FormattedCharSequence seq = styled(label, flags);
            int tw = font.width(seq);
            g.drawString(font, seq, b.x + (b.w - tw) / 2, b.y + 4, on ? 0xFFFFFFFF : TEXT, false);
        }

        // Papel
        int cx = contentX();
        int cy = contentY();
        int cw = contentW();
        int ch = contentH();
        g.fill(cx, cy, cx + cw, cy + ch, PAPER);

        int totalH = rows.size() * LINE_H;
        int maxScroll = Math.max(0, totalH - ch + 4);
        if (ensureVisible) {
            int ry = rowOf(cl, cc) * LINE_H;
            if (ry < scroll) {
                scroll = ry;
            } else if (ry + LINE_H > scroll + ch - 2) {
                scroll = ry + LINE_H - ch + 2;
            }
            ensureVisible = false;
        }
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        g.enableScissor(cx, cy, cx + cw, cy + ch);
        int[] sel = selRange();
        boolean blink = (now / 500) % 2 == 0;
        int cursorRow = rowOf(cl, cc);
        for (int i = 0; i < rows.size(); i++) {
            int ry = cy + 2 + i * LINE_H - scroll;
            if (ry + LINE_H < cy || ry > cy + ch) {
                continue;
            }
            Row r = rows.get(i);
            Line line = data.lines.get(r.line);
            int baseX = cx + 4;

            // Seleccion
            if (sel != null && r.line >= sel[0] && r.line <= sel[2]) {
                int from = r.line == sel[0] ? Math.max(sel[1], r.start) : r.start;
                int to = r.line == sel[2] ? Math.min(sel[3], r.end) : r.end;
                boolean rowIsLast = r.end >= line.length();
                boolean pastEnd = r.line < sel[2] && rowIsLast;
                if (from < to || pastEnd) {
                    int x1 = baseX + xOf(r, Math.max(from, r.start));
                    int x2 = baseX + xOf(r, Math.max(to, r.start)) + (pastEnd ? 4 : 0);
                    if (x2 > x1) {
                        g.fill(x1, ry - 1, x2, ry + LINE_H - 1, 0x805B8CFF);
                    }
                }
            }

            // Prefijo de lista (solo en la primera fila de la linea)
            if (r.start == 0 && line.list != NotesData.LIST_NONE) {
                if (line.list == NotesData.LIST_BULLET) {
                    g.fill(baseX + 4, ry + 3, baseX + 7, ry + 6, CYAN);
                } else if (line.list == NotesData.LIST_NUMBER) {
                    int n = 1;
                    for (int k = r.line - 1; k >= 0 && data.lines.get(k).list == NotesData.LIST_NUMBER; k--) {
                        n++;
                    }
                    g.drawString(font, n + ".", baseX, ry + 1, CYAN, false);
                } else {
                    g.fill(baseX + 1, ry, baseX + 10, ry + 9, 0xFF55E6FF);
                    g.fill(baseX + 2, ry + 1, baseX + 9, ry + 8, line.checked ? 0xFF2D8C5A : PAPER);
                    if (line.checked) {
                        g.fill(baseX + 4, ry + 4, baseX + 7, ry + 6, 0xFFFFFFFF);
                    }
                }
            }

            // Texto por tramos con el mismo estilo
            int x = baseX + r.indent;
            int[] w = charWidths.get(r.line);
            int p = r.start;
            while (p < r.end) {
                int st = line.styles.get(p);
                int q = p;
                int run = 0;
                while (q < r.end && line.styles.get(q) == st) {
                    run += w[q];
                    q++;
                }
                int color = colorOf(st);
                if (line.list == NotesData.LIST_CHECK && line.checked) {
                    color = Anim.mix(color, 0xFF5A6188, 0.65F);
                }
                g.drawString(font, styled(line.text.substring(p, q), st), x, ry + 1, color, false);
                x += run;
                p = q;
            }

            // Cursor
            if (i == cursorRow && blink) {
                int px = baseX + xOf(r, Math.max(r.start, Math.min(cc, r.end)));
                g.fill(px, ry - 1, px + 1, ry + LINE_H - 1, 0xFFFFFFFF);
            }
        }
        g.disableScissor();

        // Barra de scroll
        if (maxScroll > 0) {
            int trackH = ch - 4;
            int thumbH = Math.max(12, trackH * ch / (totalH + 4));
            int thumbY = cy + 2 + (trackH - thumbH) * scroll / maxScroll;
            g.fill(cx + cw - 3, thumbY, cx + cw - 1, thumbY + thumbH, 0xFF4B5599);
        }

        // Asa para cambiar el tamano
        g.fill(wx + ww - 7, wy + wh - 3, wx + ww - 3, wy + wh - 2, MUTED);
        g.fill(wx + ww - 4, wy + wh - 6, wx + ww - 3, wy + wh - 2, MUTED);
        g.fill(wx + ww - 10, wy + wh - 3, wx + ww - 9, wy + wh - 2, MUTED);
        g.fill(wx + ww - 4, wy + wh - 9, wx + ww - 3, wy + wh - 8, MUTED);
    }

    // ==================== Entrada ====================

    @Override
    public boolean charTyped(char chr0, int mods0) {
        CharacterEvent event = new CharacterEvent(chr0);
        if (event.isAllowedChatCharacter()) {
            insertText(event.codepointAsString());
            wantedX = -1;
            return true;
        }
        return super.charTyped(event.chr(), 0);
    }

    @Override
    public boolean keyPressed(int key0, int scan0, int mods0) {
        KeyEvent event = new KeyEvent(key0, scan0, mods0);
        int key = event.key();
        boolean ctrl = event.hasControlDown();
        boolean shift = net.minecraft.client.gui.screens.Screen.hasShiftDown();

        if (key == 256) { // Esc
            onClose();
            return true;
        }
        if (ctrl) {
            if (event.isSelectAll()) {
                anchorSet = true;
                al = 0;
                ac = 0;
                cl = data.lines.size() - 1;
                cc = data.lines.get(cl).length();
                return true;
            }
            if (event.isCopy() || event.isCut()) {
                String t = selectedText();
                if (!t.isEmpty() && minecraft != null) {
                    minecraft.keyboardHandler.setClipboard(t);
                    if (event.isCut()) {
                        checkpoint(0);
                        deleteSelection();
                    }
                }
                return true;
            }
            if (event.isPaste()) {
                if (minecraft != null) {
                    insertText(minecraft.keyboardHandler.getClipboard());
                }
                return true;
            }
            if (key == 90) { // Z
                if (shift) {
                    redo();
                } else {
                    undo();
                }
                return true;
            }
            if (key == 89) { // Y
                redo();
                return true;
            }
            if (key == 66) { // B
                press(B_BOLD);
                return true;
            }
            if (key == 73) { // I
                press(B_ITALIC);
                return true;
            }
            if (key == 85) { // U
                press(B_UNDER);
                return true;
            }
        }

        Line line = data.lines.get(cl);
        switch (key) {
            case 257: // Enter
            case 335:
                insertText("\n");
                wantedX = -1;
                return true;
            case 259: // Backspace
                backspace();
                wantedX = -1;
                return true;
            case 261: // Supr
                deleteForward();
                wantedX = -1;
                return true;
            case 258: // Tab
                insertText("    ");
                return true;
            case 263: // Izquierda
                if (cc > 0) {
                    moveCursor(cl, cc - 1, shift);
                } else if (cl > 0) {
                    moveCursor(cl - 1, data.lines.get(cl - 1).length(), shift);
                }
                wantedX = -1;
                return true;
            case 262: // Derecha
                if (cc < line.length()) {
                    moveCursor(cl, cc + 1, shift);
                } else if (cl + 1 < data.lines.size()) {
                    moveCursor(cl + 1, 0, shift);
                }
                wantedX = -1;
                return true;
            case 265: // Arriba
            case 264: // Abajo
                moveVertical(key == 265 ? -1 : 1, shift);
                return true;
            case 268: // Inicio
                moveCursor(cl, rows.get(rowOf(cl, cc)).start, shift);
                wantedX = -1;
                return true;
            case 269: // Fin
                Row rr = rows.get(rowOf(cl, cc));
                int endCol = rr.end;
                if (rr.end < line.length() && line.text.charAt(rr.end - 1) == ' ') {
                    endCol = rr.end - 1;
                }
                moveCursor(cl, endCol, shift);
                wantedX = -1;
                return true;
            default:
                break;
        }
        return super.keyPressed(event.key(), event.scan(), event.modifiers());
    }

    private void moveVertical(int dir, boolean shift) {
        int idx = rowOf(cl, cc);
        Row cur = rows.get(idx);
        if (wantedX < 0) {
            wantedX = xOf(cur, cc);
        }
        int target = idx + dir;
        if (target < 0 || target >= rows.size()) {
            return;
        }
        Row t = rows.get(target);
        moveCursor(t.line, colAt(t, wantedX), shift);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        double scrollY = scrollDelta;
        if (mouseX >= wx && mouseX < wx + ww && mouseY >= wy && mouseY < wy + wh) {
            scroll -= (int) (scrollY * 22);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean mouseClicked(double mx0, double my0, int btn0) {
        MouseButtonEvent event = new MouseButtonEvent(mx0, my0, btn0);
        boolean doubleClick = false;
        double mx = event.x();
        double my = event.y();
        if (event.button() != 0) {
            return super.mouseClicked(event.x(), event.y(), event.button());
        }
        boolean inside = mx >= wx && mx < wx + ww && my >= wy && my < wy + wh;
        if (!inside) {
            return super.mouseClicked(event.x(), event.y(), event.button());
        }
        // Cerrar
        if (my < wy + TITLE_H) {
            if (mx >= wx + ww - 16) {
                onClose();
            } else {
                dragMode = DRAG_MOVE;
                dragOffX = (int) mx - wx;
                dragOffY = (int) my - wy;
            }
            return true;
        }
        // Asa de tamano
        if (mx >= wx + ww - 12 && my >= wy + wh - 12) {
            dragMode = DRAG_RESIZE;
            return true;
        }
        // Botones
        for (Btn b : toolbar()) {
            if (b.hit(mx, my)) {
                press(b.id);
                return true;
            }
        }
        // Papel
        if (mx >= contentX() && mx < contentX() + contentW() && my >= contentY() && my < contentY() + contentH()
                && !rows.isEmpty()) {
            Row r = rows.get(rowAtY(my - 2));
            Line line = data.lines.get(r.line);
            int localX = (int) mx - (contentX() + 4);
            if (line.list == NotesData.LIST_CHECK && r.start == 0 && localX >= 0 && localX < INDENT - 2) {
                checkpoint(0);
                line.checked = !line.checked;
                edited = true;
                return true;
            }
            moveCursor(r.line, colAt(r, localX), net.minecraft.client.gui.screens.Screen.hasShiftDown());
            if (!net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
                al = cl;
                ac = cc;
                anchorSet = true; // el arrastre extiende desde aqui
            }
            wantedX = -1;
            dragMode = DRAG_SELECT;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mx0, double my0, int btn0, double dx, double dy) {
        MouseButtonEvent event = new MouseButtonEvent(mx0, my0, btn0);
        if (dragMode == DRAG_MOVE) {
            wx = (int) event.x() - dragOffX;
            wy = (int) event.y() - dragOffY;
            clampWindow();
            edited = true;
            return true;
        }
        if (dragMode == DRAG_RESIZE) {
            ww = Math.max(MIN_W, Math.min(width - wx, (int) event.x() - wx));
            wh = Math.max(MIN_H, Math.min(height - wy, (int) event.y() - wy));
            dirty = true;
            edited = true;
            return true;
        }
        if (dragMode == DRAG_SELECT && !rows.isEmpty()) {
            double my = event.y();
            if (my < contentY()) {
                scroll -= 4;
            } else if (my > contentY() + contentH()) {
                scroll += 4;
            }
            Row r = rows.get(rowAtY(my - 2));
            int localX = (int) event.x() - (contentX() + 4);
            cl = r.line;
            cc = colAt(r, localX);
            Line l = data.lines.get(cl);
            if (cc > 0) {
                curStyle = l.styles.get(cc - 1);
            }
            return true;
        }
        return super.mouseDragged(event.x(), event.y(), event.button(), dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx0, double my0, int btn0) {
        MouseButtonEvent event = new MouseButtonEvent(mx0, my0, btn0);
        if (dragMode != DRAG_NONE) {
            dragMode = DRAG_NONE;
            storeGeometry();
            return true;
        }
        return super.mouseReleased(event.x(), event.y(), event.button());
    }
}
