package dev.alphasucks.aea.port.mixin.item;

import dev.alphasucks.aea.port.item.ConjuredItems;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1661.class)
public abstract class InventoryDropLockedMixin {
   @Inject(method = "method_37417(Z)Lnet/minecraft/class_1799;", at = @At("HEAD"), cancellable = true)
   private void apoli$keepLockedConjured(boolean removeStack, CallbackInfoReturnable<class_1799> cir) {
      class_1661 self = (class_1661)(Object)this;
      if (ConjuredItems.isLocked(self.method_7391())) {
         cir.setReturnValue(class_1799.field_8037);
      }
   }
}
