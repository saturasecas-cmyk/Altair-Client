package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.RoundedButtons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Botones de Altair: fondo redondeado oscuro y texto centrado en lugar del sprite de vanilla. */
@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin {

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$roundedButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!AltairConfig.isRoundButtons()) {
            return;
        }
        AbstractWidget w = (AbstractWidget) (Object) this;
        RoundedButtons.draw(graphics, w);
        int color = w.active ? 0xFFFFFF : 0xA0A0A0;
        graphics.drawCenteredString(Minecraft.getInstance().font, w.getMessage(),
                w.getX() + w.getWidth() / 2, w.getY() + (w.getHeight() - 8) / 2, color);
        ci.cancel();
    }
}
