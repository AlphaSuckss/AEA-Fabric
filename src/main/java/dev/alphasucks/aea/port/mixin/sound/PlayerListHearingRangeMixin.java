package dev.alphasucks.aea.port.mixin.sound;

import dev.alphasucks.aea.port.power.builtin.ModifyHearingRangePower;
import java.util.List;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2596;
import net.minecraft.class_2765;
import net.minecraft.class_2767;
import net.minecraft.class_3222;
import net.minecraft.class_3324;
import net.minecraft.class_5321;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_3324.class)
public abstract class PlayerListHearingRangeMixin {
   @Shadow
   @Final
   private List<class_3222> field_14351;

   @Inject(method = "method_14605(Lnet/minecraft/class_1657;DDDDLnet/minecraft/class_5321;Lnet/minecraft/class_2596;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$applyHearingRange(
      @Nullable class_1657 except, double x, double y, double z, double radius, class_5321<class_1937> dimension, class_2596<?> packet, CallbackInfo ci
   ) {
      if (ModifyHearingRangePower.inUse()) {
         if (packet instanceof class_2767 || packet instanceof class_2765) {
            ci.cancel();
            int i = 0;

            for (int n = this.field_14351.size(); i < n; i++) {
               class_3222 listener = (class_3222)this.field_14351.get(i);
               if (listener != except && listener.method_37908().method_27983() == dimension) {
                  double range = ModifyHearingRangePower.soundRange(listener, radius);
                  if (!(range <= 0.0)) {
                     double dx = x - listener.method_23317();
                     double dy = y - listener.method_23318();
                     double dz = z - listener.method_23321();
                     if (dx * dx + dy * dy + dz * dz < range * range) {
                        listener.field_13987.method_14364(packet);
                     }
                  }
               }
            }
         }
      }
   }
}
