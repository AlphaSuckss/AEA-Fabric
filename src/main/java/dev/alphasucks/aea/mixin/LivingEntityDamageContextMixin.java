package dev.alphasucks.aea.mixin;

import dev.alphasucks.aea.bridge.AeaContext;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Lets ported damage conditions see who is being damaged (Apoli 2.9.2 only passes source + amount). */
@Mixin(class_1309.class)
public abstract class LivingEntityDamageContextMixin {
    @Inject(method = "method_5643", at = @At("HEAD"))
    private void aea$pushTarget(class_1282 source, float amount, CallbackInfoReturnable<Boolean> cir) {
        AeaContext.pushDamageTarget((class_1309) (Object) this);
    }

    @Inject(method = "method_5643", at = @At("RETURN"))
    private void aea$popTarget(class_1282 source, float amount, CallbackInfoReturnable<Boolean> cir) {
        AeaContext.popDamageTarget();
    }
}
