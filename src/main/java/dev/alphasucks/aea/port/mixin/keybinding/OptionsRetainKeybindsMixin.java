package dev.alphasucks.aea.port.mixin.keybinding;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.alphasucks.aea.port.client.DynamicKeyMappingManager;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_315;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_315.class)
public abstract class OptionsRetainKeybindsMixin {
   @Shadow
   @Final
   private File field_1897;

   @Inject(method = "method_1640", at = @At("HEAD"))
   private void apoli$readRetainedKeybinds(CallbackInfo ci, @Share("apoli$retained") LocalRef<Map<String, String>> retained) {
      retained.set(DynamicKeyMappingManager.retainedKeyLines(this.field_1897, ((class_315)(Object)this).field_1839));
   }

   @Inject(
      method = "method_1640",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_315;method_33666(Lnet/minecraft/class_315$class_5823;)V", shift = Shift.AFTER)
   )
   private void apoli$writeRetainedKeybinds(CallbackInfo ci, @Local PrintWriter writer, @Share("apoli$retained") LocalRef<Map<String, String>> retained) {
      Map<String, String> lines = (Map<String, String>)retained.get();
      if (lines != null) {
         for (Entry<String, String> line : lines.entrySet()) {
            writer.print((String)line.getKey());
            writer.print(':');
            writer.println((String)line.getValue());
         }
      }
   }
}
