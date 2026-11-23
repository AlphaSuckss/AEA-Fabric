package dev.alphasucks.aea.port.compat.walkers.mixin;

import dev.alphasucks.aea.port.compat.walkers.power.ShapePowers;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "tocraft/walkers/api/PlayerAbilities", remap = false)
public abstract class PlayerAbilitiesMixin {
   @Inject(method = "useAbility", at = @At("HEAD"), cancellable = true, remap = false)
   private static void apoli$shapeAbilityUse(class_1657 player, CallbackInfo ci) {
      if (!player.method_37908().method_8608()) {
         if (ShapePowers.preventShapeAbilityUse(player)) {
            ci.cancel();
         } else {
            ShapePowers.fireShapeAbilityUse(player);
         }
      }
   }
}
