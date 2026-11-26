package com.altairclient.compat;

public record KeyEvent(int key, int scan, int modifiers) {
    public boolean hasControlDown() {
        return (modifiers & 2) != 0 || (modifiers & 8) != 0;
    }

    public boolean hasShiftDown() {
        return (modifiers & 1) != 0;
    }

    public boolean hasAltDown() {
        return (modifiers & 4) != 0;
    }
}
