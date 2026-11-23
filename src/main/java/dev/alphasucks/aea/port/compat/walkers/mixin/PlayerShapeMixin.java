package dev.alphasucks.aea.port.compat.walkers.mixin;

import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers;
import net.minecraft.class_1309;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "tocraft/walkers/api/PlayerShape", remap = false)
public abstract class PlayerShapeMixin {
   @Inject(method = "updateShapes", at = @At("HEAD"), cancellable = true, remap = false)
   private static void apoli$preventShapeChange(class_3222 player, class_1309 shape, CallbackInfoReturnable<Boolean> cir) {
      if (ShapePowers.preventShapeChange(player, shape)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "updateShapes", at = @At("RETURN"), remap = false)
   private static void apoli$actionOnShapeChange(class_3222 player, class_1309 shape, CallbackInfoReturnable<Boolean> cir) {
      if (Boolean.TRUE.equals(cir.getReturnValue())) {
         ShapePowers.fireShapeChange(player, shape);
      }
   }
}
