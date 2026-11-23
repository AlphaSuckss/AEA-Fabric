package dev.alphasucks.aea.port.mixin.firstperson;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.render.GhostArmState;
import dev.alphasucks.aea.port.client.render.ModelColorState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1007.class)
@Environment(EnvType.CLIENT)
public abstract class PlayerRendererGhostArmMixin {
   @WrapOperation(
      method = "method_23205",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1921;method_23572(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;")
   )
   private class_1921 apoli$translucentArmType(class_2960 texture, Operation<class_1921> original, @Local(argsOnly = true, ordinal = 0) class_630 arm) {
      if (GhostArmState.isActive()) {
         return class_1921.method_23580(texture);
      } else {
         return ModelColorState.alphaFor(arm) < 0.999F ? class_1921.method_23689(texture) : (class_1921)original.call(new Object[]{texture});
      }
   }

   @WrapOperation(
      method = "method_23205",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_630;method_22698(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;II)V")
   )
   private void apoli$ghostArmColor(class_630 part, class_4587 pose, class_4588 consumer, int light, int overlay, Operation<Void> original) {
      if (!GhostArmState.isActive()) {
         original.call(new Object[]{part, pose, consumer, light, overlay});
      } else {
         part.method_22699(pose, consumer, light, overlay, 1.0F, 1.0F, 1.0F, GhostArmState.alpha());
      }
   }
}
