package dev.alphasucks.aea.port.mixin.custom_model;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.render.DynamicTextures;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3887;
import net.minecraft.class_583;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_3887.class)
@Environment(EnvType.CLIENT)
public abstract class RenderLayerTextureMixin<T extends class_1297, M extends class_583<T>> {
   @ModifyReturnValue(method = "method_23194", at = @At("RETURN"))
   private class_2960 apoli$replaceLayerTexture(class_2960 original, T entity) {
      if (entity instanceof class_1309 living && !(entity instanceof class_1657)) {
         Config cfg = CustomModelRenderPower.firstReplace(living);
         return cfg == null ? original : DynamicTextures.resolve(cfg.wide(), living);
      } else {
         return original;
      }
   }
}
