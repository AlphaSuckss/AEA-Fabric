package dev.alphasucks.aea.mixin;

import net.minecraft.class_4659;
import net.minecraft.class_4660;
import net.minecraft.class_4662;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla's cocoa and beehive tree decorators read the first trunk log without checking that the tree placed any.
 * Trees whose trunk was blocked but whose leaves were placed (e.g. Twilight Forest's lich tower planters) then crash
 * world generation with "Index (0) is greater than or equal to list size (0)". Such a tree just gets no cocoa / hive.
 */
@Mixin({class_4660.class, class_4659.class})
public abstract class TreeDecoratorNoTrunkMixin {
    @Inject(method = "method_23469", at = @At("HEAD"), cancellable = true)
    private void aea$skipWithoutTrunk(class_4662.class_7402 generator, CallbackInfo ci) {
        if (generator.method_43321().isEmpty()) {
            ci.cancel();
        }
    }
}
