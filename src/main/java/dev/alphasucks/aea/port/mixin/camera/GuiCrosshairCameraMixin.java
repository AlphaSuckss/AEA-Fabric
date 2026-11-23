package dev.alphasucks.aea.port.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.client.camera.CameraController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_329;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_329.class)
@Environment(EnvType.CLIENT)
public abstract class GuiCrosshairCameraMixin {
   @ModifyExpressionValue(method = "method_1736", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_5498;method_31034()Z"))
   private boolean apoli$hideDetachedCrosshair(boolean original) {
      return original && !CameraController.isDetached();
   }
}
