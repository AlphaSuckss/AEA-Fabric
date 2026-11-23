package dev.alphasucks.aea.port.mixin.power;

import dev.alphasucks.aea.port.power.builtin.ActionOnTamePower;
import net.minecraft.class_1496;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1496.class)
public abstract class AbstractHorseTameMixin {
   @Inject(method = "method_6752", at = @At("RETURN"))
   private void apoli$actionOnTame(class_1657 player, CallbackInfoReturnable<Boolean> cir) {
      if (cir.getReturnValueZ()) {
         ActionOnTamePower.fire(player, (class_1496)(Object)this);
      }
   }
}
