package dev.alphasucks.aea.port.mixin.item;

import dev.alphasucks.aea.port.item.ConjuredItems;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
public abstract class PlayerDropConjuredMixin {
   @Inject(method = "method_7329(Lnet/minecraft/class_1799;ZZ)Lnet/minecraft/class_1542;", at = @At("HEAD"), cancellable = true)
   private void apoli$swallowConjuredDrop(class_1799 stack, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<class_1542> cir) {
      if (ConjuredItems.isConjured(stack)) {
         cir.setReturnValue(null);
      }
   }
}
