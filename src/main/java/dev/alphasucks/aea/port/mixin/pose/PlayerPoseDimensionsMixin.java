package dev.alphasucks.aea.port.mixin.pose;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.power.builtin.PosePower;
import net.minecraft.class_1657;
import net.minecraft.class_4048;
import net.minecraft.class_4050;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1657.class)
public abstract class PlayerPoseDimensionsMixin {
   @ModifyReturnValue(method = "method_18377", at = @At("RETURN"))
   private class_4048 apoli$forcedPoseDimensions(class_4048 original, class_4050 pose) {
      if (EntityPoses.resizesPlayer(pose) && PosePower.hasEntityPose((class_1657)(Object)this, pose)) {
         class_4048 forced = EntityPoses.playerDimensions(pose);
         return forced != null ? forced : original;
      } else {
         return original;
      }
   }

   @ModifyReturnValue(method = "method_18394", at = @At("RETURN"))
   private float apoli$forcedPoseEyeHeight(float original, class_4050 pose, class_4048 dimensions) {
      if (EntityPoses.resizesPlayer(pose) && PosePower.hasEntityPose((class_1657)(Object)this, pose)) {
         float forced = EntityPoses.playerEyeHeight(pose);
         return forced >= 0.0F ? forced : original;
      } else {
         return original;
      }
   }
}
