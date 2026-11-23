package dev.alphasucks.aea.port.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.sound.SoundReplacer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(class_746.class)
public abstract class LocalPlayerSoundMixin {
   @WrapOperation(
      method = {"method_5783(Lnet/minecraft/class_3414;FF)V", "method_17356(Lnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V"},
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1937;method_8486(DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FFZ)V")
   )
   private void apoli$replaceOwnSound(
      class_1937 level,
      double x,
      double y,
      double z,
      class_3414 sound,
      class_3419 source,
      float volume,
      float pitch,
      boolean distanceDelay,
      Operation<Void> original
   ) {
      class_1297 self = (class_1297)(Object)this;
      if (!SoundReplacer.hasEmission(self)) {
         original.call(new Object[]{level, x, y, z, sound, source, volume, pitch, distanceDelay});
      } else {
         SoundReplacer.emitLocally(
            self,
            sound,
            volume,
            pitch,
            (replaced, newVolume, newPitch) -> original.call(new Object[]{level, x, y, z, replaced, source, newVolume, newPitch, distanceDelay})
         );
      }
   }
}
