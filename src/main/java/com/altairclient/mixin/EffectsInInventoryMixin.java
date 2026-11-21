package com.altairclient.mixin;

import com.altairclient.modules.textures.ExtraTextures;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Clean Inventory: sin el panel de efectos de poción al lado del inventario. */
@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryMixin {

    @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$cleanInventory(GuiGraphicsExtractor g, int mouseX, int mouseY, CallbackInfo ci) {
        if (ExtraTextures.isCleanInventory()) {
            ci.cancel();
        }
    }
}
