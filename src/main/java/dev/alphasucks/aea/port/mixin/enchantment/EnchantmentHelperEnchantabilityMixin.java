package dev.alphasucks.aea.port.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantabilityPower;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_5819;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1890.class)
public abstract class EnchantmentHelperEnchantabilityMixin {
   @ModifyExpressionValue(method = "method_8230", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1792;method_7837()I"))
   private static int apoli$modifyEnchantability(int original, class_5819 random, class_1799 stack, int level, boolean treasure) {
      return ModifyEnchantabilityPower.modify(stack, original);
   }
}
