package dev.alphasucks.aea.mobai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.mobai.ResolvedPath;
import net.minecraft.class_14;
import net.minecraft.class_7;
import net.minecraft.class_9;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * aea:mob_pathing on land: plan step-ups up to jump_height, drops up to drop_height, and apply the
 * walkable / blocked / avoid block rules. Everything is a no-op unless the searching mob has the power.
 */
@Mixin(class_14.class)
public abstract class LandPathNodeMakerMixin {
    private ResolvedPath aea$path() {
        return ((ResolvedPath.Holder) (Object) this).aea$resolvedPath();
    }

    /** getSuccessors: how many blocks a single step may climb (vanilla: 1, or the step height). */
    @ModifyExpressionValue(method = "method_18", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3532;method_15375(F)I"))
    private int aea$jumpSteps(int original) {
        ResolvedPath path = aea$path();
        return path != null && path.jump > original ? path.jump : original;
    }

    /** getPathNode: the largest feet-height difference a step may have (vanilla: 1.125). */
    @ModifyReturnValue(method = "method_46739", at = @At("RETURN"))
    private double aea$maxStepUp(double original) {
        ResolvedPath path = aea$path();
        return path != null && path.jump > 0 ? Math.max(original, path.jump + 0.125) : original;
    }

    /** getPathNode: how far the mob may drop (vanilla: the mob's safe fall distance). */
    @ModifyExpressionValue(method = "method_62", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1308;method_5850()I"))
    private int aea$dropHeight(int original) {
        ResolvedPath path = aea$path();
        return path != null && path.drop >= 0 ? path.drop : original;
    }

    /** getDefaultNodeType (per block of the mob's footprint): "blocked" blocks are walls, also for wide mobs. */
    @ModifyReturnValue(method = "method_17", at = @At("RETURN"))
    private class_7 aea$blockedCells(class_7 original, @Local(argsOnly = true, ordinal = 0) int x, @Local(argsOnly = true, ordinal = 1) int y,
                                     @Local(argsOnly = true, ordinal = 2) int z) {
        ResolvedPath path = aea$path();
        if (path == null || path.config.blocked().isEmpty() || original == class_7.field_22) {
            return original;
        }
        return path.isBlocked(x, y, z) ? class_7.field_22 : original;
    }

    /**
     * getPathNode: nodes that break a rule are dropped (null = not a successor — marking them blocked would let the
     * search cross them as negative "fall" nodes); avoided blocks raise the node's cost (max, so revisits don't stack).
     */
    @ModifyReturnValue(method = "method_62", at = @At("RETURN"))
    private class_9 aea$blockRules(class_9 node) {
        if (node == null || node.field_43 < 0.0F) {
            return node;
        }
        ResolvedPath path = aea$path();
        if (path == null || !path.hasBlockRules()) {
            return node;
        }
        int x = node.field_40;
        int y = node.field_39;
        int z = node.field_38;
        if (path.isBlocked(x, y, z)) {
            return null;
        }
        class_7 type = node.field_41;
        boolean standing = type != class_7.field_7 && type != class_7.field_18 && type != class_7.field_14;
        if (standing && !path.isWalkableFloor(x, y, z)) {
            return null;
        }
        if (!path.config.avoid().isEmpty()) {
            float penalty = path.avoidPenalty(x, y, z);
            if (penalty < 0.0F) {
                return null;
            }
            if (penalty > node.field_43) {
                node.field_43 = penalty;
            }
        }
        return node;
    }
}
