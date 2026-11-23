package dev.alphasucks.aea.port.mixin.enchantment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.ModifyEnchantmentLevelHandler;
import net.minecraft.class_1799;
import net.minecraft.class_2073;
import net.minecraft.class_2499;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_2073.class)
public abstract class ItemPredicateModifyLevelMixin {
   @ModifyExpressionValue(
      method = "method_8970(Lnet/minecraft/class_1799;)Z",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7921()Lnet/minecraft/class_2499;")
   )
   private class_2499 apoli$modifyMatchedEnchantments(class_2499 original, @Local(argsOnly = true) class_1799 stack) {
      return ModifyEnchantmentLevelHandler.enchantmentTags(stack, original);
   }
}
