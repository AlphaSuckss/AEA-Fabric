package io.github.apace100.originsclasses.mixin;

import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/class_1708$class_1711")
public class BrewingStandPotionSlotMixin {
   @Inject(method = "method_7631(Lnet/minecraft/class_1799;)Z", at = @At("HEAD"), cancellable = true)
   private static void preventBrewingExtendedPotions(class_1799 stack, CallbackInfoReturnable<Boolean> cir) {
      // AEA: getNbt instead of getOrCreateNbt - this check gave every item it saw an empty tag,
      // which stopped it stacking with untouched copies
      if (stack.method_7985() && stack.method_7969().method_10577("IsExtendedByCleric")) {
         cir.setReturnValue(false);
      }
   }
}
