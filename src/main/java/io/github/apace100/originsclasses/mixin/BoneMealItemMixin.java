package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1269;
import net.minecraft.class_1752;
import net.minecraft.class_1799;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2256;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1752.class)
public class BoneMealItemMixin {
   // AEA: per-thread (client and server threads both run useOnBlock in singleplayer)
   private static final ThreadLocal<Boolean> FARMER = ThreadLocal.withInitial(() -> false);

   @Inject(
      method = "method_7884(Lnet/minecraft/class_1838;)Lnet/minecraft/class_1269;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_2338;method_10093(Lnet/minecraft/class_2350;)Lnet/minecraft/class_2338;",
         shift = Shift.AFTER
      )
   )
   private void saveFarmerForLater(class_1838 context, CallbackInfoReturnable<class_1269> cir) {
      if (context.method_8036() != null && ClassPowerTypes.BETTER_BONE_MEAL.isActive(context.method_8036())) {
         FARMER.set(true);
      }
   }

   @Inject(method = "method_7884(Lnet/minecraft/class_1838;)Lnet/minecraft/class_1269;", at = @At("RETURN"))
   private void removeSavedFarmer(class_1838 context, CallbackInfoReturnable<class_1269> cir) {
      FARMER.set(false);
   }

   @Inject(method = "method_7720(Lnet/minecraft/class_1799;Lnet/minecraft/class_1937;Lnet/minecraft/class_2338;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7934(I)V"))
   private static void applyAdditionalFarmerBoneMeal(class_1799 stack, class_1937 world, class_2338 pos, CallbackInfoReturnable<Boolean> cir) {
      if (FARMER.get() && world instanceof class_3218) {
         class_2680 blockState = world.method_8320(pos);
         // AEA: the first growth can turn the block into something else (sapling -> tree), which crashed here
         if (blockState.method_26204() instanceof class_2256 fertilizable
            && fertilizable.method_9651(world, pos, blockState, false)
            && fertilizable.method_9650(world, world.field_9229, pos, blockState)) {
            fertilizable.method_9652((class_3218)world, world.field_9229, pos, blockState);
         }
      }
   }
}
