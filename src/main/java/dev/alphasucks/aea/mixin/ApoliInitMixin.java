package dev.alphasucks.aea.mixin;

import dev.alphasucks.aea.bridge.AeaRegistrar;
import io.github.apace100.apoli.Apoli;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** AEA registers its ported types only after Apoli 2.9.2 registered its own. */
@Mixin(value = Apoli.class, remap = false)
public abstract class ApoliInitMixin {
    @Inject(method = "onInitialize", at = @At("TAIL"), remap = false)
    private void aea$afterApoliInit(CallbackInfo ci) {
        AeaRegistrar.onApoliInitialized();
    }
}
