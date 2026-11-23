package dev.alphasucks.aea.compat.spell_engine.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.compat.spell_engine.ModifySpellPower;
import dev.alphasucks.aea.compat.spell_engine.SpellEngineTypes;
import net.minecraft.class_1291;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.SpellInfo;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.casting.SpellCast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code aea:prevent_spell_cast} on Spell Engine's casting check, {@code aea:modify_spell} on cast time, cooldown and
 * range, and {@code aea:cast_spell} without {@code use_cost}: no cost, ammo or cooldown.
 */
@Mixin(value = SpellHelper.class, remap = false)
public abstract class SpellHelperMixin {
    // ---- prevent_spell_cast, cast_spell ----

    @ModifyReturnValue(method = "attemptCasting(Lnet/minecraft/class_1657;Lnet/minecraft/class_1799;Lnet/minecraft/class_2960;Z)Lnet/spell_engine/internals/casting/SpellCast$Attempt;", at = @At("RETURN"))
    private static SpellCast.Attempt aea$attempt(SpellCast.Attempt original, @Local(argsOnly = true) class_1657 player, @Local(argsOnly = true) class_2960 spellId) {
        if (SpellEngineTypes.free()) return SpellCast.Attempt.success();
        if (original == null || !original.isSuccess()) return original;
        SpellCast.Attempt prevented = SpellEngineTypes.attempt(player, spellId);
        return prevented != null ? prevented : original;
    }

    // ---- modify_spell ----

    @ModifyReturnValue(method = "getCastDuration(Lnet/minecraft/class_1309;Lnet/spell_engine/api/spell/Spell;Lnet/minecraft/class_1799;)F", at = @At("RETURN"))
    private static float aea$castDuration(float original, @Local(argsOnly = true) class_1309 caster, @Local(argsOnly = true) Spell spell) {
        return ModifySpellPower.castTime(caster, spell, original);
    }

    /** The cast bar's length; worked out here without going through getCastDuration. */
    @ModifyExpressionValue(method = "getCastTimeDetails", at = @At(value = "INVOKE", target = "Lnet/spell_engine/internals/SpellHelper;hasteAffectedValue(FF)F"))
    private static float aea$castTimeDetails(float original, @Local(argsOnly = true) class_1309 caster, @Local(argsOnly = true) Spell spell) {
        return ModifySpellPower.castTime(caster, spell, original);
    }

    @ModifyReturnValue(method = "getCooldownDuration(Lnet/minecraft/class_1309;Lnet/spell_engine/api/spell/Spell;Lnet/minecraft/class_1799;)F", at = @At("RETURN"))
    private static float aea$cooldown(float original, @Local(argsOnly = true) class_1309 caster, @Local(argsOnly = true) Spell spell) {
        return ModifySpellPower.cooldown(caster, spell, original);
    }

    @ModifyExpressionValue(method = "performSpell", at = @At(value = "FIELD", target = "Lnet/spell_engine/api/spell/Spell;range:F"))
    private static float aea$areaRange(float original, @Local(argsOnly = true) class_1657 player, @Local(argsOnly = true) class_2960 spellId) {
        return ModifySpellPower.range(player, spellId, original);
    }

    @ModifyExpressionValue(method = "shootProjectile(Lnet/minecraft/class_1937;Lnet/minecraft/class_1309;Lnet/minecraft/class_1297;Lnet/spell_engine/api/spell/SpellInfo;Lnet/spell_engine/internals/SpellHelper$ImpactContext;I)V",
        at = @At(value = "FIELD", target = "Lnet/spell_engine/api/spell/Spell;range:F"))
    private static float aea$projectileRange(float original, @Local(argsOnly = true) class_1309 caster, @Local(argsOnly = true) SpellInfo info) {
        return ModifySpellPower.range(caster, info == null ? null : info.id(), original);
    }

    // ---- cast_spell without use_cost ----

    @ModifyExpressionValue(method = "performSpell", at = @At(value = "INVOKE", target = "Lnet/spell_engine/internals/SpellHelper$AmmoResult;satisfied()Z"))
    private static boolean aea$freeAmmo(boolean original) {
        return original || SpellEngineTypes.free();
    }

    @WrapWithCondition(method = "performSpell", at = @At(value = "INVOKE", target = "Lnet/spell_engine/internals/SpellHelper;imposeCooldown(Lnet/minecraft/class_1657;Lnet/minecraft/class_2960;Lnet/spell_engine/api/spell/Spell;F)V"))
    private static boolean aea$freeCooldown(class_1657 player, class_2960 id, Spell spell, float progress) {
        return !SpellEngineTypes.free();
    }

    @WrapWithCondition(method = "performSpell", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_7322(F)V"))
    private static boolean aea$freeExhaust(class_1657 player, float amount) {
        return !SpellEngineTypes.free();
    }

    @WrapWithCondition(method = "performSpell", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7956(ILnet/minecraft/class_1309;Ljava/util/function/Consumer;)V"))
    private static boolean aea$freeDurability(class_1799 stack, int amount, class_1309 entity, java.util.function.Consumer<?> onBreak) {
        return !SpellEngineTypes.free();
    }

    @WrapWithCondition(method = "performSpell", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1799;method_7934(I)V"))
    private static boolean aea$freeItem(class_1799 stack, int amount) {
        return !SpellEngineTypes.free();
    }

    @WrapOperation(method = "performSpell", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1657;method_6016(Lnet/minecraft/class_1291;)Z"))
    private static boolean aea$freeEffect(class_1657 player, class_1291 effect, Operation<Boolean> original) {
        return !SpellEngineTypes.free() && original.call(player, effect);
    }
}
