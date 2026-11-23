package dev.alphasucks.aea.port.compat.icarus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.cammiescorner.icarus.client.IcarusClient;
import dev.alphasucks.aea.port.compat.icarus.IcarusFieldHooks;
import dev.alphasucks.aea.port.compat.icarus.WingsAccess;
import dev.alphasucks.aea.port.compat.icarus.WingsPower;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IcarusClient.class, remap = false)
public class IcarusClientMixin {
   @ModifyReturnValue(method = "getWingsForRendering", at = @At("RETURN"))
   private static class_1799 apoli$renderWings(class_1799 original, class_1309 entity) {
      IcarusFieldHooks.install();
      if (original.method_7960()) {
         WingsPower.Config cfg = WingsAccess.get(entity);
         if (cfg != null) {
            return cfg.wingsType();
         }
      }

      return original;
   }
}
