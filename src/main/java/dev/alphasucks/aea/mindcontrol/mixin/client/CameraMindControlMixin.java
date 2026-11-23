package dev.alphasucks.aea.mindcontrol.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.mindcontrol.client.MindControlKeys;
import net.minecraft.class_1297;
import net.minecraft.class_4184;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** While controlling with {@code look}, the camera turns with your mouse straight away instead of waiting for the victim. */
@Mixin(class_4184.class)
public abstract class CameraMindControlMixin {
    @WrapOperation(method = "method_19321", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4184;method_19325(FF)V", ordinal = 0))
    private void aea$controllerLook(class_4184 camera, float yaw, float pitch, Operation<Void> original,
                                    @Local(argsOnly = true) class_1297 focused, @Local(argsOnly = true) float tickDelta) {
        if (MindControlKeys.overridesCamera(focused)) {
            original.call(camera, MindControlKeys.cameraYaw(tickDelta), MindControlKeys.cameraPitch(tickDelta));
        } else {
            original.call(camera, yaw, pitch);
        }
    }
}
