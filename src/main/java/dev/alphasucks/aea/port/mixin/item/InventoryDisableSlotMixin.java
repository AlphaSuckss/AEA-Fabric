package dev.alphasucks.aea.port.mixin.item;

import dev.alphasucks.aea.port.power.builtin.DisableSlotPower;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1661.class)
public abstract class InventoryDisableSlotMixin {
   @Shadow
   @Final
   public class_1657 field_7546;

   @Inject(method = "method_7376", at = @At("RETURN"), cancellable = true)
   private void apoli$skipDisabledFreeSlot(CallbackInfoReturnable<Integer> cir) {
      int found = (Integer)cir.getReturnValue();
      if (found >= 0 && DisableSlotPower.has(this.field_7546)) {
         if (DisableSlotPower.disabled(this.field_7546, found, class_1799.field_8037)) {
            class_1661 self = (class_1661)(Object)this;

            for (int index = 0; index < 36; index++) {
               if (self.method_5438(index).method_7960() && !DisableSlotPower.disabled(this.field_7546, index, class_1799.field_8037)) {
                  cir.setReturnValue(index);
                  return;
               }
            }

            cir.setReturnValue(-1);
         }
      }
   }

   @Inject(method = "method_7390", at = @At("RETURN"), cancellable = true)
   private void apoli$skipDisabledPartialSlot(class_1799 stack, CallbackInfoReturnable<Integer> cir) {
      int found = (Integer)cir.getReturnValue();
      if (found >= 0 && DisableSlotPower.has(this.field_7546)) {
         if (DisableSlotPower.disabled(this.field_7546, found, stack)) {
            cir.setReturnValue(-1);
         }
      }
   }
}
