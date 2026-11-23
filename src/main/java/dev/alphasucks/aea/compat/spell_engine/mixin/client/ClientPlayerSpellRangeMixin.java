package dev.alphasucks.aea.compat.spell_engine.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.compat.spell_engine.ModifySpellPower;
import net.minecraft.class_746;
import net.spell_engine.api.spell.Spell;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code aea:modify_spell} range for what the player aims at. Spell Engine adds {@code findTargets} to the client
 * player with its own mixin, so this applies after it (higher priority) and is skipped if the method ever changes.
 */
@Mixin(value = class_746.class, priority = 1500)
public abstract class ClientPlayerSpellRangeMixin {
    @ModifyExpressionValue(method = "findTargets", at = @At(value = "FIELD", target = "Lnet/spell_engine/api/spell/Spell;range:F"), require = 0)
    private float aea$targetRange(float original, @Local(argsOnly = true) Spell spell) {
        return ModifySpellPower.range((class_746) (Object) this, spell, original);
    }
}
