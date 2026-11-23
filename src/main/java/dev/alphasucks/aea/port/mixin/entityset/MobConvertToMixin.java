package dev.alphasucks.aea.port.mixin.entityset;

import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1308.class)
public abstract class MobConvertToMixin {
   @Inject(method = "method_29243(Lnet/minecraft/class_1299;Z)Lnet/minecraft/class_1308;", at = @At("RETURN"))
   private void apoli$onConvertTo(class_1299<?> entityType, boolean bl, CallbackInfoReturnable<class_1308> cir) {
      class_1308 result = (class_1308)cir.getReturnValue();
      if (result != null) {
         class_1308 self = (class_1308)(Object)this;
         if (!self.method_37908().method_8608()) {
            EntitySetPower.onEntityConverted(self.method_5667(), result.method_5667());
         }
      }
   }
}
