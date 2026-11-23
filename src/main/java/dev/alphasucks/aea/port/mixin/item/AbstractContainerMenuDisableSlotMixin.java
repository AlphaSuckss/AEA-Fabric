package dev.alphasucks.aea.port.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.power.builtin.DisableSlotPower;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1703.class)
public abstract class AbstractContainerMenuDisableSlotMixin {
   private static final String DO_CLICK = "doClick(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V";
   private static final String MOVE_ITEM_STACK_TO = "moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z";

   @WrapOperation(
      method = {"method_30010(IILnet/minecraft/class_1713;Lnet/minecraft/class_1657;)V", "method_7616(Lnet/minecraft/class_1799;IIZ)Z"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1735;method_7680(Lnet/minecraft/class_1799;)Z")
   )
   private boolean apoli$refuseDisabledSlot(class_1735 slot, class_1799 stack, Operation<Boolean> original) {
      if (!(Boolean)original.call(new Object[]{slot, stack})) {
         return false;
      } else if (slot.field_7871 instanceof class_1661 inventory) {
         return !DisableSlotPower.has(inventory.field_7546) ? true : !DisableSlotPower.disabled(inventory.field_7546, slot.method_34266(), stack);
      } else {
         return true;
      }
   }
}
