package dev.alphasucks.aea.port.mixin.sound;

import dev.alphasucks.aea.port.sound.SoundTracker;
import dev.alphasucks.aea.port.sound.SoundVariants;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_3218;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_6880;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3218.class)
public abstract class ServerLevelSoundTrackMixin {
   @Inject(method = "method_8465(Lnet/minecraft/class_1657;DDDLnet/minecraft/class_6880;Lnet/minecraft/class_3419;FFJ)V", at = @At("HEAD"))
   private void apoli$trackPositionedSound(
      @Nullable class_1657 except,
      double x,
      double y,
      double z,
      class_6880<class_3414> sound,
      class_3419 source,
      float volume,
      float pitch,
      long seed,
      CallbackInfo ci
   ) {
      if (SoundTracker.watching()) {
         class_3414 event = sound.comp_349();
         SoundTracker.record((class_1937)(Object)this, SoundVariants.unwrap(event.method_14833()), source, x, y, z, event.method_43044(volume));
      }
   }

   @Inject(
      method = "method_8449(Lnet/minecraft/class_1657;Lnet/minecraft/class_1297;Lnet/minecraft/class_6880;Lnet/minecraft/class_3419;FFJ)V",
      at = @At("HEAD")
   )
   private void apoli$trackEntitySound(
      @Nullable class_1657 except, class_1297 entity, class_6880<class_3414> sound, class_3419 source, float volume, float pitch, long seed, CallbackInfo ci
   ) {
      if (SoundTracker.watching()) {
         class_3414 event = sound.comp_349();
         SoundTracker.record(
            (class_1937)(Object)this,
            SoundVariants.unwrap(event.method_14833()),
            source,
            entity.method_23317(),
            entity.method_23318(),
            entity.method_23321(),
            event.method_43044(volume)
         );
      }
   }
}
