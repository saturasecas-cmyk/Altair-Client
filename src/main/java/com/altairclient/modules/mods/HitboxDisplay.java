package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;

public class HitboxDisplay {

    private static final HitboxDisplay INSTANCE = new HitboxDisplay();

    private boolean enabled = false;

    private HitboxDisplay() {
    }

    public static HitboxDisplay getInstance() {
        return INSTANCE;
    }

    public void toggle() {
        enabled = !enabled;
        triggerVanillaHitboxToggle();
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            triggerVanillaHitboxToggle();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void triggerVanillaHitboxToggle() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.keyboardHandler == null) {
            return;
        }

        try {
            // Invoca directamente el atajo de depuración oficial de Minecraft (F3 + B)
            Method m = mc.keyboardHandler.getClass().getDeclaredMethod("handleDebugKeys", int.class);
            m.setAccessible(true);
            m.invoke(mc.keyboardHandler, GLFW.GLFW_KEY_B);
        } catch (Throwable t) {
            // Búsqueda dinámica en caso de cualquier cambio de nombres internos
            for (Method method : mc.keyboardHandler.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == int.class) {
                    try {
                        method.setAccessible(true);
                        Object result = method.invoke(mc.keyboardHandler, GLFW.GLFW_KEY_B);
                        if (Boolean.TRUE.equals(result)) {
                            break;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }
}
