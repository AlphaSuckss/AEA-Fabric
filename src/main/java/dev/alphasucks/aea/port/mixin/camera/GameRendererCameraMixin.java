package dev.alphasucks.aea.port.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.camera.CameraController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_757;
import net.minecraft.class_7833;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
@Environment(EnvType.CLIENT)
public abstract class GameRendererCameraMixin {
   @ModifyReturnValue(method = "method_3196", at = @At("RETURN"))
   private double apoli$cameraFov(double original, class_4184 camera, float partialTick, boolean useFovSetting) {
      return !useFovSetting ? original : CameraController.fov(original, partialTick);
   }

   @Inject(
      method = "method_3188",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_4184;method_19321(Lnet/minecraft/class_1922;Lnet/minecraft/class_1297;ZZF)V",
         shift = Shift.AFTER
      )
   )
   private void apoli$cameraRoll(float partialTick, long nanos, class_4587 pose, CallbackInfo ci) {
      float roll = CameraController.roll();
      if (roll != 0.0F) {
         pose.method_22907(class_7833.field_40718.rotationDegrees(roll));
      }
   }

   @ModifyExpressionValue(method = "method_3172", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_5498;method_31034()Z", ordinal = 0))
   private boolean apoli$hideHand(boolean original) {
      return original && !CameraController.hidesHand();
   }

   @ModifyExpressionValue(method = "method_3172", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_5498;method_31034()Z", ordinal = 1))
   private boolean apoli$hideScreenEffects(boolean original) {
      return original && !CameraController.isDetached();
   }

   @Inject(method = "method_3186", at = @At("HEAD"), cancellable = true)
   private void apoli$cinematicBobView(class_4587 pose, float partialTick, CallbackInfo ci) {
      if (CameraController.suppressesBob()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_3198", at = @At("HEAD"), cancellable = true)
   private void apoli$cinematicBobHurt(class_4587 pose, float partialTick, CallbackInfo ci) {
      if (CameraController.suppressesBob()) {
         ci.cancel();
      }
   }
}
