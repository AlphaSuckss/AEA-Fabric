package dev.alphasucks.aea.port.mixin.power;

import dev.alphasucks.aea.port.power.builtin.ActionOnTamePower;
import net.minecraft.class_1321;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1321.class)
public abstract class TamableAnimalTameMixin {
   @Inject(method = "method_6170", at = @At("TAIL"))
   private void apoli$actionOnTame(class_1657 player, CallbackInfo ci) {
      ActionOnTamePower.fire(player, (class_1321)(Object)this);
   }
}
