package dev.alphasucks.aea.port.mixin.emissive;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.EmissivePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_897.class)
@Environment(EnvType.CLIENT)
public abstract class EntityRendererEmissiveMixin {
   @ModifyReturnValue(method = "method_24087", at = @At("RETURN"))
   private int apoli$emissiveBlockLight(int original, class_1297 entity, class_2338 pos) {
      int luminance = EmissivePower.selfLitLuminanceOf(entity);
      return luminance > original ? luminance : original;
   }
}
