package io.github.apace100.originsclasses.mixin;

import net.minecraft.class_1766;
import net.minecraft.class_1799;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(class_1766.class)
public class MiningToolItemMixin {
   @Shadow
   @Final
   protected float field_7940;

   // AEA: @ModifyExpressionValue instead of @Redirect on the field read
   @ModifyExpressionValue(
      method = "method_7865(Lnet/minecraft/class_1799;Lnet/minecraft/class_2680;)F",
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_1766;field_7940:F", opcode = 180, ordinal = 0)
   )
   private float applyMiningSpeedMultiplierMultiplier(float speed, @Local(argsOnly = true) class_1799 stack) {
      return stack != null && stack.method_7985() && stack.method_7969().method_10545("MiningSpeedMultiplier")
         ? speed * stack.method_7969().method_10583("MiningSpeedMultiplier")
         : speed;
   }
}
