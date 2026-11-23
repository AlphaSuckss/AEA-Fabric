package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.alphasucks.aea.port.power.builtin.FluidVisionPower;
import dev.alphasucks.aea.port.power.builtin.FluidVisionPower.Config;
import dev.alphasucks.aea.port.power.builtin.FluidVisionPower.FogColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_758;
import net.minecraft.class_758.class_4596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_758.class, priority = 1500)
@Environment(EnvType.CLIENT)
public abstract class FogRendererFluidVisionMixin {
   @Shadow
   private static float field_4034;
   @Shadow
   private static float field_4033;
   @Shadow
   private static float field_4032;

   @WrapOperation(
      method = "method_3211(Lnet/minecraft/class_4184;Lnet/minecraft/class_758$class_4596;FZF)V",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V")
   )
   private static void apoli$fluidFogStart(
      float value, Operation<Void> original, class_4184 camera, class_4596 mode, float viewDistance, boolean thickFog, float partialTick
   ) {
      Config cfg = apoli$fluidVision(camera);
      original.call(new Object[]{cfg == null ? value : cfg.start()});
   }

   @WrapOperation(
      method = "method_3211(Lnet/minecraft/class_4184;Lnet/minecraft/class_758$class_4596;FZF)V",
      at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V")
   )
   private static void apoli$fluidFogEnd(
      float value, Operation<Void> original, class_4184 camera, class_4596 mode, float viewDistance, boolean thickFog, float partialTick
   ) {
      Config cfg = apoli$fluidVision(camera);
      original.call(new Object[]{cfg == null ? value : cfg.end()});
   }

   @Inject(method = "method_3210(Lnet/minecraft/class_4184;FLnet/minecraft/class_638;IF)V", at = @At("RETURN"))
   private static void apoli$fluidFogColor(class_4184 camera, float partialTick, class_638 level, int renderDistance, float darkness, CallbackInfo ci) {
      Config cfg = apoli$fluidVision(camera);
      if (cfg != null && !cfg.fogColor().isEmpty()) {
         FogColor color = (FogColor)cfg.fogColor().get();
         field_4034 = color.red();
         field_4033 = color.green();
         field_4032 = color.blue();
         RenderSystem.clearColor(field_4034, field_4033, field_4032, 0.0F);
      }
   }

   private static Config apoli$fluidVision(class_4184 camera) {
      return FluidVisionPower.activeFor(camera.method_19331(), camera.method_19334());
   }
}
