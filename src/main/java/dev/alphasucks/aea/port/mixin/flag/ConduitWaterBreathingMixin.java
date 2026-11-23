package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.WaterBreathingPower;
import net.minecraft.class_1657;
import net.minecraft.class_2597;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_2597.class)
public abstract class ConduitWaterBreathingMixin {
   @ModifyExpressionValue(method = "method_11055", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_5721()Z"))
   private static boolean apoli$conduitReachesWaterBreathers(boolean original, @Local class_1657 player) {
      return original || WaterBreathingPower.suffocatesOutsideWater(player);
   }
}
