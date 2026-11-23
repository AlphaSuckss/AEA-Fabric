package dev.alphasucks.aea.port.mixin.custom_model;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.render.ModelPartNames;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_5610;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_5610.class)
@Environment(EnvType.CLIENT)
public abstract class PartDefinitionNamesMixin {
   @ModifyReturnValue(method = "method_32112", at = @At("RETURN"))
   private class_630 apoli$recordPartNames(class_630 baked) {
      ModelPartNames.recordBaked(baked);
      return baked;
   }
}
