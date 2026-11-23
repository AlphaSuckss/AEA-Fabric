package dev.alphasucks.aea.port.mixin.teleport;

import dev.alphasucks.aea.port.power.builtin.PreventTeleportHandler;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_2709;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3222.class)
public abstract class ServerPlayerTeleportMixin {
   @Inject(method = "method_48105(Lnet/minecraft/class_3218;DDDLjava/util/Set;FF)Z", at = @At("HEAD"), cancellable = true)
   private void apoli$preventTeleport(
      class_3218 level, double x, double y, double z, Set<class_2709> flags, float yaw, float pitch, CallbackInfoReturnable<Boolean> cir
   ) {
      class_3222 self = (class_3222)(Object)this;
      if (PreventTeleportHandler.prevented(self, level != self.method_37908())) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "method_5859(DDD)V", at = @At("HEAD"), cancellable = true)
   private void apoli$preventSimpleTeleport(double x, double y, double z, CallbackInfo ci) {
      if (PreventTeleportHandler.prevented((class_3222)(Object)this, false)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_14251(Lnet/minecraft/class_3218;DDDFF)V", at = @At("HEAD"), cancellable = true)
   private void apoli$preventDimensionTeleport(class_3218 level, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
      class_3222 self = (class_3222)(Object)this;
      if (PreventTeleportHandler.prevented(self, level != self.method_37908())) {
         ci.cancel();
      }
   }

   @Inject(method = "method_5731(Lnet/minecraft/class_3218;)Lnet/minecraft/class_1297;", at = @At("HEAD"), cancellable = true)
   private void apoli$preventDimensionChange(class_3218 destination, CallbackInfoReturnable<class_1297> cir) {
      class_3222 self = (class_3222)(Object)this;
      if (PreventTeleportHandler.prevented(self, true)) {
         cir.setReturnValue(self);
      }
   }
}
