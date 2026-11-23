package dev.alphasucks.aea.port.mixin.flag;

import dev.alphasucks.aea.port.power.builtin.FluidVisionPower;
import dev.alphasucks.aea.port.power.builtin.FluidVisionPower.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4603;
import net.minecraft.class_5636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_4603.class)
@Environment(EnvType.CLIENT)
public abstract class ScreenEffectFluidVisionMixin {
   @Inject(method = "method_23069(Lnet/minecraft/class_310;Lnet/minecraft/class_4587;)V", at = @At("HEAD"), cancellable = true)
   private static void apoli$suppressWaterOverlay(class_310 minecraft, class_4587 poseStack, CallbackInfo ci) {
      class_1297 camera = minecraft.method_1560();
      Config cfg = FluidVisionPower.activeFor(camera, class_5636.field_27886);
      if (cfg != null && !cfg.renderOverlay()) {
         ci.cancel();
      }
   }
}
