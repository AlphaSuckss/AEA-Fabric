package dev.alphasucks.aea.port.mixin.tick;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.alphasucks.aea.port.tick.TickRates;
import net.minecraft.class_1297;
import net.minecraft.class_2818;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3218.class)
public abstract class ServerLevelTickRateMixin {
   @Inject(method = "method_18762(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$gateEntityTick(class_1297 entity, CallbackInfo ci) {
      if (!TickRates.idle()) {
         if (!TickRates.shouldTickEntity(entity)) {
            ci.cancel();
         }
      }
   }

   @WrapWithCondition(
      method = "method_18763(Lnet/minecraft/class_1297;Lnet/minecraft/class_1297;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5842()V")
   )
   private boolean apoli$gatePassengerTick(class_1297 passenger, class_1297 vehicle, class_1297 rider) {
      if (!TickRates.idle() && !TickRates.shouldTickEntity(passenger)) {
         vehicle.method_24201(passenger);
         return false;
      } else {
         return true;
      }
   }

   @Inject(method = "method_18203(Lnet/minecraft/class_2818;I)V", at = @At("HEAD"), cancellable = true)
   private void apoli$gateChunkTick(class_2818 chunk, int randomTickSpeed, CallbackInfo ci) {
      if (!TickRates.idle()) {
         if (!TickRates.shouldTickChunk((class_3218)(Object)this, chunk.method_12004().method_8324())) {
            ci.cancel();
         }
      }
   }

   @ModifyReturnValue(method = "method_39425(J)Z", at = @At("RETURN"))
   private boolean apoli$gateBlockTicks(boolean original, long chunkPos) {
      return original && (TickRates.idle() || TickRates.shouldTickChunk((class_3218)(Object)this, chunkPos));
   }
}
