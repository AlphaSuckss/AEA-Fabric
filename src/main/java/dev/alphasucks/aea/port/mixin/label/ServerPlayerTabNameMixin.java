package dev.alphasucks.aea.port.mixin.label;

import dev.alphasucks.aea.port.entity.DisplayNameOverrides;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3222.class)
public abstract class ServerPlayerTabNameMixin {
   @Inject(method = "method_14206", at = @At("HEAD"), cancellable = true)
   private void apoli$overrideTabName(CallbackInfoReturnable<class_2561> cir) {
      class_2561 override = DisplayNameOverrides.tabNameFor((class_3222)(Object)this);
      if (override != null) {
         cir.setReturnValue(override);
      }
   }
}
