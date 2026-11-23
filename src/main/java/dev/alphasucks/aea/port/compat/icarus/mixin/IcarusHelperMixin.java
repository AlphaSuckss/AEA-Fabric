package dev.alphasucks.aea.port.compat.icarus.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cammiescorner.icarus.api.IcarusPlayerValues;
import dev.cammiescorner.icarus.util.IcarusHelper;
import dev.alphasucks.aea.port.compat.icarus.WingsAccess;
import dev.alphasucks.aea.port.compat.icarus.WingsPower;
import dev.alphasucks.aea.port.compat.icarus.WingsValues;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IcarusHelper.class, remap = false)
public class IcarusHelperMixin {
   @ModifyReturnValue(method = "getConfigValues", at = @At("RETURN"))
   private static IcarusPlayerValues apoli$injectWingsConfig(IcarusPlayerValues original, class_1309 entity) {
      WingsPower.Config cfg = WingsAccess.get(entity);
      return cfg != null ? WingsValues.of(cfg, original) : original;
   }
}
