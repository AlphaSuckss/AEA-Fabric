package dev.overgrown.sync.mixin.suppress_power;

import dev.overgrown.sync.factory.action.bientity.suppress_power.utils.SuppressedPowerManager;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Power.class, remap = false)
public abstract class PowerSuppressMixin {
   @Shadow
   protected class_1309 entity;
   @Shadow
   protected PowerType<?> type;

   @Inject(method = "isActive", at = @At("RETURN"), cancellable = true, remap = false)
   private void sync$checkSuppression(CallbackInfoReturnable<Boolean> cir) {
      if (Boolean.TRUE.equals(cir.getReturnValue())) {
         if (this.entity != null && this.type != null) {
            // picks the client-side copy for entities in client worlds (see SuppressedPowerSync)
            if (SuppressedPowerManager.isSuppressed(this.entity, this.type.getIdentifier())) {
               cir.setReturnValue(false);
            }
         }
      }
   }
}
