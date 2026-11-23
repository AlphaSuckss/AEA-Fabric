package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.ModifyBlockStuckSpeedHandler;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1297.class)
public abstract class EntityStuckSpeedMixin {
   private static final String MAKE_STUCK_IN_BLOCK = "makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V";

   @Inject(method = "method_5844(Lnet/minecraft/class_2680;Lnet/minecraft/class_243;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$skipStuckEntirely(class_2680 state, class_243 original, CallbackInfo ci) {
      class_1297 self = (class_1297)(Object)this;
      if (ModifyBlockStuckSpeedHandler.fullyFree(ModifyBlockStuckSpeedHandler.modify(self, state, original))) {
         self.method_38785();
         ci.cancel();
      }
   }

   @ModifyVariable(method = "method_5844(Lnet/minecraft/class_2680;Lnet/minecraft/class_243;)V", at = @At("HEAD"), argsOnly = true)
   private class_243 apoli$modifyStuckSpeed(class_243 original, class_2680 state) {
      return ModifyBlockStuckSpeedHandler.modify((class_1297)(Object)this, state, original);
   }

   @ModifyReturnValue(method = "method_23326()F", at = @At("RETURN"))
   private float apoli$modifyBlockSpeedFactor(float original) {
      return ModifyBlockStuckSpeedHandler.modifySpeedFactor((class_1297)(Object)this, original);
   }
}
