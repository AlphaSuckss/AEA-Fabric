package dev.alphasucks.aea.port.mixin.power;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.client.render.ClientRenderFlags;
import dev.alphasucks.aea.port.power.builtin.PreventEntitySelectionPower;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_757;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_757.class)
public class PreventEntitySelectionMixin {
   @ModifyExpressionValue(
      method = "method_18144(Lnet/minecraft/class_1297;)Z",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1297;method_5863()Z")
   )
   private static boolean apoli$preventEntitySelection(boolean original, class_1297 entity) {
      if (original && ClientRenderFlags.has(2)) {
         class_746 viewer = class_310.method_1551().field_1724;
         return viewer == null ? true : !PreventEntitySelectionPower.prevents(viewer, entity);
      } else {
         return original;
      }
   }
}
