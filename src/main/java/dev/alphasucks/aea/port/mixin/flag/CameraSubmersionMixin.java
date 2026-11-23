package dev.alphasucks.aea.port.mixin.flag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.power.builtin.ModifyCameraSubmersionPower;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_4184.class)
@Environment(EnvType.CLIENT)
public abstract class CameraSubmersionMixin {
   @ModifyReturnValue(method = "method_19334", at = @At("RETURN"))
   private class_5636 apoli$modifySubmersion(class_5636 original) {
      return ModifyCameraSubmersionPower.remap(((class_4184)(Object)this).method_19331(), original);
   }
}
