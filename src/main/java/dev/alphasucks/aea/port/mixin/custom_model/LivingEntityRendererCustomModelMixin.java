package dev.alphasucks.aea.port.mixin.custom_model;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.render.CustomModelManager;
import dev.alphasucks.aea.port.client.render.DynamicTextures;
import dev.alphasucks.aea.port.client.render.LivingCustomModelLayer;
import dev.alphasucks.aea.port.client.summon.CloneRenderer;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import dev.alphasucks.aea.port.mixin.disguise.LivingEntityRendererAddLayerAccessor;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1007;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_583;
import net.minecraft.class_922;
import net.minecraft.class_5617.class_5618;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererCustomModelMixin<T extends class_1309, M extends class_583<T>> {
   @Inject(method = "<init>", at = @At("TAIL"))
   private void apoli$addCustomModelLayer(class_5618 context, M model, float shadowRadius, CallbackInfo ci) {
      Object self = this;
      if (!(self instanceof class_1007) && !(self instanceof CloneRenderer)) {
         class_922<T, M> renderer = (class_922<T, M>)self;
         ((LivingEntityRendererAddLayerAccessor)self).apoli$addLayer(new LivingCustomModelLayer<>(renderer));
      }
   }

   @ModifyExpressionValue(
      method = "method_24302",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_922;method_3931(Lnet/minecraft/class_1297;)Lnet/minecraft/class_2960;")
   )
   private class_2960 apoli$replaceTexture(class_2960 original, T entity) {
      if (entity instanceof class_1657) {
         return original;
      }

      Config cfg = CustomModelRenderPower.firstReplace(entity);
      return cfg == null ? original : DynamicTextures.resolve(cfg.wide(), entity);
   }

   @ModifyReturnValue(method = "method_24302", at = @At("RETURN"))
   private class_1921 apoli$hideReplacedModel(class_1921 original, T entity, boolean bodyVisible, boolean translucent, boolean glowing) {
      if (original != null && !(entity instanceof class_1657) && !(entity instanceof MinionEntity)) {
         return CustomModelRenderPower.replacesModel(entity, id -> CustomModelManager.get(id) != null) ? null : original;
      } else {
         return original;
      }
   }
}
