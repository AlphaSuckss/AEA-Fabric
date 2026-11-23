package dev.alphasucks.aea.port.mixin.sound;

import dev.alphasucks.aea.port.sound.SoundTracker;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import net.minecraft.class_310;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1140.class)
public abstract class SoundEngineTrackMixin {
   @Inject(method = "method_4854(Lnet/minecraft/class_1113;)V", at = @At("HEAD"))
   private void apoli$trackReceivedSound(class_1113 instance, CallbackInfo ci) {
      if (SoundTracker.watching()) {
         class_638 level = class_310.method_1551().field_1687;
         if (level != null && instance.method_26273()) {
            SoundTracker.record(
               level, instance.method_4775(), instance.method_4774(), instance.method_4784(), instance.method_4779(), instance.method_4778(), -1.0F
            );
         }
      }
   }
}
