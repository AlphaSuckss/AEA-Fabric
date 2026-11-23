package dev.alphasucks.aea.port.mixin.disguise;

import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2663;
import net.minecraft.class_634;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_634.class)
@Environment(EnvType.CLIENT)
public abstract class ClientPacketListenerDisguiseEventMixin {
   @Shadow
   private class_638 field_3699;

   @Inject(method = "method_11148", at = @At("TAIL"))
   private void apoli$forwardEventToDisguise(class_2663 packet, CallbackInfo ci) {
      if (this.field_3699 != null) {
         class_1297 entity = packet.method_11469(this.field_3699);
         if (entity != null) {
            ClientDisguiseManager.onEntityEvent(entity, packet.method_11470());
         }
      }
   }
}
