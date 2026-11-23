package dev.alphasucks.aea.port.mixin.keybinding;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.port.client.DynamicKeyMappingManager;
import net.minecraft.class_1078;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1078.class)
public abstract class ClientLanguageKeybindNameMixin {
   @ModifyReturnValue(method = "method_4679(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At("RETURN"))
   private String apoli$dynamicKeybindName(String original, String key, String fallback) {
      if (original == fallback && DynamicKeyMappingManager.hasNameHints()) {
         String hint = DynamicKeyMappingManager.nameHint(key);
         return hint != null ? hint : original;
      } else {
         return original;
      }
   }
}
