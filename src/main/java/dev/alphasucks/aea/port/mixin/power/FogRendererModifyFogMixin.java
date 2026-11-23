package dev.alphasucks.aea.port.mixin.power;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.alphasucks.aea.port.power.builtin.ModifyFogInterpolator;
import dev.alphasucks.aea.port.power.builtin.ModifyFogHandler.FogData;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_638;
import net.minecraft.class_758;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_758.class)
public class FogRendererModifyFogMixin {
   @Shadow
   private static float field_4034;
   @Shadow
   private static float field_4033;
   @Shadow
   private static float field_4032;

   @Inject(method = "method_3210", at = @At("RETURN"))
   private static void apoli$modifyFogColor(class_4184 camera, float partialTick, class_638 level, int renderDistance, float darkenAmount, CallbackInfo ci) {
      class_243 vanilla = new class_243(field_4034, field_4033, field_4032);
      ModifyFogInterpolator.setDefaultColor(vanilla);
      FogData fog = ModifyFogInterpolator.getCurrent(null, null, vanilla);
      if (!fog.color.equals(FogData.UNSET_COLOR)) {
         field_4034 = (float)fog.color.field_1352;
         field_4033 = (float)fog.color.field_1351;
         field_4032 = (float)fog.color.field_1350;
         RenderSystem.clearColor(field_4034, field_4033, field_4032, 0.0F);
      }
   }

   @WrapOperation(method = "method_3211", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogStart(F)V"))
   private static void apoli$modifyFogNear(float vanilla, Operation<Void> original) {
      ModifyFogInterpolator.setDefaultS(vanilla);
      FogData fog = ModifyFogInterpolator.getCurrent(vanilla, null, null);
      original.call(new Object[]{fog.s == -1.0F ? vanilla : fog.s});
   }

   @WrapOperation(method = "method_3211", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderFogEnd(F)V"))
   private static void apoli$modifyFogFar(float vanilla, Operation<Void> original) {
      ModifyFogInterpolator.setDefaultV(vanilla);
      FogData fog = ModifyFogInterpolator.getCurrent(null, vanilla, null);
      original.call(new Object[]{fog.v == -1.0F ? vanilla : fog.v});
   }
}
