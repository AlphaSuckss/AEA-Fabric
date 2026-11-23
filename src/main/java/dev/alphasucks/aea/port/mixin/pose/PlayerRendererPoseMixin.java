package dev.alphasucks.aea.port.mixin.pose;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.access.ModifiedPoseHolder;
import dev.alphasucks.aea.port.client.ArmPoseReferenceClient;
import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.data.ArmPoseReference;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.power.builtin.PosePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_1268;
import net.minecraft.class_4050;
import net.minecraft.class_742;
import net.minecraft.class_572.class_573;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1007.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerRendererPoseMixin {
   private static final String SETUP_ROTATIONS = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V";
   private static final String RENDER_OFFSET = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;";

   @ModifyReturnValue(method = "method_4210", at = @At("RETURN"))
   private static class_573 apoli$overrideArmPose(class_573 original, class_742 player, class_1268 hand) {
      if (player instanceof ModifiedPoseHolder holder) {
         ArmPoseReference ref = holder.apoli$getModifiedArmPose(hand);
         return ref != null ? ArmPoseReferenceClient.toVanilla(ref) : original;
      } else {
         return original;
      }
   }

   @ModifyExpressionValue(
      method = "method_4212(Lnet/minecraft/class_742;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6128()Z")
   )
   private boolean apoli$forceFallFlyingPose(boolean original, @Local(argsOnly = true) class_742 player) {
      return ModelPartAnimator.overridesPose(player) ? false : original || PosePower.hasEntityPose(player, class_4050.field_18077);
   }

   @ModifyExpressionValue(
      method = "method_4212(Lnet/minecraft/class_742;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6003()I")
   )
   private int apoli$forcedFallFlyingTicks(int original, @Local(argsOnly = true) class_742 player) {
      return EntityPoses.fallFlyingTicks(player, original);
   }

   @ModifyExpressionValue(
      method = "method_4212(Lnet/minecraft/class_742;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6123()Z")
   )
   private boolean apoli$forceRiptidePose(boolean original, @Local(argsOnly = true) class_742 player) {
      return original || PosePower.hasEntityPose(player, class_4050.field_18080);
   }

   @ModifyExpressionValue(
      method = "method_4212(Lnet/minecraft/class_742;Lnet/minecraft/class_4587;FFF)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_6024(F)F")
   )
   private float apoli$neutralizeSwimRotation(float original, @Local(argsOnly = true) class_742 player) {
      return ModelPartAnimator.overridesPose(player) ? 0.0F : original;
   }

   @ModifyExpressionValue(
      method = "method_23206(Lnet/minecraft/class_742;F)Lnet/minecraft/class_243;",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_18276()Z")
   )
   private boolean apoli$neutralizeCrouchOffset(boolean original, @Local(argsOnly = true) class_742 player) {
      return original && !ModelPartAnimator.overridesPose(player);
   }
}
