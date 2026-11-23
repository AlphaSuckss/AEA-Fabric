package dev.alphasucks.aea.port.mixin.flag;

import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.SprintingPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_746.class)
@Environment(EnvType.CLIENT)
public abstract class LocalPlayerSprintMixin {
   @Inject(method = "method_46743()Z", at = @At("RETURN"), cancellable = true)
   private void apoli$gateSprinting(CallbackInfoReturnable<Boolean> cir) {
      class_746 self = (class_746)(Object)this;
      if (PowerLookup.hasActive(self, ApoliIds.PREVENT_SPRINTING)) {
         cir.setReturnValue(false);
      } else if (cir.getReturnValueZ()) {
         if (self.method_31549().field_7478) {
            if (!self.method_5765()) {
               if (!(self.method_7344().method_7586() > 6.0F)) {
                  if (!self.method_7337() && !self.method_7325()) {
                     cir.setReturnValue(false);
                  }
               }
            }
         }
      }
   }

   @Inject(method = "method_6007", at = @At("HEAD"))
   private void apoli$applySprintPowers(CallbackInfo ci) {
      class_746 self = (class_746)(Object)this;
      if (self.method_5624()) {
         if (PowerLookup.hasActive(self, ApoliIds.PREVENT_SPRINTING)) {
            self.method_5728(false);
         }
      } else if (self.field_3913.field_3910 && SprintingPower.isSprinting(self)) {
         self.method_5728(true);
      }
   }
}
