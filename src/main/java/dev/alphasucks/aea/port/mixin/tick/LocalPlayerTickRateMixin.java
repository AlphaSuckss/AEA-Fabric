package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.client.ClientTickRates;
import dev.alphasucks.aea.port.tick.TickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_746.class)
@Environment(EnvType.CLIENT)
public abstract class LocalPlayerTickRateMixin {
   @Inject(method = "method_5773()V", at = @At("HEAD"), cancellable = true)
   private void apoli$gateLocalPlayer(CallbackInfo ci) {
      if (!ClientTickRates.idle()) {
         class_746 self = (class_746)(Object)this;
         int rate = ClientTickRates.rateOf(self);
         if (rate >= 0) {
            if (rate <= 0 || !TickRates.gate(rate, ClientTickRates.baseRate(), self.method_37908().method_8510())) {
               ClientTickRates.keepViewBob(self);
               ci.cancel();
            }
         }
      }
   }
}
