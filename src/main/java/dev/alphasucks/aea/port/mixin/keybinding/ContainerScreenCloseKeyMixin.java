package dev.alphasucks.aea.port.mixin.keybinding;

import dev.alphasucks.aea.port.client.ScreenCloseKeys;
import net.minecraft.class_1735;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_465.class)
public abstract class ContainerScreenCloseKeyMixin {
   @Shadow
   @Nullable
   protected class_1735 field_2787;

   @Inject(method = "method_25404(III)Z", at = @At("HEAD"), cancellable = true)
   private void apoli$closeOnOpeningKey(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
      class_437 self = (class_437)(Object)this;
      if (ScreenCloseKeys.closes(self, keyCode, scanCode)) {
         if (this.field_2787 == null || !ScreenCloseKeys.isSlotKey(class_310.method_1551().field_1690, keyCode, scanCode)) {
            self.method_25419();
            cir.setReturnValue(true);
         }
      }
   }
}
