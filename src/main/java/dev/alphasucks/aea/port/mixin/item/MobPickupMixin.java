package dev.alphasucks.aea.port.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.ItemPickupHandler;
import dev.alphasucks.aea.port.power.builtin.ItemPickupHandler.Pending;
import net.minecraft.class_1308;
import net.minecraft.class_1542;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1308.class)
public abstract class MobPickupMixin {
   @Unique
   private Pending apoli$pickup;

   @ModifyExpressionValue(
      method = "method_6007()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1308;method_20820(Lnet/minecraft/class_1799;)Z")
   )
   private boolean apoli$beforeMobPickup(boolean wants, @Local class_1542 item) {
      this.apoli$pickup = null;
      if (!wants) {
         return false;
      }

      class_1308 self = (class_1308)(Object)this;
      if (!ItemPickupHandler.watches(self)) {
         return true;
      }

      if (ItemPickupHandler.prevents(item, self)) {
         return false;
      }

      this.apoli$pickup = ItemPickupHandler.before(item, self, true);
      return true;
   }

   @Inject(
      method = "method_6007()V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1308;method_5949(Lnet/minecraft/class_1542;)V", shift = Shift.AFTER)
   )
   private void apoli$afterMobPickup(CallbackInfo ci) {
      Pending pending = this.apoli$pickup;
      if (pending != null) {
         this.apoli$pickup = null;
         if (pending.taken()) {
            ItemPickupHandler.after(pending, (class_1308)(Object)this);
         }
      }
   }
}
