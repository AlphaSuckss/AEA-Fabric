package dev.alphasucks.aea.extra.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.seeinvisible.client.SeeInvisible;
import io.github.apace100.apoli.power.InvisibilityPower;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Armor hidden by {@code apoli:invisibility} shows again for a player who sees through that invisibility. */
@Mixin(value = InvisibilityPower.class, remap = false)
public abstract class ApoliInvisibilityArmorMixin {
    @ModifyReturnValue(method = "shouldRenderArmor", at = @At("RETURN"))
    private boolean aea$seenThrough(boolean original) {
        if (original) {
            return true;
        }
        class_1309 entity = ((PowerEntityAccessor) (Object) this).aea$entity();
        return entity != null && entity.method_37908().field_9236 && !Float.isNaN(SeeInvisible.opacity(entity));
    }
}
