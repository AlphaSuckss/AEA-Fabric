package dev.alphasucks.aea.port.mixin.label;

import dev.alphasucks.aea.port.entity.DisplayNameOverrides;
import net.minecraft.class_1297;
import net.minecraft.class_2556;
import net.minecraft.class_2561;
import net.minecraft.class_5321;
import net.minecraft.class_2556.class_7602;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_2556.class)
public abstract class ChatTypeNameMixin {
   @Inject(
      method = "method_44832(Lnet/minecraft/class_5321;Lnet/minecraft/class_1297;)Lnet/minecraft/class_2556$class_7602;",
      at = @At("HEAD"),
      cancellable = true
   )
   private static void apoli$overrideChatName(class_5321<class_2556> key, class_1297 entity, CallbackInfoReturnable<class_7602> cir) {
      class_2561 override = DisplayNameOverrides.chatNameFor(entity);
      if (override != null) {
         cir.setReturnValue(class_2556.method_44834(key, entity.method_37908().method_30349(), override));
      }
   }
}
