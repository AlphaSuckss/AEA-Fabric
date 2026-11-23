package dev.alphasucks.aea.port.mixin.pose;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.power.builtin.PosePower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4050;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_746.class)
@Environment(EnvType.CLIENT)
public abstract class LocalPlayerCrouchMixin {
   @ModifyExpressionValue(method = "method_6007", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5715()Z"))
   private boolean apoli$forceCrouching(boolean original) {
      return original || PosePower.hasEntityPose((class_746)(Object)this, class_4050.field_18081);
   }
}
