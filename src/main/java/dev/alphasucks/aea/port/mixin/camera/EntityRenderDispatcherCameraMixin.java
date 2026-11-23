package dev.alphasucks.aea.port.mixin.camera;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.camera.CameraController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_4604;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_898.class)
@Environment(EnvType.CLIENT)
public abstract class EntityRenderDispatcherCameraMixin {
   @ModifyReturnValue(method = "method_3950", at = @At("RETURN"))
   private boolean apoli$hideCameraAnchor(boolean original, class_1297 entity, class_4604 frustum, double cameraX, double cameraY, double cameraZ) {
      return original && !CameraController.hides(entity);
   }
}
