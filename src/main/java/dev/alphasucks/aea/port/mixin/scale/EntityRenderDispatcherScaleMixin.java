package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.render.ScaleRender;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_4538;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import net.minecraft.class_898;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_898.class)
@Environment(EnvType.CLIENT)
public abstract class EntityRenderDispatcherScaleMixin {
   @Inject(
      method = "method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(
         value = "INVOKE",
         shift = Shift.BEFORE,
         target = "Lnet/minecraft/class_897;method_3936(Lnet/minecraft/class_1297;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      )
   )
   private void apoli$scaleModelPush(
      class_1297 entity,
      double x,
      double y,
      double z,
      float yaw,
      float partialTick,
      class_4587 poseStack,
      class_4597 buffer,
      int light,
      CallbackInfo ci,
      @Local class_897<?> renderer
   ) {
      if (!(renderer instanceof class_922)) {
         float width = ScaleRender.modelWidth(entity, partialTick);
         float height = ScaleRender.modelHeight(entity, partialTick);
         if (width != 1.0F || height != 1.0F) {
            poseStack.method_22903();
            poseStack.method_22905(width, height, width);
         }
      }
   }

   @Inject(
      method = "method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(
         value = "INVOKE",
         shift = Shift.AFTER,
         target = "Lnet/minecraft/class_897;method_3936(Lnet/minecraft/class_1297;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V"
      )
   )
   private void apoli$scaleModelPop(
      class_1297 entity,
      double x,
      double y,
      double z,
      float yaw,
      float partialTick,
      class_4587 poseStack,
      class_4597 buffer,
      int light,
      CallbackInfo ci,
      @Local class_897<?> renderer
   ) {
      if (!(renderer instanceof class_922)) {
         if (ScaleRender.modelWidth(entity, partialTick) != 1.0F || ScaleRender.modelHeight(entity, partialTick) != 1.0F) {
            poseStack.method_22909();
         }
      }
   }

   @ModifyArg(
      method = "method_3954(Lnet/minecraft/class_1297;DDDFFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      index = 6,
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_898;method_23166(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;Lnet/minecraft/class_1297;FFLnet/minecraft/class_4538;F)V"
      )
   )
   private float apoli$scaleShadow(
      class_4587 poseStack, class_4597 buffer, class_1297 entity, float strength, float partialTick, class_4538 level, float radius
   ) {
      return Scales.untouched(entity) ? radius : radius * Scales.applied(entity, ScaleTypes.MODEL_WIDTH, partialTick);
   }
}
