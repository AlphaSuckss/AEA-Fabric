package dev.alphasucks.aea.port.mixin.ghost;

import dev.alphasucks.aea.port.block.GhostBlocks;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2626;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3225;
import net.minecraft.class_3965;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3225.class)
public abstract class ServerPlayerGameModeGhostMixin {
   @Shadow
   @Final
   protected class_3222 field_14008;
   @Shadow
   protected class_3218 field_14007;

   @Inject(method = "method_14266", at = @At("HEAD"), cancellable = true)
   private void apoli$refuseGhostBreak(class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
      if (GhostBlocks.isGhost(this.field_14007, pos)) {
         this.field_14008.field_13987.method_14364(new class_2626(this.field_14007, pos));
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "method_14262", at = @At("HEAD"), cancellable = true)
   private void apoli$refuseGhostUse(
      class_3222 player, class_1937 level, class_1799 stack, class_1268 hand, class_3965 hitResult, CallbackInfoReturnable<class_1269> cir
   ) {
      if (GhostBlocks.isGhost(level, hitResult.method_17777())) {
         cir.setReturnValue(class_1269.field_5814);
      }
   }
}
