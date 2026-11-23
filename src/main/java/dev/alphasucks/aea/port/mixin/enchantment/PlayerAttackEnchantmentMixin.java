package dev.alphasucks.aea.port.mixin.enchantment;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelHandler;
import net.minecraft.class_1310;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1657.class)
public abstract class PlayerAttackEnchantmentMixin {
   @WrapOperation(
      method = "method_7324",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1890;method_8218(Lnet/minecraft/class_1799;Lnet/minecraft/class_1310;)F")
   )
   private float apoli$emptyHandDamageBonus(class_1799 stack, class_1310 mobType, Operation<Float> original) {
      float bonus = (Float)original.call(new Object[]{stack, mobType});
      return stack.method_7960() ? bonus + ModifyEnchantmentLevelHandler.emptyDamageBonus((class_1657)(Object)this, mobType) : bonus;
   }
}
