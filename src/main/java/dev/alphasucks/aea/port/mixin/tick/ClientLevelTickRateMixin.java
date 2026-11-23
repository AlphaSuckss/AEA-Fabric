package dev.alphasucks.aea.port.mixin.tick;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.alphasucks.aea.port.client.ClientTickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_638.class)
@Environment(EnvType.CLIENT)
public abstract class ClientLevelTickRateMixin {
   @Inject(method = "method_18116()V", at = @At("HEAD"))
   private void apoli$beginClientTick(CallbackInfo ci) {
      ClientTickRates.beginClientTick(((class_638)(Object)this).method_8510());
   }

   @Inject(method = "method_18646(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$gateEntityTick(class_1297 entity, CallbackInfo ci) {
      if (!ClientTickRates.idle()) {
         if (ClientTickRates.isGated(entity)) {
            if (!ClientTickRates.simulatedOnClient(entity) || !ClientTickRates.shouldTick(entity)) {
               ClientTickRates.keepRenderPose(entity);
               ClientTickRates.advanceLerp(entity);
               ClientTickRates.fadeHurt(entity);
               ci.cancel();
            }
         }
      }
   }

   @WrapWithCondition(
      method = "method_18647(Lnet/minecraft/class_1297;Lnet/minecraft/class_1297;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5842()V")
   )
   private boolean apoli$gatePassengerTick(class_1297 passenger, class_1297 vehicle, class_1297 rider) {
      if (!ClientTickRates.holdsPassenger(passenger)) {
         return true;
      }

      vehicle.method_24201(passenger);
      return false;
   }

   @Inject(
      method = "method_18646(Lnet/minecraft/class_1297;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_22862()V", shift = Shift.AFTER)
   )
   private void apoli$followOnGateTick(class_1297 entity, CallbackInfo ci) {
      if (!ClientTickRates.idle()) {
         ClientTickRates.advanceLerp(entity);
      }
   }
}
