package dev.alphasucks.aea.port.mixin.command;

import dev.alphasucks.aea.port.command.ApoliSelectorOptions;
import net.minecraft.class_2306;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2306.class)
public class EntitySelectorOptionsMixin {
   @Inject(method = "method_9960", at = @At("TAIL"))
   private static void apoli$registerSelectorOptions(CallbackInfo ci) {
      ApoliSelectorOptions.bootstrap();
   }
}
