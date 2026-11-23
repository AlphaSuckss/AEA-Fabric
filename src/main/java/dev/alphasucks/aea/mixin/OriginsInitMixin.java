package dev.alphasucks.aea.mixin;

import dev.alphasucks.aea.bridge.AeaRegistrar;
import io.github.apace100.origins.Origins;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Origins 1.10.2 registers origins: power types and conditions; AEA waits for it too. */
@Mixin(value = Origins.class, remap = false)
public abstract class OriginsInitMixin {
    @Inject(method = "onInitialize", at = @At("TAIL"), remap = false)
    private void aea$afterOriginsInit(CallbackInfo ci) {
        AeaRegistrar.onOriginsInitialized();
    }
}
