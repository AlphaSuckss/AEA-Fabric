package dev.alphasucks.aea.port.mixin.power;

import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.power.builtin.ActionOnBreedPower;
import net.minecraft.class_1296;
import net.minecraft.class_1429;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1429.class)
public abstract class AnimalBreedMixin {
   @Inject(
      method = "method_24650",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3218;method_30771(Lnet/minecraft/class_1297;)V", shift = Shift.AFTER)
   )
   private void apoli$actionOnBreed(class_3218 level, class_1429 mate, CallbackInfo ci, @Local class_1296 baby) {
      ActionOnBreedPower.fire((class_1429)(Object)this, mate, baby);
   }
}
