package dev.alphasucks.aea.hitbox.mixin;

import dev.alphasucks.aea.hitbox.HitboxPart;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A melee hit on an {@code aea:hitbox_part} box is carried out on the box's owner, so damage, knockback, enchantments,
 * weapon wear and statistics all behave as if the owner had been hit directly. The server player class has its own
 * attack method (a spectator's click starts spectating the target), so it gets the same treatment.
 */
@Mixin({class_1657.class, class_3222.class})
public abstract class PlayerAttackHitboxMixin {
    @Inject(method = "method_7324", at = @At("HEAD"), cancellable = true)
    private void aea$neverAttackOwnPart(class_1297 target, CallbackInfo ci) {
        if (target instanceof HitboxPart part && part.owner == (Object) this) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "method_7324", at = @At("HEAD"), argsOnly = true)
    private class_1297 aea$attackPartOwner(class_1297 target) {
        return target instanceof HitboxPart part && part.owner != (Object) this ? part.owner : target;
    }
}
