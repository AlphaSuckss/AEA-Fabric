package dev.alphasucks.aea.port.mixin.disguise;

import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_7923;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_897.class)
@Environment(EnvType.CLIENT)
public abstract class EntityRendererDisguiseNameMixin<T extends class_1297> {
   @Inject(method = "method_3926", at = @At("HEAD"), cancellable = true)
   private void apoli$hideDisguiseName(T entity, class_2561 name, class_4587 pose, class_4597 buffers, int light, CallbackInfo ci) {
      if (ClientDisguiseManager.hidesName(entity)) {
         ci.cancel();
      }
   }

   @ModifyVariable(method = "method_3926", at = @At("HEAD"), argsOnly = true)
   private class_2561 apoli$disguiseName(class_2561 original, T entity, class_2561 name, class_4587 pose, class_4597 buffers, int light) {
      DisguiseData data = ClientDisguiseManager.get(entity.method_5628());
      if (data == null) {
         return original;
      }

      if (data.name().isPresent()) {
         String disguiseName = (String)data.name().get();
         return disguiseName.isEmpty() ? original : class_2561.method_43470(disguiseName);
      }

      if (data.isPlayerDisguise()) {
         return original;
      }

      class_1299<?> type = class_7923.field_41177.method_10223(data.entityTypeId());
      return type != null ? class_2561.method_43471(type.method_5882()) : original;
   }
}
