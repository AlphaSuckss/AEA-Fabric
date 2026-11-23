package dev.alphasucks.aea.compat.spell_engine.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.compat.spell_engine.GrantSpellPower;
import net.minecraft.class_1657;
import net.spell_engine.api.spell.SpellContainer;
import net.spell_engine.internals.SpellContainerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** {@code aea:grant_spell}: adds granted spells to the held item's spells, which the spell hotbar is built from. */
@Mixin(value = SpellContainerHelper.class, remap = false)
public abstract class SpellContainerHelperMixin {
    @ModifyReturnValue(method = "getEquipped(Lnet/minecraft/class_1799;Lnet/minecraft/class_1657;)Lnet/spell_engine/api/spell/SpellContainer;", at = @At("RETURN"))
    private static SpellContainer aea$grantSpells(SpellContainer original, @Local(argsOnly = true) class_1657 player) {
        return GrantSpellPower.extend(original, player);
    }
}
