package dev.alphasucks.aea.port.mixin.input;

import dev.alphasucks.aea.port.client.ScrollWatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_312;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
@Environment(EnvType.CLIENT)
public abstract class MouseHandlerScrollMixin {
   @Inject(method = "method_1598(JDD)V", at = @At("HEAD"), cancellable = true)
   private void apoli$scrollPower(long window, double xOffset, double yOffset, CallbackInfo ci) {
      if (yOffset != 0.0) {
         if (window == class_310.method_1551().method_22683().method_4490()) {
            if (ScrollWatcher.onScroll(yOffset)) {
               ci.cancel();
            }
         }
      }
   }
}
