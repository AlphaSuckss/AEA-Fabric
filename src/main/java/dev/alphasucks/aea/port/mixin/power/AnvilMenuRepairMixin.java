package dev.alphasucks.aea.port.mixin.power;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.power.builtin.ModifyAnvilPower;
import net.minecraft.class_1661;
import net.minecraft.class_1706;
import net.minecraft.class_3914;
import net.minecraft.class_3917;
import net.minecraft.class_4861;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1706.class)
public abstract class AnvilMenuRepairMixin extends class_4861 {
   private AnvilMenuRepairMixin(@Nullable class_3917<?> type, int containerId, class_1661 inventory, class_3914 access) {
      super(type, containerId, inventory, access);
   }

   @ModifyExpressionValue(method = "method_24928", at = @At(value = "CONSTANT", args = "intValue=4"))
   private int apoli$modifyRepairMaterial(int original) {
      return ModifyAnvilPower.repairMaterial(this.field_22482, this.field_22480.method_5438(0), original);
   }

   @ModifyExpressionValue(method = "method_24928", at = @At(value = "CONSTANT", args = "intValue=12"))
   private int apoli$modifyCombineBonus(int original) {
      return ModifyAnvilPower.combineBonus(this.field_22482, this.field_22480.method_5438(0), original);
   }
}
