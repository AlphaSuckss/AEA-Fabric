package dev.alphasucks.aea.extra.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.seeinvisible.SeeInvisiblePower;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code aea:see_invisible}: an invisible entity is not invisible to a player whose power reveals it. The priority puts
 * this after Apoli's own checks. {@code isInvisible} stays true, so {@code apoli:invisible} is unaffected.
 */
@Mixin(value = class_1297.class, priority = 1500)
public abstract class EntityInvisibleToMixin {
    @ModifyReturnValue(method = "method_5756", at = @At("RETURN"))
    private boolean aea$seeInvisible(boolean original, class_1657 player) {
        return original && Float.isNaN(SeeInvisiblePower.revealOpacity(player, (class_1297) (Object) this));
    }
}
