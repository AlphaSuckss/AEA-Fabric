package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.tick.TickRates;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3222.class)
public abstract class ServerPlayerTickRateMixin {
   @Inject(method = "method_14226()V", at = @At("HEAD"), cancellable = true)
   private void apoli$gatePlayerTick(CallbackInfo ci) {
      if (!TickRates.idle()) {
         if (!TickRates.shouldTickEntity((class_3222)(Object)this)) {
            ci.cancel();
         }
      }
   }
}
