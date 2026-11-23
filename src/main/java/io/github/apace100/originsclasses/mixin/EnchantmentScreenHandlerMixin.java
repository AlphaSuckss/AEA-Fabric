package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.OriginsClasses;
import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1718;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_3914;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1718.class)
public class EnchantmentScreenHandlerMixin {
   private class_1657 enchanter;

   @Inject(method = "<init>(ILnet/minecraft/class_1661;Lnet/minecraft/class_3914;)V", at = @At("TAIL"))
   private void saveEnchanterInHandler(int syncId, class_1661 playerInventory, class_3914 context, CallbackInfo ci) {
      this.enchanter = playerInventory.field_7546;
   }

   @Inject(
      method = "method_17411(Lnet/minecraft/class_1799;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1718;method_7637(Lnet/minecraft/class_1799;II)Ljava/util/List;"
      )
   )
   private void saveEnchanterForPreview(class_1799 stack, class_1937 world, class_2338 pos, CallbackInfo ci) {
      OriginsClasses.isClericEnchanting = ClassPowerTypes.BETTER_ENCHANTING.isActive(this.enchanter);
   }

   @Inject(
      method = "method_17410(Lnet/minecraft/class_1799;ILnet/minecraft/class_1657;ILnet/minecraft/class_1799;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1718;method_7637(Lnet/minecraft/class_1799;II)Ljava/util/List;"
      )
   )
   private void saveEnchanter(
      class_1799 itemStack, int id, class_1657 playerEntity, int level, class_1799 stack2, class_1937 world, class_2338 pos, CallbackInfo ci
   ) {
      OriginsClasses.isClericEnchanting = ClassPowerTypes.BETTER_ENCHANTING.isActive(playerEntity);
   }

   // AEA: reset the flag after each roll - it used to stay set when the item had 0 enchantability,
   // leaking the +10 into the next unrelated enchant roll (loot, mob gear, villager trades)
   @Inject(
      method = "method_17411(Lnet/minecraft/class_1799;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1718;method_7637(Lnet/minecraft/class_1799;II)Ljava/util/List;", shift = org.spongepowered.asm.mixin.injection.At.Shift.AFTER)
   )
   private void aea$clearAfterPreview(class_1799 stack, class_1937 world, class_2338 pos, CallbackInfo ci) {
      OriginsClasses.isClericEnchanting = false;
   }

   @Inject(
      method = "method_17410(Lnet/minecraft/class_1799;ILnet/minecraft/class_1657;ILnet/minecraft/class_1799;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1718;method_7637(Lnet/minecraft/class_1799;II)Ljava/util/List;", shift = org.spongepowered.asm.mixin.injection.At.Shift.AFTER)
   )
   private void aea$clearAfterEnchant(class_1799 itemStack, int id, class_1657 playerEntity, int level, class_1799 stack2, class_1937 world, class_2338 pos, CallbackInfo ci) {
      OriginsClasses.isClericEnchanting = false;
   }
}
