package dev.alphasucks.aea.mobai.mixin;

import dev.alphasucks.aea.mobai.MobAiPower;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_3218;
import net.minecraft.class_4095;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** aea:mob_ai "remove": ["brain"] (or "all"): a brain-based mob's brain stops ticking while the power is active. */
@Mixin(class_4095.class)
public abstract class BrainMixin {
    @Inject(method = "method_19542", at = @At("HEAD"), cancellable = true)
    private void aea$suppress(class_3218 world, class_1309 entity, CallbackInfo ci) {
        if (entity instanceof class_1308 mob && MobAiPower.suppressesBrain(mob)) {
            ci.cancel();
        }
    }
}
