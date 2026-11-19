package com.altairclient.mixin;

import com.altairclient.client.AltairConfig;
import com.altairclient.client.RoundedButtons;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sustituye el sprite de los botones de vanilla por un fondo redondeado oscuro de Altair. */
@Mixin(AbstractButton.class)
public abstract class AbstractButtonMixin {

    @Inject(method = "extractDefaultSprite(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$roundedSprite(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (AltairConfig.isRoundButtons()) {
            RoundedButtons.draw(graphics, (AbstractWidget) (Object) this);
            ci.cancel();
        }
    }
}
