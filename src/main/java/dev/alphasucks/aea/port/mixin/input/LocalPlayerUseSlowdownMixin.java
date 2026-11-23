package dev.alphasucks.aea.port.mixin.input;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.power.builtin.ModifyUseSlowdownPower;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// AEA: @ModifyExpressionValue instead of Overgrown's @ModifyConstant, so other mods' @ModifyConstant on the
// same 0.2F (Origins: Classes, etc.) can still apply instead of hard-crashing the client on launch.
@Mixin(class_746.class)
public abstract class LocalPlayerUseSlowdownMixin {
   @ModifyExpressionValue(method = "method_6007()V", at = @At(value = "CONSTANT", args = "floatValue=0.2F"))
   private float apoli$modifyUseSlowdown(float original) {
      return ModifyUseSlowdownPower.compute((class_746)(Object)this, original);
   }
}
