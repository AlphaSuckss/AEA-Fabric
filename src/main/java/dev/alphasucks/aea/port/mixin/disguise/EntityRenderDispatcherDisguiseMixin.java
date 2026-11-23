package dev.alphasucks.aea.port.mixin.disguise;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.entity.disguise.DisguiseData;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_640;
import net.minecraft.class_742;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_898.class, priority = 900)
@Environment(EnvType.CLIENT)
public abstract class EntityRenderDispatcherDisguiseMixin {
   @Shadow
   private Map<String, class_897<? extends class_1657>> field_4687;

   @Inject(method = "method_3954", at = @At("HEAD"), cancellable = true)
   private <E extends class_1297> void apoli$renderDisguise(
      E entity, double x, double y, double z, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light, CallbackInfo ci
   ) {
      DisguiseData data = ClientDisguiseManager.get(entity.method_5628());
      if (data != null) {
         if (!data.isPlayerDisguise() || !(entity instanceof class_1657)) {
            class_1297 dummy = ClientDisguiseManager.syncedDummy(entity.method_5628(), entity);
            if (dummy != null) {
               ClientDisguiseManager.ensureCustomModelLayer((class_898)(Object)this, dummy);
               class_1297 previous = ClientDisguiseManager.beginDisguiseRender(entity);

               try {
                  ((class_898)(Object)this).method_3954(dummy, x, y, z, yaw, partialTick, pose, buffers, light);
               } finally {
                  ClientDisguiseManager.endDisguiseRender(previous);
               }

               ci.cancel();
            }
         }
      }
   }

   @ModifyReturnValue(method = "method_3953", at = @At("RETURN"))
   private class_897<?> apoli$disguiseRenderer(class_897<?> original, class_1297 entity) {
      if (!(entity instanceof class_742)) {
         return original;
      }

      class_640 info = ClientDisguiseManager.playerInfo(entity.method_5628());
      if (info == null) {
         return original;
      }

      class_897<? extends class_1657> renderer = (class_897<? extends class_1657>)this.field_4687.get(info.method_2977());
      return renderer == null ? original : renderer;
   }
}
