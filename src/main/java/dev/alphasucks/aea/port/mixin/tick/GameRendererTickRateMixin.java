package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.client.ClientTickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_757.class)
@Environment(EnvType.CLIENT)
public abstract class GameRendererTickRateMixin {
   @ModifyVariable(method = "method_3172(Lnet/minecraft/class_4587;Lnet/minecraft/class_4184;F)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
   private float apoli$slowedFirstPersonPartial(float partialTick) {
      return ClientTickRates.cameraPartial(partialTick);
   }
}
