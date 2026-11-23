package dev.alphasucks.aea.port.mixin.scale;

import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_4184.class)
@Environment(EnvType.CLIENT)
public abstract class CameraScaleMixin {
   @Shadow
   private class_1297 field_18711;

   @ModifyArg(
      method = "method_19321(Lnet/minecraft/class_1922;Lnet/minecraft/class_1297;ZZF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19318(D)D")
   )
   private double apoli$scaleThirdPersonDistance(double distance) {
      class_1297 viewed = this.field_18711;
      if (viewed == null) {
         return distance;
      } else {
         return Scales.untouched(viewed) ? distance : distance * Scales.applied(viewed, ScaleTypes.THIRD_PERSON);
      }
   }
}
