package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.IgnoreFluidPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_746.class)
@Environment(EnvType.CLIENT)
public abstract class LocalPlayerIgnoreFluidMixin {
   @ModifyReturnValue(method = "method_5869", at = @At("RETURN"))
   private boolean apoli$ignoredWaterIsNotUnderwater(boolean original) {
      if (!original) {
         return false;
      }

      class_746 self = (class_746)(Object)this;
      return self.method_5799() || !IgnoreFluidPower.holds(self);
   }
}
