package dev.alphasucks.aea.mobai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.mobai.MobAiPower;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * aea:mob_ai "never_target": hooked into canTarget, which target goals, revenge, target predicates and brain
 * sensors all consult, so a matching entity is never picked (instead of being picked and dropped).
 */
@Mixin(class_1309.class)
public abstract class LivingEntityCanTargetMixin {
    @ModifyReturnValue(method = "method_18395", at = @At("RETURN"))
    private boolean aea$neverTarget(boolean original, @Local(argsOnly = true) class_1309 target) {
        if (original && target != null && (Object) this instanceof class_1308 mob && MobAiPower.neverTargets(mob, target)) {
            return false;
        }
        return original;
    }
}
