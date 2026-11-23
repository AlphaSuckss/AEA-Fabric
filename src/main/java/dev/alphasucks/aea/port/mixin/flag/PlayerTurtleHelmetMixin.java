package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.power.builtin.WaterBreathingPower;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1657.class)
public abstract class PlayerTurtleHelmetMixin {
   @ModifyExpressionValue(method = "method_7330()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_5777(Lnet/minecraft/class_6862;)Z"))
   private boolean apoli$invertTurtleHelmetForWaterBreathers(boolean eyeInWater) {
      return eyeInWater != WaterBreathingPower.suffocatesOutsideWater((class_1657)(Object)this);
   }
}
