package dev.alphasucks.aea.mixin;

import dev.alphasucks.aea.port.power.ApoliPowers;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Keeps AEA's view of loaded powers in step with Apoli's registry (server reloads and client sync). */
@Mixin(value = PowerTypeRegistry.class, remap = false)
public abstract class ApacePowerTypeRegistryMixin {
    @Inject(method = {"register", "update"}, at = @At("TAIL"), remap = false)
    private static void aea$changed(CallbackInfoReturnable<?> cir) {
        ApoliPowers.invalidate();
    }

    @Inject(method = {"remove", "clear", "reset", "disable"}, at = @At("TAIL"), remap = false)
    private static void aea$removed(CallbackInfo ci) {
        ApoliPowers.invalidate();
    }
}
