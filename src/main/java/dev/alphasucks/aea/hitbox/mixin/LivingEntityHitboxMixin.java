package dev.alphasucks.aea.hitbox.mixin;

import dev.alphasucks.aea.hitbox.HitboxParts;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Holds an entity's {@code aea:hitbox_part} boxes on the entity itself, so they go away with it. */
@Mixin(class_1309.class)
public abstract class LivingEntityHitboxMixin implements HitboxParts.Holder {
    @Unique
    private HitboxParts aea$hitboxParts;

    @Override
    public HitboxParts aea$hitboxParts() {
        HitboxParts state = aea$hitboxParts;
        if (state == null) {
            state = new HitboxParts();
            aea$hitboxParts = state;
        }
        return state;
    }
}
