package com.altairclient.modules.mods;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Auto Tool: al picar un bloque, selecciona de tu barra rápida la herramienta más rápida
 * para ese bloque, y al terminar vuelve al hueco que tenías. Solo cambia el hueco
 * seleccionado, igual que si pulsaras el número tú.
 */
public final class AutoTool {

    private static boolean enabled = false;
    private static int previousSlot = -1;

    private AutoTool() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void toggle() {
        enabled = !enabled;
        if (!enabled) {
            restore(Minecraft.getInstance());
        }
    }

    public static void tick(Minecraft mc) {
        if (!enabled || mc.player == null || mc.level == null || mc.screen != null) {
            restore(mc);
            return;
        }

        Inventory inv = mc.player.getInventory();

        if (mc.options.keyAttack.isDown()
                && mc.hitResult instanceof BlockHitResult hit
                && hit.getType() == HitResult.Type.BLOCK) {
            BlockState state = mc.level.getBlockState(hit.getBlockPos());
            if (state.isAir()) {
                return;
            }

            int current = inv.selected;
            int best = current;
            float bestSpeed = inv.getItem(current).getDestroySpeed(state);
            for (int i = 0; i < 9; i++) {
                float speed = inv.getItem(i).getDestroySpeed(state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    best = i;
                }
            }

            if (best != current) {
                if (previousSlot < 0) {
                    previousSlot = current;
                }
                inv.selected = best;
            }
        } else {
            restore(mc);
        }
    }

    private static void restore(Minecraft mc) {
        if (previousSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selected = previousSlot;
        }
        previousSlot = -1;
    }
}
