package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.client.ClientTickRates;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1665;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({class_1297.class, class_1309.class, class_1665.class})
@Environment(EnvType.CLIENT)
public abstract class EntityLerpFollowMixin {
   @Inject(method = "method_5759(DDDFFIZ)V", at = @At("HEAD"), cancellable = true)
   private void apoli$followServer(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport, CallbackInfo ci) {
      if (!ClientTickRates.idle()) {
         class_1297 self = (class_1297)(Object)this;
         if (self.method_37908().method_8608()) {
            if (ClientTickRates.captureLerp(self, x, y, z, yRot, xRot)) {
               ci.cancel();
            }
         }
      }
   }
}
