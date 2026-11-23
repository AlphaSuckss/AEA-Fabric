package dev.alphasucks.aea.port.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.client.camera.CameraController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(class_312.class)
@Environment(EnvType.CLIENT)
public abstract class MouseHandlerZoomMixin {
   @ModifyExpressionValue(method = "method_1606", at = @At(value = "FIELD", target = "Lnet/minecraft/class_315;field_1914:Z"))
   private boolean apoli$zoomSmoothing(boolean original) {
      return original || CameraController.smoothsMouse();
   }

   @ModifyArg(method = "method_1606", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5872(DD)V"), index = 0)
   private double apoli$zoomSensitivityYaw(double yaw) {
      return yaw * CameraController.sensitivity();
   }

   @ModifyArg(method = "method_1606", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5872(DD)V"), index = 1)
   private double apoli$zoomSensitivityPitch(double pitch) {
      return pitch * CameraController.sensitivity();
   }
}
