package dev.alphasucks.aea.port.mixin.swing;

import dev.alphasucks.aea.port.client.render.GhostArmTrails;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import net.minecraft.class_897;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public abstract class LivingEntityRendererGhostArmsMixin {
   @Shadow
   protected abstract boolean method_4056(class_1309 var1);

   @Inject(
      method = "method_4054(Lnet/minecraft/class_1309;FFLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22909()V")
   )
   private void apoli$thirdPersonGhostArms(
      class_1309 entity, float yaw, float partialTick, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci
   ) {
      class_922<?, ?> self = (class_922<?, ?>)(Object)this;
      if (self.method_4038() instanceof class_572<?> model && !(model.field_3447 <= 0.0F)) {
         if (!entity.method_7325() && this.method_4056(entity)) {
            GhostArmTrails.render(model, entity, poseStack, buffers, ((class_897)(Object)this).method_3931(entity), light);
         }
      }
   }
}
