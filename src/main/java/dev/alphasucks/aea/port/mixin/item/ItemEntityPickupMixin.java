package dev.alphasucks.aea.port.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.ItemPickupHandler;
import dev.alphasucks.aea.port.power.builtin.ItemPickupHandler.Pending;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_1542.class)
public abstract class ItemEntityPickupMixin {
   @Unique
   private Pending apoli$pickup;

   @ModifyArg(
      method = "method_5694(Lnet/minecraft/class_1657;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1661;method_7394(Lnet/minecraft/class_1799;)Z")
   )
   private class_1799 apoli$beforePickup(class_1799 stack, @Local(argsOnly = true) class_1657 player) {
      this.apoli$pickup = null;
      if (!ItemPickupHandler.watches(player)) {
         return stack;
      }

      class_1542 self = (class_1542)(Object)this;
      if (ItemPickupHandler.prevents(self, player)) {
         return class_1799.field_8037;
      }

      this.apoli$pickup = ItemPickupHandler.before(self, player, ItemPickupHandler.hasRoom(player, stack));
      return self.method_6983();
   }

   @ModifyExpressionValue(
      method = "method_5694(Lnet/minecraft/class_1657;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1661;method_7394(Lnet/minecraft/class_1799;)Z")
   )
   private boolean apoli$afterPickup(boolean added, @Local(argsOnly = true) class_1657 player) {
      Pending pending = this.apoli$pickup;
      if (pending == null) {
         return added;
      }

      this.apoli$pickup = null;
      if (added) {
         ItemPickupHandler.after(pending, player);
      }

      return added;
   }
}
