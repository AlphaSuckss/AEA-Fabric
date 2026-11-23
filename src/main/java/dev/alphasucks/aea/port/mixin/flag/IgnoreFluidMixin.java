package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.IgnoreFluidPower;
import net.minecraft.class_1297;
import net.minecraft.class_3610;
import net.minecraft.class_2338.class_2339;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1297.class)
public abstract class IgnoreFluidMixin {
   @ModifyExpressionValue(
      method = "method_5692(Lnet/minecraft/class_6862;D)Z",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3610;method_15767(Lnet/minecraft/class_6862;)Z")
   )
   private boolean apoli$ignoreFluidBlock(boolean original, @Local class_3610 fluid, @Local class_2339 pos) {
      if (!original) {
         return false;
      }

      class_1297 self = (class_1297)(Object)this;
      return !IgnoreFluidPower.mayIgnore(self) ? true : !IgnoreFluidPower.ignores(self, fluid, pos);
   }
}
