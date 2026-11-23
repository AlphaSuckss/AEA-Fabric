package dev.alphasucks.aea.port.mixin.disguise;

import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1309.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntitySwingDisguiseMixin {
   @Inject(method = "method_23667(Lnet/minecraft/class_1268;Z)V", at = @At("TAIL"))
   private void apoli$swingDisguise(class_1268 hand, boolean updateSelf, CallbackInfo ci) {
      class_1309 self = (class_1309)(Object)this;
      if (self.method_37908().field_9236) {
         ClientDisguiseManager.onSwing(self, hand);
      }
   }
}
