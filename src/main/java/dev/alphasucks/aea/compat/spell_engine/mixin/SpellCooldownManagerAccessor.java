package dev.alphasucks.aea.compat.spell_engine.mixin;

import net.minecraft.class_2960;
import net.spell_engine.internals.SpellCooldownManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/** {@code aea:spell_on_cooldown} with a school or no spell: which spells have a cooldown entry. */
@Mixin(value = SpellCooldownManager.class, remap = false)
public interface SpellCooldownManagerAccessor {
    @Accessor("entries")
    Map<class_2960, ?> aea$entries();
}
