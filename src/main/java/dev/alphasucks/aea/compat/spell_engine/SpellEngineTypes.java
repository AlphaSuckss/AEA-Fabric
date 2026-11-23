package dev.alphasucks.aea.compat.spell_engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.compat.spell_engine.mixin.SpellCooldownManagerAccessor;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.TextComponent;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.spell_engine.api.event.CombatEvents;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.SpellContainer;
import net.spell_engine.internals.SpellContainerHelper;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.SpellRegistry;
import net.spell_engine.internals.casting.SpellCast;
import net.spell_engine.internals.casting.SpellCasterEntity;
import net.spell_engine.utils.TargetHelper;
import net.spell_power.api.SpellSchool;
import net.spell_power.api.SpellSchools;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Everything AEA adds for Spell Engine 0.15 (Minecraft 1.20.1). Only called when Spell Engine is installed; nothing
 * else in AEA refers to this package (the mixin config has its own plugin check). The JSON is the same as AEA for
 * NeoForge 1.21.1.
 */
public final class SpellEngineTypes {
    private SpellEngineTypes() {}

    public static final class_2960 PREVENT = new class_2960("aea", "prevent_spell_cast");
    public static final class_2960 ON_CAST = new class_2960("aea", "action_on_spell_cast");
    private static final PowerTypeGate PREVENT_GATE = new PowerTypeGate(PREVENT), ON_CAST_GATE = new PowerTypeGate(ON_CAST);

    public static void register() {
        PowerTypeRegistry.register(GrantSpellPower.ID, new GrantSpellPower());
        PowerTypeRegistry.register(ModifySpellPower.ID, new ModifySpellPower());
        PowerTypeRegistry.register(PREVENT, new PreventSpellCast());
        PowerTypeRegistry.register(ON_CAST, new OnSpellCast());

        CombatEvents.SPELL_CAST.register(args -> OnSpellCast.cast(args.caster(), args.spell().id(), args.spell().spell(), args.targets(), args.action()));

        ActionTypes.ENTITY.register(new class_2960("aea", "cast_spell"), new CastSpell.Self());
        ActionTypes.BI_ENTITY.register(new class_2960("aea", "cast_spell"), new CastSpell.AtTarget());

        ConditionTypes.ENTITY.register(new class_2960("aea", "casting_spell"), new EntitySpellCondition(Check.CASTING));
        ConditionTypes.ENTITY.register(new class_2960("aea", "spell_on_cooldown"), new EntitySpellCondition(Check.COOLDOWN));
        ConditionTypes.ENTITY.register(new class_2960("aea", "knows_spell"), new EntitySpellCondition(Check.KNOWS));
        ConditionTypes.ITEM.register(new class_2960("aea", "has_spell"), new ItemSpellCondition());
        ConditionTypes.DAMAGE.register(new class_2960("aea", "spell_damage"), new SpellDamageCondition());
        AEA.LOGGER.info("[AEA] Spell Engine found: spell powers, conditions and actions are on.");
    }

    // ---- casts started by aea:cast_spell ----

    /** Set while {@code aea:cast_spell} runs: prevent_spell_cast doesn't stop it, and without use_cost nothing is paid. */
    private static final ThreadLocal<Boolean> CASTING = new ThreadLocal<>(), FREE = new ThreadLocal<>();

    public static boolean castByAction() {
        return CASTING.get() != null;
    }

    public static boolean free() {
        return FREE.get() != null;
    }

    // ---- power: aea:prevent_spell_cast ----

    /** {@code aea:prevent_spell_cast}: the holder can't start casting matching spells. */
    public record PreventCfg(SpellFilter filter, Optional<class_2561> message) {}

    private static final class PreventSpellCast extends PowerType<PreventCfg> {
        private static final MapCodec<PreventCfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SpellFilter.FIELDS.forGetter(PreventCfg::filter),
            LoggedOptionalField.strict("message", TextComponent.CODEC).forGetter(PreventCfg::message)
        ).apply(i, PreventCfg::new));

        @Override public MapCodec<PreventCfg> configCodec() { return CODEC; }
        @Override public boolean resolvesForClient() { return true; }
    }

    /** {@code SpellHelperMixin}, on Spell Engine's casting check: null lets Spell Engine decide. */
    public static SpellCast.Attempt attempt(class_1657 caster, class_2960 spellId) {
        if (caster == null || castByAction() || !SpellPowers.holds(caster, PREVENT)) return null;
        Spell spell = SpellRegistry.getSpell(spellId);
        PreventCfg[] hit = {null};
        SpellPowers.forEach(caster, PREVENT, PREVENT_GATE, PreventCfg.class, cfg -> {
            if (hit[0] == null && (cfg.filter().any() || cfg.filter().test(spellId, spell))) hit[0] = cfg;
        });
        if (hit[0] == null) return null;
        hit[0].message().ifPresent(m -> caster.method_7353(m, true));
        return SpellCast.Attempt.none();
    }

    // ---- power: aea:action_on_spell_cast ----

    /**
     * {@code aea:action_on_spell_cast}: when the holder casts a matching spell, runs {@code entity_action} on the
     * holder and {@code bientity_action} on each target it hit (holder as actor), filtered by {@code bientity_condition}.
     */
    public record OnCastCfg(SpellFilter filter, Optional<EntityAction> entityAction, Optional<BiEntityAction> bientityAction,
                            Optional<BiEntityCondition> bientityCondition) {}

    private static final class OnSpellCast extends PowerType<OnCastCfg> {
        private static final MapCodec<OnCastCfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SpellFilter.FIELDS.forGetter(OnCastCfg::filter),
            LoggedOptionalField.strict("entity_action", EntityAction.CODEC).forGetter(OnCastCfg::entityAction),
            LoggedOptionalField.strict("bientity_action", BiEntityAction.CODEC).forGetter(OnCastCfg::bientityAction),
            LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(OnCastCfg::bientityCondition)
        ).apply(i, OnCastCfg::new));

        @Override public MapCodec<OnCastCfg> configCodec() { return CODEC; }

        /** Spell Engine fires this for each channel tick too; like a cast spell on 1.21.1, only the release counts. */
        static void cast(class_1657 caster, class_2960 id, Spell spell, List<class_1297> targets, SpellCast.Action action) {
            if (caster == null || caster.method_37908().field_9236 || action != SpellCast.Action.RELEASE || !ON_CAST_GATE.inUse()) return;
            List<OnCastCfg> active = PowerLookup.active(caster, ON_CAST, OnCastCfg.class);
            for (int i = 0; i < active.size(); i++) {
                OnCastCfg cfg = active.get(i);
                if (!cfg.filter().any() && !cfg.filter().test(id, spell)) continue;
                cfg.entityAction().ifPresent(a -> a.run(EntityCtx.of(caster, caster.method_37908())));
                if (cfg.bientityAction().isEmpty() || targets == null) continue;
                for (class_1297 target : List.copyOf(targets)) {
                    BiEntityCtx ctx = BiEntityCtx.of(caster, target, caster.method_37908());
                    if (cfg.bientityCondition().isEmpty() || cfg.bientityCondition().get().test(ctx)) cfg.bientityAction().get().run(ctx);
                }
            }
        }
    }

    // ---- action: aea:cast_spell ----

    /**
     * {@code aea:cast_spell}: the entity (a player) casts a spell at once, with no cast time. As an entity action it
     * aims the way Spell Engine would (where the caster looks); as a bi-entity action it targets the target. With
     * {@code use_cost} false (the default) it costs nothing and starts no cooldown. The player doesn't need to know it.
     */
    public record CastCfg(class_2960 spell, boolean useCost) {}

    private static final MapCodec<CastCfg> CAST_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        class_2960.field_25139.fieldOf("spell").forGetter(CastCfg::spell),
        Codec.BOOL.optionalFieldOf("use_cost", false).forGetter(CastCfg::useCost)
    ).apply(i, CastCfg::new));

    private static final class CastSpell {
        static void cast(CastCfg cfg, class_1297 caster, class_1297 target) {
            if (!(caster instanceof class_1657 player) || player.method_37908().field_9236) return;
            Spell spell = SpellRegistry.getSpell(cfg.spell());
            if (spell == null) {
                AEA.LOGGER.warn("[AEA] aea:cast_spell: no spell {}", cfg.spell());
                return;
            }
            List<class_1297> targets = target != null ? List.of(target) : findTargets(player, cfg.spell(), spell);
            boolean outer = CASTING.get() == null;
            CASTING.set(Boolean.TRUE);
            if (!cfg.useCost()) FREE.set(Boolean.TRUE);
            try {
                SpellHelper.performSpell(player.method_37908(), player, cfg.spell(), targets, SpellCast.Action.RELEASE, 1F);
            } finally {
                if (outer) {
                    CASTING.remove();
                    FREE.remove();
                }
            }
        }

        /** What Spell Engine's client would aim at: the area around the caster, the line they look along, or what they look at. */
        private static List<class_1297> findTargets(class_1657 player, class_2960 id, Spell spell) {
            if (spell.release == null || spell.release.target == null || spell.release.target.type == null) return List.of();
            TargetHelper.TargetingMode mode = SpellHelper.selectionTargetingMode(spell);
            EnumSet<TargetHelper.Intent> intents = SpellHelper.intents(spell);
            Predicate<class_1297> allowed = e -> {
                if (e == player) return false;
                for (TargetHelper.Intent intent : intents) if (TargetHelper.actionAllowed(mode, intent, player, e)) return true;
                return intents.isEmpty();
            };
            float range = ModifySpellPower.range(player, id, spell.range);
            switch (spell.release.target.type) {
                case AREA -> {
                    List<class_1297> list = new ArrayList<>(TargetHelper.targetsFromArea(player, range, spell.release.target.area, allowed));
                    if (spell.release.target.area != null && spell.release.target.area.include_caster) list.add(player);
                    return list;
                }
                case BEAM -> {
                    return TargetHelper.targetsFromRaycast(player, range, allowed);
                }
                case SELF -> {
                    return List.of(player);
                }
                default -> {
                    class_1297 t = TargetHelper.targetFromRaycast(player, range, allowed);
                    return t == null ? List.of() : List.of(t);
                }
            }
        }

        static final class Self implements ActionType<EntityCtx, CastCfg> {
            @Override public MapCodec<CastCfg> codec() { return CAST_CODEC; }
            @Override public void run(CastCfg cfg, EntityCtx ctx) { cast(cfg, ctx.raw(), null); }
        }

        static final class AtTarget implements ActionType<BiEntityCtx, CastCfg> {
            @Override public MapCodec<CastCfg> codec() { return CAST_CODEC; }
            @Override public void run(CastCfg cfg, BiEntityCtx ctx) {
                if (ctx.target() != null) cast(cfg, ctx.actor(), ctx.target());
            }
        }
    }

    // ---- conditions ----

    private enum Check { CASTING, COOLDOWN, KNOWS }

    /**
     * {@code aea:casting_spell}: the player is casting a matching spell. {@code aea:spell_on_cooldown}: a matching spell
     * is on cooldown. {@code aea:knows_spell}: the player can use a matching spell from what they hold (with spell
     * books) or from {@code aea:grant_spell}. With no {@code spell} or {@code school}, any spell counts.
     */
    private static final class EntitySpellCondition implements ConditionType<EntityCtx, SpellFilter> {
        private final Check check;

        EntitySpellCondition(Check check) {
            this.check = check;
        }

        @Override public MapCodec<SpellFilter> codec() { return SpellFilter.FIELDS; }

        @Override
        public boolean test(SpellFilter filter, EntityCtx ctx) {
            if (!(ctx.raw() instanceof class_1657 player) || !(player instanceof SpellCasterEntity caster)) return false;
            switch (check) {
                case CASTING -> {
                    SpellCast.Process process = caster.getSpellCastProcess();
                    return process != null && (filter.any() || filter.test(process.id(), process.spell()));
                }
                case COOLDOWN -> {
                    if (caster.getCooldownManager() == null) return false;
                    for (class_2960 id : List.copyOf(((SpellCooldownManagerAccessor) caster.getCooldownManager()).aea$entries().keySet())) {
                        if (caster.getCooldownManager().isCoolingDown(id) && (filter.any() || filter.test(id))) return true;
                    }
                    return false;
                }
                default -> {
                    SpellContainer held = SpellContainerHelper.getEquipped(player.method_6047(), player);
                    if (held != null && held.spell_ids != null) {
                        for (String raw : held.spell_ids) {
                            class_2960 id = class_2960.method_12829(raw);
                            if (id != null && (filter.any() ? SpellRegistry.getSpell(id) != null : filter.test(id))) return true;
                        }
                    }
                    for (class_2960 id : GrantSpellPower.granted(player)) {
                        if (filter.any() ? SpellRegistry.getSpell(id) != null : filter.test(id)) return true;
                    }
                    return false;
                }
            }
        }
    }

    /** {@code aea:has_spell}: the item carries a matching spell (a spell book, scroll or spell weapon). */
    private static final class ItemSpellCondition implements ConditionType<ItemCtx, SpellFilter> {
        @Override public MapCodec<SpellFilter> codec() { return SpellFilter.FIELDS; }

        @Override
        public boolean test(SpellFilter filter, ItemCtx ctx) {
            if (ctx.stack() == null || ctx.stack().method_7960()) return false;
            SpellContainer container = SpellContainerHelper.containerFromItemStack(ctx.stack());
            if (container == null || container.spell_ids == null) return false;
            for (String raw : container.spell_ids) {
                class_2960 id = class_2960.method_12829(raw);
                if (id == null) continue;
                if (filter.any() || filter.test(id)) return true;
            }
            return false;
        }
    }

    /** {@code aea:spell_damage}: the damage is spell damage (a Spell Power school's damage type), optionally of certain schools. */
    public record SpellDamageCfg(List<class_2960> schools) {}

    private static final class SpellDamageCondition implements ConditionType<DamageCtx, SpellDamageCfg> {
        private static final MapCodec<SpellDamageCfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            SingleOrList.of(class_2960.field_25139).optionalFieldOf("school", List.of()).forGetter(SpellDamageCfg::schools)
        ).apply(i, SpellDamageCfg::new));

        @Override public MapCodec<SpellDamageCfg> codec() { return CODEC; }

        @Override
        public boolean test(SpellDamageCfg cfg, DamageCtx ctx) {
            if (ctx.source() == null) return false;
            for (SpellSchool school : SpellSchools.all()) {
                if (school.damageType == null || (!cfg.schools().isEmpty() && !cfg.schools().contains(school.id))) continue;
                if (ctx.source().method_49708(school.damageType)) return true;
            }
            return false;
        }
    }
}
