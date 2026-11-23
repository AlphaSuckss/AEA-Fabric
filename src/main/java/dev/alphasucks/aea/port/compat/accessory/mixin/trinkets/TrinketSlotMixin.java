package dev.alphasucks.aea.port.compat.accessory.mixin.trinkets;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.api.SlotReference;
import dev.alphasucks.aea.port.compat.accessory.backend.TrinketsBackend;
import dev.alphasucks.aea.port.compat.accessory.power.PreventAccessoryEquipPower;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TrinketSlot.class)
public interface TrinketSlotMixin {
   @ModifyReturnValue(method = "canInsert", at = @At("RETURN"))
   private static boolean apoli$preventEquip(boolean original, class_1799 stack, SlotReference slotRef, class_1309 entity) {
      return original && !PreventAccessoryEquipPower.isPrevented(entity, TrinketsBackend.slotRef(slotRef), stack);
   }
}
