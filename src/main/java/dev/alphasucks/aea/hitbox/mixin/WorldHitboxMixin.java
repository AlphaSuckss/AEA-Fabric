package dev.alphasucks.aea.hitbox.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.hitbox.HitboxPart;
import dev.alphasucks.aea.hitbox.HitboxWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_238;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The world's {@code aea:hitbox_part} boxes. Entity searches (crosshair, projectiles, explosions) find them the way
 * they find ender dragon parts. A box is never returned to its own owner. Costs one empty-array check per search
 * while no entity has the power.
 */
@Mixin(class_1937.class)
public abstract class WorldHitboxMixin implements HitboxWorld {
    @Unique
    private volatile HitboxPart[] aea$hitboxParts = HitboxPart.NONE;

    @Override
    public HitboxPart[] aea$hitboxParts() {
        // null in a world object that was created without running a constructor
        HitboxPart[] parts = aea$hitboxParts;
        return parts == null ? HitboxPart.NONE : parts;
    }

    @Override
    public void aea$setHitboxParts(HitboxPart[] parts) {
        aea$hitboxParts = parts;
    }

    @ModifyReturnValue(method = "method_8333", at = @At("RETURN"))
    private List<class_1297> aea$findHitboxParts(List<class_1297> original, @Local(argsOnly = true) class_1297 except,
                                                 @Local(argsOnly = true) class_238 box, @Local(argsOnly = true) Predicate<? super class_1297> predicate) {
        HitboxPart[] parts = aea$hitboxParts;
        if (parts == null || parts.length == 0) {
            return original;
        }
        List<class_1297> out = original;
        for (HitboxPart part : parts) {
            if (part == except || part.owner == except || !part.method_5829().method_994(box) || !predicate.test(part)) {
                continue;
            }
            if (out == original && !(original instanceof ArrayList)) {
                out = new ArrayList<>(original);
            }
            out.add(part);
        }
        return out;
    }
}
