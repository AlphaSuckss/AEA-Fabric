package dev.alphasucks.aea.port.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.ClientResolvedPowers;
import dev.alphasucks.aea.port.power.builtin.ModifyFovPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_742;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_742.class)
@Environment(EnvType.CLIENT)
public abstract class AbstractClientPlayerFovMixin {
   @ModifyReturnValue(method = "method_3118", at = @At(value = "RETURN", ordinal = 0))
   private float apoli$modifyScopedFov(float original) {
      return ClientResolvedPowers.isEmpty() ? original : ModifyFovPower.apply((class_742)(Object)this, original, ClientResolvedPowers::isActive);
   }

   @ModifyArg(method = "method_3118", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3532;method_16439(FFF)F"), index = 0)
   private float apoli$fovEffectScale(float delta, float start, float end) {
      if (ClientResolvedPowers.isEmpty()) {
         return delta;
      }

      class_742 self = (class_742)(Object)this;
      return ModifyFovPower.ignoresEffectScale(self, ClientResolvedPowers::isActive) ? 1.0F : delta;
   }

   @ModifyArg(method = "method_3118", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_3532;method_16439(FFF)F"), index = 2)
   private float apoli$modifyFov(float delta, float start, float end) {
      return ClientResolvedPowers.isEmpty() ? end : ModifyFovPower.apply((class_742)(Object)this, end, ClientResolvedPowers::isActive);
   }
}
