package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.client.ClientTickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_4184.class)
@Environment(EnvType.CLIENT)
public abstract class CameraTickRateMixin {
   @ModifyVariable(method = "method_19321(Lnet/minecraft/class_1922;Lnet/minecraft/class_1297;ZZF)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float apoli$slowedCameraPartial(float partialTick) {
      return ClientTickRates.cameraPartial(partialTick);
   }
}
