package dev.alphasucks.aea.port.mixin.item;

import dev.alphasucks.aea.port.item.ConjuredItems;
import net.minecraft.class_1263;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1735.class)
public abstract class SlotConjuredMixin {
   @Shadow
   @Final
   public class_1263 field_7871;

   @Inject(method = "method_7674", at = @At("HEAD"), cancellable = true)
   private void apoli$lockConjured(class_1657 player, CallbackInfoReturnable<Boolean> cir) {
      if (ConjuredItems.isLocked(((class_1735)(Object)this).method_7677())) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "method_7680", at = @At("HEAD"), cancellable = true)
   private void apoli$refuseConjuredOutsideInventory(class_1799 stack, CallbackInfoReturnable<Boolean> cir) {
      if (!(this.field_7871 instanceof class_1661) && ConjuredItems.isConjured(stack)) {
         cir.setReturnValue(false);
      }
   }

   @ModifyVariable(method = "method_7673", at = @At("HEAD"), argsOnly = true)
   private class_1799 apoli$deleteConjuredOutsideInventory(class_1799 stack) {
      return !(this.field_7871 instanceof class_1661) && ConjuredItems.isConjured(stack) ? class_1799.field_8037 : stack;
   }
}
