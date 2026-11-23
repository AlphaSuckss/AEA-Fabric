package dev.alphasucks.aea.port.mixin.modelparts;

import dev.alphasucks.aea.port.access.PartVisibilityUndo;
import dev.alphasucks.aea.port.client.particle.RenderedPoses;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_897;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererModelPartsMixin {
   private static final String RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
   private static final String RENDER_TO_BUFFER = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V";
   @Shadow
   protected class_583<?> field_4737;

   @Inject(method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V", at = @At("HEAD"))
   private void apoli$beginModelParts(class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      RenderedPoses.begin(entity, poseStack);
   }

   @Inject(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_583;method_2828(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;IIFFFF)V")
   )
   private void apoli$captureModelParts(class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      RenderedPoses.capture((class_897)(Object)this, entity, poseStack, this.field_4737, partialTick);
   }

   @Inject(method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V", at = @At("RETURN"))
   private void apoli$endModelParts(class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      if (this.field_4737 instanceof PartVisibilityUndo undo) {
         undo.apoli$undoPartVisibility();
      }

      RenderedPoses.end();
   }
}
