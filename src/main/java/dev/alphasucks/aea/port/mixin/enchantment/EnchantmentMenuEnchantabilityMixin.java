package dev.alphasucks.aea.port.mixin.enchantment;

import dev.alphasucks.aea.port.power.builtin.ModifyEnchantabilityPower;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1718;
import net.minecraft.class_1799;
import net.minecraft.class_1889;
import net.minecraft.class_3914;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1718.class)
public abstract class EnchantmentMenuEnchantabilityMixin {
   @Unique
   private class_1657 apoli$enchanter;

   @Inject(method = "<init>(ILnet/minecraft/class_1661;Lnet/minecraft/class_3914;)V", at = @At("TAIL"))
   private void apoli$rememberEnchanter(int containerId, class_1661 inventory, class_3914 access, CallbackInfo ci) {
      this.apoli$enchanter = inventory.field_7546;
   }

   @Inject(method = "method_7637", at = @At("HEAD"))
   private void apoli$beginEnchanting(class_1799 stack, int slot, int cost, CallbackInfoReturnable<List<class_1889>> cir) {
      ModifyEnchantabilityPower.begin(this.apoli$enchanter);
   }

   @Inject(method = "method_7637", at = @At("RETURN"))
   private void apoli$endEnchanting(class_1799 stack, int slot, int cost, CallbackInfoReturnable<List<class_1889>> cir) {
      ModifyEnchantabilityPower.end();
   }
}
