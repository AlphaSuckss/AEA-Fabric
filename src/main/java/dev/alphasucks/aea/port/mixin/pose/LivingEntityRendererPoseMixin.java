package dev.alphasucks.aea.port.mixin.pose;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.render.ForcedPoseShapes;
import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.power.builtin.PosePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_1309;
import net.minecraft.class_4050;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_7833;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererPoseMixin {
   private static final String SETUP_ROTATIONS = "setupRotations(Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V";
   private static final String RENDER = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V";
   @Shadow
   protected class_583<?> field_4737;

   @ModifyExpressionValue(
      method = "method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_6123()Z")
   )
   private boolean apoli$forceRiptidePose(boolean original, @Local(argsOnly = true) class_1309 entity) {
      return ModelPartAnimator.overridesPose(entity) ? false : original || PosePower.hasEntityPose(entity, class_4050.field_18080);
   }

   @ModifyExpressionValue(
      method = "method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1309;method_41328(Lnet/minecraft/class_4050;)Z")
   )
   private boolean apoli$neutralizeSleepRotation(boolean original, @Local(argsOnly = true) class_1309 entity) {
      return original && !ModelPartAnimator.overridesPose(entity);
   }

   @ModifyExpressionValue(
      method = "method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_1309;field_6213:I")
   )
   private int apoli$forceDyingPose(int original, @Local(argsOnly = true) class_1309 entity) {
      int forced = EntityPoses.deathTime(entity, original);
      return forced != original && !ModelPartAnimator.overridesPose(entity) ? forced : original;
   }

   @Inject(method = "method_4058(Lnet/minecraft/class_1309;Lnet/minecraft/class_4587;FFF)V", at = @At("TAIL"))
   private void apoli$forcedPoseFrame(class_1309 entity, class_4587 poseStack, float bob, float yBodyRot, float partialTick, CallbackInfo ci) {
      class_4050 pose = EntityPoses.forced(entity);
      if (pose != null && EntityPoses.movesBody(pose) && this.field_4737 instanceof class_572) {
         if (!ModelPartAnimator.overridesPose(entity)) {
            float progress = EntityPoses.progress(entity, partialTick);
            float pitch = EntityPoses.bodyPitch(pose) * progress;
            if (pitch != 0.0F) {
               poseStack.method_22907(class_7833.field_40714.rotationDegrees(pitch));
            }

            float modelScale = (Object)this instanceof class_1007 ? 0.9375F : 1.0F;
            poseStack.method_46416(0.0F, -EntityPoses.bodyDrop(pose) * progress / 16.0F * modelScale, 0.0F);
         }
      }
   }

   @Inject(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(value = "INVOKE", shift = Shift.AFTER, target = "Lnet/minecraft/class_583;method_2819(Lnet/minecraft/class_1297;FFFFF)V")
   )
   private void apoli$shapeMobPose(class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      if (this.field_4737 instanceof class_572<?> humanoid && !(humanoid instanceof class_591)) {
         class_4050 pose = EntityPoses.forced(entity);
         if (pose != null && EntityPoses.shapes(pose) && !ModelPartAnimator.overridesPose(entity)) {
            ForcedPoseShapes.apply(humanoid, entity, pose, entity.field_6012 + partialTick);
         }
      }
   }
}
