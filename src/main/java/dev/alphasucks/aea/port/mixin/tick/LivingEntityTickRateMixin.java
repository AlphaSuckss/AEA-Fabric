package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.client.ClientTickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_1309.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityTickRateMixin {
   @ModifyVariable(method = "method_5683(FI)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private int apoli$headLerpSteps(int steps) {
      class_1297 self = (class_1297)(Object)this;
      return !ClientTickRates.idle() && self.method_37908().method_8608() ? ClientTickRates.headLerpSteps(self, steps) : steps;
   }

   @ModifyArg(method = "method_29242(Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_48565(F)V"))
   private float apoli$slowedWalkAnimation(float distance) {
      class_1297 self = (class_1297)(Object)this;
      return !ClientTickRates.idle() && self.method_37908().method_8608() ? ClientTickRates.walkAnimationInput(self, distance) : distance;
   }
}
