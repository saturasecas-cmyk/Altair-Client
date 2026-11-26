package com.altairclient.mixin;

import com.altairclient.modules.mods.RenderHacks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Xray: hides the faces of every block that is not in the chosen list. */
@Mixin(Block.class)
public abstract class BlockFaceMixin {

    @Inject(method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void altair$xray5(BlockState state, BlockGetter level, BlockPos pos, Direction dir, BlockPos neighborPos,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (RenderHacks.XRAY.enabled) {
            cir.setReturnValue(RenderHacks.xrayShows(state));
        }
    }
}
