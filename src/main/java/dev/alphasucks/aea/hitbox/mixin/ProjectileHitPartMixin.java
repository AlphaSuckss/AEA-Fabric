package dev.alphasucks.aea.hitbox.mixin;

import dev.alphasucks.aea.hitbox.HitboxPart;
import net.minecraft.class_1676;
import net.minecraft.class_239;
import net.minecraft.class_3966;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * A projectile that lands on an {@code aea:hitbox_part} box hits the box's owner at that spot, so knockback, fire,
 * stuck arrows and potion effects reach the owner like the damage does.
 */
@Mixin(class_1676.class)
public abstract class ProjectileHitPartMixin {
    @ModifyVariable(method = "method_7488", at = @At("HEAD"), argsOnly = true)
    private class_239 aea$hitPartOwner(class_239 hit) {
        if (hit instanceof class_3966 entityHit && entityHit.method_17782() instanceof HitboxPart part) {
            return new class_3966(part.owner, hit.method_17784());
        }
        return hit;
    }
}
