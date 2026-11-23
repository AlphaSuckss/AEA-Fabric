package dev.alphasucks.aea.port.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.item.ConjuredItems;
import net.minecraft.class_1542;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1542.class)
public abstract class ItemEntityConjuredMixin {
   @ModifyExpressionValue(method = "method_5773", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7960()Z", ordinal = 0))
   private boolean apoli$discardConjured(boolean empty) {
      if (empty) {
         return true;
      }

      class_1542 self = (class_1542)(Object)this;
      return !self.method_37908().method_8608() && ConjuredItems.isConjured(self.method_6983());
   }
}
