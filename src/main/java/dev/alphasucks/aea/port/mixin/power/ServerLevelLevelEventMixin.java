package dev.alphasucks.aea.port.mixin.power;

import dev.alphasucks.aea.port.power.builtin.PreventLevelEventPower;
import net.minecraft.class_1657;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3218.class)
public abstract class ServerLevelLevelEventMixin {
   @Inject(method = "method_8444(Lnet/minecraft/class_1657;ILnet/minecraft/class_2338;I)V", at = @At("HEAD"), cancellable = true)
   private void apoli$preventLevelEvent(@Nullable class_1657 player, int event, class_2338 pos, int data, CallbackInfo ci) {
      if (PreventLevelEventPower.prevents(player, event)) {
         ci.cancel();
      }
   }
}
