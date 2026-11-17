package com.altairclient.mixin;

import com.altairclient.modules.textures.PvpTextures;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Minimal Enchantment Glint: los objetos encantados se dibujan sin brillo. */
@Mixin(ItemStackRenderState.LayerRenderState.class)
public abstract class ItemLayerRenderStateMixin {

    @ModifyVariable(method = "setFoilType", at = @At("HEAD"), argsOnly = true, require = 0)
    private ItemStackRenderState.FoilType altair$noGlint(ItemStackRenderState.FoilType type) {
        return PvpTextures.minimalGlint() ? ItemStackRenderState.FoilType.NONE : type;
    }
}
