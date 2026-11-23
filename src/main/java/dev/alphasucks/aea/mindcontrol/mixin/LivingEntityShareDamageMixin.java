package dev.alphasucks.aea.mindcontrol.mixin;

import dev.alphasucks.aea.mindcontrol.MindControl;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** {@code share_damage}: after a controlled entity is hurt, part of the damage (before its armour) hits the controller. */
@Mixin(class_1309.class)
public abstract class LivingEntityShareDamageMixin {
    @Inject(method = "method_5643", at = @At("RETURN"))
    private void aea$shareDamage(class_1282 source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && !((class_1309) (Object) this).method_37908().field_9236) {
            MindControl.onDamageTaken((class_1309) (Object) this, source, amount);
        }
    }
}
