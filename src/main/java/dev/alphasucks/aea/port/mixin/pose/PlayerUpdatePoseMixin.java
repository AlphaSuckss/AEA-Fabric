package dev.alphasucks.aea.port.mixin.pose;

import dev.alphasucks.aea.port.access.ModifiedPoseHolder;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1657.class)
public abstract class PlayerUpdatePoseMixin {
   @Inject(method = "method_7318", at = @At("HEAD"), cancellable = true)
   private void apoli$preventUpdatingPose(CallbackInfo ci) {
      if (((ModifiedPoseHolder)this).apoli$getModifiedEntityPose() != null) {
         ci.cancel();
      }
   }
}
