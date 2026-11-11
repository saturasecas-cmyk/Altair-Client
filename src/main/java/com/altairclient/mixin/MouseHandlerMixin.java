package com.altairclient.mixin;

import com.altairclient.modules.mods.CPSCounter;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    private void altairclient$recordClick(
            long window,
            MouseButtonInfo buttonInfo,
            int action,
            CallbackInfo callbackInfo
    ) {
        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        if (buttonInfo.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            CPSCounter.getInstance().addLeftClick();
        } else if (buttonInfo.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            CPSCounter.getInstance().addRightClick();
        }
    }
}
