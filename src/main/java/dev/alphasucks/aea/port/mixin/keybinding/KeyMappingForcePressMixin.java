package dev.alphasucks.aea.port.mixin.keybinding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.BlockedKeys;
import dev.alphasucks.aea.port.client.ForcedKeys;
import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_304.class)
public class KeyMappingForcePressMixin {
   @ModifyReturnValue(method = "method_1434", at = @At("RETURN"))
   private boolean apoli$forcedIsDown(boolean original) {
      class_304 self = (class_304)(Object)this;
      return BlockedKeys.blocks(self) ? false : original || ForcedKeys.isForced(self);
   }

   @Inject(method = "method_1436", at = @At("HEAD"), cancellable = true)
   private void apoli$forcedConsumeClick(CallbackInfoReturnable<Boolean> cir) {
      class_304 self = (class_304)(Object)this;
      if (BlockedKeys.blocks(self)) {
         cir.setReturnValue(false);
      } else {
         if (ForcedKeys.consumeForcedClick(self)) {
            cir.setReturnValue(true);
         }
      }
   }
}
