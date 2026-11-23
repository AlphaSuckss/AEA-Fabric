package dev.alphasucks.aea.hitbox.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.hitbox.HitboxPart;
import net.minecraft.class_1297;
import net.minecraft.class_1676;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** A projectile never hits a hit box that belongs to whoever fired it. */
@Mixin(class_1676.class)
public abstract class ProjectileOwnPartMixin {
    @Shadow
    public abstract class_1297 method_24921();

    @ModifyReturnValue(method = "method_26958", at = @At("RETURN"))
    private boolean aea$skipOwnParts(boolean original, @Local(argsOnly = true) class_1297 target) {
        return original && !(target instanceof HitboxPart part && part.owner == method_24921());
    }
}
