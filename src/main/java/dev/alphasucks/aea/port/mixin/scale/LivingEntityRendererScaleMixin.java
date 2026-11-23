package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.render.ScaleRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererScaleMixin {
   private static final String RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
   private static final String SETUP_ROTATIONS = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V";

   @Inject(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_922;method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V")
   )
   private void apoli$uniformModelScale(class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      float width = ScaleRender.modelWidth(entity, partialTick);
      if (width != 1.0F) {
         if (ScaleRender.lying(entity)) {
            poseStack.method_46416(0.0F, 0.125F * (width - 1.0F), 0.0F);
         }

         poseStack.method_22905(width, width, width);
      }
   }

   @Inject(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_922;method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V",
         shift = Shift.AFTER
      )
   )
   private void apoli$stretchModel(class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      float width = ScaleRender.modelWidth(entity, partialTick);
      float height = ScaleRender.modelHeight(entity, partialTick);
      if (height != width) {
         poseStack.method_22905(1.0F, height / width, 1.0F);
      }
   }

   @ModifyArg(
      method = "method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_46416(FFF)V"),
      index = 1
   )
   private float apoli$upsideDownInWorldUnits(float y, @Local(argsOnly = true) class_1309 entity, @Local(argsOnly = true, ordinal = 2) float partialTick) {
      float width = ScaleRender.modelWidth(entity, partialTick);
      return width == 1.0F ? y : y / width;
   }
}
