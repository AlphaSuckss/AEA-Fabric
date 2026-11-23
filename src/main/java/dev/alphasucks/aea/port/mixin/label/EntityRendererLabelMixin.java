package dev.alphasucks.aea.port.mixin.label;

import dev.alphasucks.aea.port.client.ClientLabelState;
import dev.alphasucks.aea.port.client.ClientLabelState.Pick;
import dev.alphasucks.aea.port.power.builtin.ModifyLabelRenderPower.LabelMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_897;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = class_897.class, priority = 1100)
@Environment(EnvType.CLIENT)
public abstract class EntityRendererLabelMixin<T extends class_1297> {
   @Inject(method = "method_3926", at = @At("HEAD"), cancellable = true)
   private void apoli$hideLabel(T entity, class_2561 name, class_4587 pose, class_4597 buffers, int light, CallbackInfo ci) {
      Pick pick = ClientLabelState.pick(entity, class_310.method_1551().method_1560());
      if (pick != null && pick.mode() == LabelMode.HIDE_COMPLETELY) {
         ci.cancel();
      }
   }

   @ModifyVariable(method = "method_3926", at = @At("HEAD"), argsOnly = true)
   private class_2561 apoli$replaceLabel(class_2561 original, T entity, class_2561 name, class_4587 pose, class_4597 buffers, int light) {
      Pick pick = ClientLabelState.pick(entity, class_310.method_1551().method_1560());
      return pick != null && pick.text() != null ? pick.text() : original;
   }

   @ModifyVariable(method = "method_3926", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
   private boolean apoli$partialHide(boolean visible, T entity, class_2561 name, class_4587 pose, class_4597 buffers, int light) {
      if (!visible) {
         return false;
      }

      Pick pick = ClientLabelState.pick(entity, class_310.method_1551().method_1560());
      return pick == null || pick.mode() != LabelMode.HIDE_PARTIALLY;
   }
}
