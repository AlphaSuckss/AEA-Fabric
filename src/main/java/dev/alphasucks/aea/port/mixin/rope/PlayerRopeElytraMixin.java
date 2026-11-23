package dev.alphasucks.aea.port.mixin.rope;

import dev.alphasucks.aea.port.rope.RopeManager;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
public abstract class PlayerRopeElytraMixin {
   @Inject(method = "method_23668", at = @At("HEAD"), cancellable = true)
   private void apoli$blockRopeElytra(CallbackInfoReturnable<Boolean> cir) {
      class_1657 self = (class_1657)(Object)this;
      if (RopeManager.controllableRopeOf(self.method_5667()) != null) {
         cir.setReturnValue(false);
      }
   }
}
