package dev.alphasucks.aea.port.mixin.sound;

import dev.alphasucks.aea.port.sound.ReplaceableSound;
import dev.alphasucks.aea.port.sound.SoundReplacer;
import dev.alphasucks.aea.port.sound.SoundReplacer.Emitter;
import net.minecraft.class_1102;
import net.minecraft.class_1109;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1140.class)
public abstract class SoundEngineReceptionMixin {
   @Unique
   private static boolean apoli$reentrant;

   @Inject(method = "method_4854(Lnet/minecraft/class_1113;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$replaceReceivedSound(class_1113 instance, CallbackInfo ci) {
      if (!apoli$reentrant) {
         if (instance instanceof class_1102) {
            class_746 player = class_310.method_1551().field_1724;
            if (player != null && SoundReplacer.hasReception(player)) {
               final ReplaceableSound access = (ReplaceableSound)instance;
               final class_1140 self = (class_1140)(Object)this;
               class_3414 original = class_3414.method_47908(instance.method_4775());
               apoli$reentrant = true;

               try {
                  SoundReplacer.receive(
                     player,
                     original,
                     access.apoli$baseVolume(),
                     access.apoli$basePitch(),
                     new Emitter() {
                        @Override
                        public void play(class_3414 sound, float volume, float pitch) {
                           access.apoli$replaceWith(sound.method_14833(), volume, pitch);
                           self.method_4854(instance);
                        }

                        @Override
                        public void layer(class_3414 sound, float volume, float pitch) {
                           self.method_4854(
                              new class_1109(
                                 sound.method_14833(),
                                 instance.method_4774(),
                                 volume,
                                 pitch,
                                 class_1113.method_43221(),
                                 false,
                                 0,
                                 instance.method_4777(),
                                 instance.method_4784(),
                                 instance.method_4779(),
                                 instance.method_4778(),
                                 instance.method_4787()
                              )
                           );
                        }
                     }
                  );
               } finally {
                  apoli$reentrant = false;
               }

               ci.cancel();
            }
         }
      }
   }
}
