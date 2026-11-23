package dev.alphasucks.aea.compat.spell_engine;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.SpellRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * {@code aea:modify_spell}: changes the cast time (in seconds), cooldown (in seconds) and range (in blocks) of the
 * holder's Spell Engine spells, with Apoli modifiers. Applied on the values Spell Engine works out, after spell haste
 * ({@code SpellHelperMixin}, {@code ClientPlayerSpellRangeMixin}). Resolved on the server so the client's cast bar,
 * targeting and cooldowns match.
 */
public final class ModifySpellPower extends PowerType<ModifySpellPower.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "modify_spell");
    public static final PowerTypeGate GATE = new PowerTypeGate(ID);

    public record Cfg(SpellFilter filter, List<AttributeModifier> castTime, List<AttributeModifier> cooldown, List<AttributeModifier> range) {
        /** Sorted once at load, so a single matching power applies its list as is. */
        public Cfg {
            castTime = AttributeModifierHelper.ensureSorted(castTime);
            cooldown = AttributeModifierHelper.ensureSorted(cooldown);
            range = AttributeModifierHelper.ensureSorted(range);
        }
    }

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        SpellFilter.FIELDS.forGetter(Cfg::filter),
        AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("cast_time", List.of()).forGetter(Cfg::castTime),
        AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("cooldown", List.of()).forGetter(Cfg::cooldown),
        AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("range", List.of()).forGetter(Cfg::range)
    ).apply(i, Cfg::new));

    @Override
    public MapCodec<Cfg> configCodec() {
        return CODEC;
    }

    @Override
    public boolean resolvesForClient() {
        return true;
    }

    private static final Function<Cfg, List<AttributeModifier>> CAST_TIME = Cfg::castTime, COOLDOWN = Cfg::cooldown, RANGE = Cfg::range;

    // These run whenever Spell Engine asks for a spell's numbers, which on the client can be every frame: they bail out
    // before any lookup unless the caster holds the type.

    public static float castTime(@Nullable class_1297 caster, @Nullable Spell spell, float value) {
        return apply(caster, null, spell, value, CAST_TIME);
    }

    public static float cooldown(@Nullable class_1297 caster, @Nullable Spell spell, float value) {
        return apply(caster, null, spell, value, COOLDOWN);
    }

    public static float range(@Nullable class_1297 caster, @Nullable Spell spell, float value) {
        return apply(caster, null, spell, value, RANGE);
    }

    public static float range(@Nullable class_1297 caster, @Nullable class_2960 spellId, float value) {
        return spellId == null ? value : apply(caster, spellId, SpellRegistry.getSpell(spellId), value, RANGE);
    }

    private static float apply(@Nullable class_1297 caster, @Nullable class_2960 id, @Nullable Spell spell, float value,
                               Function<Cfg, List<AttributeModifier>> field) {
        if (caster == null || spell == null || !SpellPowers.holds(caster, ID)) return value;
        class_2960 spellId = id != null ? id : SpellIds.of(spell);
        List<AttributeModifier>[] mods = new List[1];
        boolean[] merged = {false};
        SpellPowers.forEach(caster, ID, GATE, Cfg.class, cfg -> {
            List<AttributeModifier> m = field.apply(cfg);
            if (m.isEmpty() || (!cfg.filter().any() && !cfg.filter().test(spellId, spell))) return;
            if (mods[0] == null) {
                mods[0] = m;
            } else {
                if (!merged[0]) mods[0] = new ArrayList<>(mods[0]);
                merged[0] = true;
                mods[0].addAll(m);
            }
        });
        if (mods[0] == null) return value;
        List<AttributeModifier> list = merged[0] ? AttributeModifierHelper.ensureSorted(mods[0]) : mods[0];
        return (float) Math.max(0, AttributeModifierHelper.apply((double) value, list, caster));
    }
}
