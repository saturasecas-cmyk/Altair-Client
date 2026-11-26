package com.altairclient.mixin;

import com.altairclient.modules.mods.NoAnimation;
import com.altairclient.modules.textures.PvpTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Short Swords / Low Shield / Minimal Totem (escala del objeto en la mano) y No Animation (sin balanceo del brazo). */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "renderItem", at = @At("HEAD"), require = 0)
    private void altair$heldItem(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                 PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            PvpTextures.transformHeldItem(stack, context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND, poseStack);
        }
    }

    @Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true, require = 0)
    private void altair$noAttackSwing(PoseStack poseStack, HumanoidArm arm, float swing, CallbackInfo ci) {
        if (NoAnimation.isEnabled()) {
            ci.cancel();
        }
    }
}
