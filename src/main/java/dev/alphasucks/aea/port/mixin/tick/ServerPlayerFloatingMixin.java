package dev.alphasucks.aea.port.mixin.tick;

import dev.alphasucks.aea.port.tick.TickRates;
import net.minecraft.class_3222;
import net.minecraft.class_3244;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3244.class)
public abstract class ServerPlayerFloatingMixin {
   @Shadow
   private boolean field_14131;
   @Shadow
   private boolean field_14129;
   @Shadow
   public class_3222 field_14140;

   @Inject(method = "method_18784()V", at = @At("HEAD"))
   private void apoli$forgiveGatedFloat(CallbackInfo ci) {
      if (!TickRates.idle()) {
         if (!TickRates.shouldTickEntity(this.field_14140)) {
            this.field_14131 = false;
            this.field_14129 = false;
         }
      }
   }
}
