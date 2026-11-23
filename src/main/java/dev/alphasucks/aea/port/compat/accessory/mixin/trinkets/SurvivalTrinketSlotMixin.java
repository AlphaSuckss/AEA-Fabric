package dev.alphasucks.aea.port.compat.accessory.mixin.trinkets;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.SurvivalTrinketSlot;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketInventory;
import dev.alphasucks.aea.port.compat.accessory.backend.TrinketsBackend;
import dev.alphasucks.aea.port.compat.accessory.power.PreventAccessoryUnequipPower;
import dev.alphasucks.aea.port.item.ConjuredItems;
import net.minecraft.class_1263;
import net.minecraft.class_1657;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurvivalTrinketSlot.class)
public abstract class SurvivalTrinketSlotMixin extends class_1735 {
   @Shadow
   @Final
   private TrinketInventory trinketInventory;
   @Shadow
   @Final
   private int slotOffset;

   public SurvivalTrinketSlotMixin(class_1263 container, int index, int x, int y) {
      super(container, index, x, y);
   }

   @ModifyReturnValue(method = "method_7674", at = @At("RETURN"))
   private boolean apoli$preventUnequip(boolean original, class_1657 player) {
      class_1799 stack = this.method_7677();
      if (ConjuredItems.isLocked(stack)) {
         return false;
      }

      SlotReference slotRef = new SlotReference(this.trinketInventory, this.slotOffset);
      return original && !PreventAccessoryUnequipPower.isPrevented(player, TrinketsBackend.slotRef(slotRef), stack);
   }
}
