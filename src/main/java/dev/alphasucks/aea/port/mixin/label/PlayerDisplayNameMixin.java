package dev.alphasucks.aea.port.mixin.label;

import dev.alphasucks.aea.port.entity.DisplayNameOverrides;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1657.class)
public abstract class PlayerDisplayNameMixin {
   @Inject(method = "method_5476", at = @At("HEAD"), cancellable = true)
   private void apoli$overrideDisplayName(CallbackInfoReturnable<class_2561> cir) {
      class_2561 override = DisplayNameOverrides.chatNameFor((class_1657)(Object)this);
      if (override != null) {
         cir.setReturnValue(override);
      }
   }
}
