package com.altairclient.compat;

import net.minecraft.client.gui.screens.Screen;

public record KeyEvent(int key, int scan, int modifiers) {
    public boolean hasControlDown() {
        return Screen.hasControlDown();
    }

    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    public boolean hasAltDown() {
        return Screen.hasAltDown();
    }

    public boolean isSelectAll() {
        return Screen.isSelectAll(key);
    }

    public boolean isCopy() {
        return Screen.isCopy(key);
    }

    public boolean isCut() {
        return Screen.isCut(key);
    }

    public boolean isPaste() {
        return Screen.isPaste(key);
    }
}
