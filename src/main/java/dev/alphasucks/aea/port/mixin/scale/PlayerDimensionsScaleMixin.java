package dev.alphasucks.aea.port.mixin.scale;

import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
public abstract class PlayerDimensionsScaleMixin {
   @Inject(method = "method_18377", at = @At("RETURN"), cancellable = true)
   private void apoli$scaleDimensions(class_4050 pose, CallbackInfoReturnable<class_4048> cir) {
      class_1297 self = (class_1297)(Object)this;
      if (!Scales.untouched(self)) {
         float width = Scales.applied(self, ScaleTypes.HITBOX_WIDTH);
         float height = Scales.applied(self, ScaleTypes.HITBOX_HEIGHT);
         if (width != 1.0F || height != 1.0F) {
            cir.setReturnValue(((class_4048)cir.getReturnValue()).method_19539(width, height));
         }
      }
   }
}
