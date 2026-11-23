package dev.alphasucks.aea.port.mixin.power;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.alphasucks.aea.port.power.builtin.ModifyFogInterpolator;
import dev.alphasucks.aea.port.power.builtin.ModifyFogHandler.FogData;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(class_761.class)
public class LevelRendererSkyMixin {
   @WrapMethod(method = "method_3257")
   void apoli$preventRenderSky(class_4587 poseStack, Matrix4f matrix4f, float f, class_4184 camera, boolean bl, Runnable runnable, Operation<Void> original) {
      FogData fog = ModifyFogInterpolator.getCurrent(null, null, null);
      if (fog.color.equals(FogData.UNSET_COLOR)) {
         original.call(new Object[]{poseStack, matrix4f, f, camera, bl, runnable});
      }
   }
}
