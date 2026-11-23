package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1661;
import net.minecraft.class_1706;
import net.minecraft.class_3914;
import net.minecraft.class_3917;
import net.minecraft.class_4861;
import org.spongepowered.asm.mixin.Mixin;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1706.class)
public abstract class AnvilScreenHandlerMixin extends class_4861 {
   public AnvilScreenHandlerMixin(class_3917<?> type, int syncId, class_1661 playerInventory, class_3914 context) {
      super(type, syncId, playerInventory, context);
   }

   // AEA: both repair-per-material spots (vanilla has two), so material cost is really halved
   @ModifyExpressionValue(method = "method_24928()V", at = {@At(value = "CONSTANT", args = "intValue=4", ordinal = 0), @At(value = "CONSTANT", args = "intValue=4", ordinal = 1)})
   private int halfRepairMaterialCost(int original) {
      return ClassPowerTypes.EFFICIENT_REPAIRS.isActive(this.field_22482) ? Math.max(1, original / 2) : original;
   }

   @ModifyExpressionValue(method = "method_24928()V", at = @At(value = "CONSTANT", args = "intValue=12", ordinal = 0))
   private int doubleCombineRepairDurabilityBonus(int original) {
      return ClassPowerTypes.EFFICIENT_REPAIRS.isActive(this.field_22482) ? original * 2 : original; // AEA: was * 12 (144% bonus instead of double)
   }
}
