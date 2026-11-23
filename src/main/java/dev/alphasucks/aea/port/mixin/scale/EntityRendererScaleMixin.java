package dev.alphasucks.aea.port.mixin.scale;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.client.render.ScaleRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_897.class)
@Environment(EnvType.CLIENT)
public abstract class EntityRendererScaleMixin {
   @Inject(method = "method_3926", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = Shift.AFTER))
   private void apoli$nameTagInWorldUnits(class_1297 entity, class_2561 name, class_4587 poseStack, class_4597 buffers, int light, CallbackInfo ci) {
      if (!((Object)this instanceof class_922)) {
         ScaleRender.undoOuterScale(entity, poseStack, class_310.method_1551().method_1488());
      }
   }

   @ModifyExpressionValue(method = "method_3926", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_51152()F"))
   private float apoli$nameTagFollowsModel(float offset, @Local(argsOnly = true) class_1297 entity) {
      return ScaleRender.nameTagOffset(entity, offset, class_310.method_1551().method_1488());
   }
}
