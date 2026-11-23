package dev.alphasucks.aea.port.mixin.damage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.data.CriticalHitContext;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1657.class)
public abstract class PlayerCriticalHitMixin {
   @WrapOperation(
      method = "method_7324(Lnet/minecraft/class_1297;)V",
      at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/class_1297;method_5643(Lnet/minecraft/class_1282;F)Z")
   )
   private boolean apoli$markCriticalHit(class_1297 target, class_1282 source, float amount, Operation<Boolean> original, @Local(ordinal = 2) boolean critical) {
      class_1657 self = (class_1657)(Object)this;
      long previous = CriticalHitContext.set(self.method_5628(), critical);

      try {
         return (Boolean)original.call(new Object[]{target, source, amount});
      } finally {
         CriticalHitContext.restore(previous);
      }
   }
}
