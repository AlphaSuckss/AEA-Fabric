package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1714;
import net.minecraft.class_1723;
import net.minecraft.class_4317;
import net.minecraft.class_5455;
import net.minecraft.class_8566;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_4317.class)
public class RepairItemRecipeMixin {
   @ModifyExpressionValue(method = "method_20807(Lnet/minecraft/class_8566;Lnet/minecraft/class_5455;)Lnet/minecraft/class_1799;", at = @At(value = "CONSTANT", args = "intValue=5", ordinal = 0))
   private int doubleRepairDurabilityBonus(int original, @Local(argsOnly = true) class_8566 inventory) {
      class_1703 handler = ((CraftingInventoryAccessor)inventory).getHandler();
      class_1657 player = null;
      if (handler instanceof class_1714) {
         player = ((CraftingScreenHandlerAccessor)handler).getPlayer();
      } else if (handler instanceof class_1723) {
         player = ((PlayerScreenHandlerAccessor)handler).getOwner();
      }

      return player != null && ClassPowerTypes.EFFICIENT_REPAIRS.isActive(player) ? original * 3 : original;
   }
}
