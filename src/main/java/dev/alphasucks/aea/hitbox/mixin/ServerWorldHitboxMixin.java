package dev.alphasucks.aea.hitbox.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.hitbox.HitboxWorld;
import net.minecraft.class_1297;
import net.minecraft.class_3218;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** An attack packet names its target by network id; this is where ender dragon parts, and now AEA's boxes, are found. */
@Mixin(class_3218.class)
public abstract class ServerWorldHitboxMixin {
    @ModifyReturnValue(method = "method_31424", at = @At("RETURN"))
    private class_1297 aea$hitboxPartById(class_1297 original, @Local(argsOnly = true) int id) {
        return original != null ? original : HitboxWorld.byId(this, id);
    }
}
