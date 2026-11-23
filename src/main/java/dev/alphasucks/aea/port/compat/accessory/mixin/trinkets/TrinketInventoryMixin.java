package dev.alphasucks.aea.port.compat.accessory.mixin.trinkets;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.emi.trinkets.api.TrinketInventory;
import dev.alphasucks.aea.port.compat.accessory.backend.TrinketsBackend;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TrinketInventory.class)
public abstract class TrinketInventoryMixin {
   @WrapMethod(method = "method_5447")
   private void apoli$onSetItem(int slot, class_1799 stack, Operation<Void> original) {
      TrinketInventory self = (TrinketInventory)(Object)this;
      class_1799 old = self.method_5438(slot).method_7972();
      original.call(new Object[]{slot, stack});
      if (!class_1799.method_7973(old, stack)) {
         TrinketsBackend.handleChange(self, slot, old, stack);
      }
   }
}
