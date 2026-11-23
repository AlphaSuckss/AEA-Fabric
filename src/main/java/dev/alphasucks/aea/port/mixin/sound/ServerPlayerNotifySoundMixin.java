package dev.alphasucks.aea.port.mixin.sound;

import dev.alphasucks.aea.port.sound.SoundReplacer;
import net.minecraft.class_2767;
import net.minecraft.class_3222;
import net.minecraft.class_3244;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_7923;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3222.class)
public abstract class ServerPlayerNotifySoundMixin {
   @Shadow
   public class_3244 field_13987;

   @Inject(method = "method_17356(Lnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V", at = @At("HEAD"), cancellable = true)
   private void apoli$replaceNotifySound(class_3414 sound, class_3419 source, float volume, float pitch, CallbackInfo ci) {
      class_3222 self = (class_3222)(Object)this;
      if (SoundReplacer.hasEmission(self)) {
         ci.cancel();
         SoundReplacer.emit(
            self,
            sound,
            volume,
            pitch,
            (replaced, newVolume, newPitch) -> this.field_13987
               .method_14364(
                  new class_2767(
                     class_7923.field_41172.method_47983(replaced),
                     source,
                     self.method_23317(),
                     self.method_23318(),
                     self.method_23321(),
                     newVolume,
                     newPitch,
                     self.method_6051().method_43055()
                  )
               )
         );
      }
   }
}
