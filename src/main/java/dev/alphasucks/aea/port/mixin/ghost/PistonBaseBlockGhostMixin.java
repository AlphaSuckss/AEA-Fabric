package dev.alphasucks.aea.port.mixin.ghost;

import dev.alphasucks.aea.port.block.GhostBlocks;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2665;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_2665.class)
public abstract class PistonBaseBlockGhostMixin {
   @Inject(method = "method_11484", at = @At("HEAD"), cancellable = true)
   private static void apoli$ghostBlocksAreImmovable(
      class_2680 state,
      class_1937 level,
      class_2338 pos,
      class_2350 moveDirection,
      boolean allowDestroy,
      class_2350 pistonFacing,
      CallbackInfoReturnable<Boolean> cir
   ) {
      if (GhostBlocks.isGhost(level, pos)) {
         cir.setReturnValue(false);
      }
   }
}
