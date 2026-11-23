package dev.alphasucks.aea.port.mixin.loot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.power.builtin.LootTableReplacement;
import dev.alphasucks.aea.port.power.builtin.ReplaceLootTablePower;
import net.minecraft.class_2960;
import net.minecraft.class_52;
import net.minecraft.class_83;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_83.class)
public abstract class LootTableReferenceReplacedMixin {
   @Shadow
   @Final
   class_2960 field_993;

   @ModifyExpressionValue(
      method = "method_433",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_8489;getLootTable(Lnet/minecraft/class_2960;)Lnet/minecraft/class_52;")
   )
   private class_52 apoli$replacedTable(class_52 original) {
      return LootTableReplacement.isActive() && this.field_993.equals(ReplaceLootTablePower.REPLACED_TABLE) ? LootTableReplacement.peek() : original;
   }
}
