package dev.alphasucks.aea.port.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.sound.SoundReplacer;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1657.class)
public abstract class PlayerPlaySoundMixin {
   @WrapOperation(
      method = {
            "method_5783(Lnet/minecraft/class_3414;FF)V",
            "method_7324(Lnet/minecraft/class_1297;)V",
            "method_7316(I)V",
            "method_18866(Lnet/minecraft/class_1937;Lnet/minecraft/class_1799;)Lnet/minecraft/class_1799;"
      },
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_1937;method_43128(Lnet/minecraft/class_1657;DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V"
      )
   )
   private void apoli$replacePlayerSound(
      class_1937 level,
      class_1657 except,
      double x,
      double y,
      double z,
      class_3414 sound,
      class_3419 source,
      float volume,
      float pitch,
      Operation<Void> original
   ) {
      SoundReplacer.emit(
         (class_1297)(Object)this,
         sound,
         volume,
         pitch,
         (replaced, newVolume, newPitch) -> original.call(new Object[]{level, except, x, y, z, replaced, source, newVolume, newPitch})
      );
   }
}
