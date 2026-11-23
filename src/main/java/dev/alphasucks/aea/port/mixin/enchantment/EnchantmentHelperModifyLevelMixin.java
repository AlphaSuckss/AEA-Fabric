package dev.alphasucks.aea.port.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelHandler;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_2499;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1890.class)
public abstract class EnchantmentHelperModifyLevelMixin {
   @ModifyReturnValue(method = "method_8225(Lnet/minecraft/class_1887;Lnet/minecraft/class_1799;)I", at = @At("RETURN"))
   private static int apoli$modifyItemLevel(int original, class_1887 enchantment, class_1799 stack) {
      return ModifyEnchantmentLevelHandler.level(stack, enchantment, original);
   }

   @ModifyReturnValue(method = "method_8203(Lnet/minecraft/class_1887;Lnet/minecraft/class_1309;)I", at = @At("RETURN"))
   private static int apoli$modifyEmptySlotLevel(int original, class_1887 enchantment, class_1309 living) {
      return ModifyEnchantmentLevelHandler.emptySlotLevel(living, enchantment, original);
   }

   @ModifyExpressionValue(
      method = "method_8220(Lnet/minecraft/class_1890$class_1891;Lnet/minecraft/class_1799;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7921()Lnet/minecraft/class_2499;")
   )
   private static class_2499 apoli$modifyIteratedEnchantments(class_2499 original, @Local(argsOnly = true) class_1799 stack) {
      return ModifyEnchantmentLevelHandler.enchantmentTags(stack, original);
   }
}
