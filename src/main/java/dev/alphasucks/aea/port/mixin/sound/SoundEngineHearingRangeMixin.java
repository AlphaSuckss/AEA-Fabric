package dev.alphasucks.aea.port.mixin.sound;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.ClientResolvedPowers;
import dev.alphasucks.aea.port.power.builtin.ModifyHearingRangePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1111;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import net.minecraft.class_310;
import net.minecraft.class_1113.class_1114;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1140.class)
@Environment(EnvType.CLIENT)
public abstract class SoundEngineHearingRangeMixin {
   @ModifyExpressionValue(method = "method_4854(Lnet/minecraft/class_1113;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
   private float apoli$hearingRange(float volumeFactor, @Local(argsOnly = true) class_1113 instance, @Local class_1111 sound) {
      if (ClientResolvedPowers.isEmpty()) {
         return volumeFactor;
      }

      if (!instance.method_4787() && instance.method_4777() == class_1114.field_5476) {
         int distance = sound.method_4770();
         if (distance <= 0) {
            return volumeFactor;
         }

         double base = (double)volumeFactor * distance;
         double range = ModifyHearingRangePower.resolvedSoundRange(class_310.method_1551().field_1724, base, ClientResolvedPowers::isActive);
         return range == base ? volumeFactor : (float)(Math.max(range, 0.001) / distance);
      } else {
         return volumeFactor;
      }
   }
}
