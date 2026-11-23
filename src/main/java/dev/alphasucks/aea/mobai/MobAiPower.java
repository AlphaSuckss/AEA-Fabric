package dev.alphasucks.aea.mobai;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.mobai.mixin.AttributeContainerAccessor;
import dev.alphasucks.aea.mobai.mixin.MobEntitySelectorsAccessor;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1322;
import net.minecraft.class_1324;
import net.minecraft.class_1352;
import net.minecraft.class_1355;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_4095;
import net.minecraft.class_4135;
import net.minecraft.class_5131;
import net.minecraft.class_5134;

/**
 * {@code aea:mob_ai}: replaces or extends the holder's (any mob's) goals and targets. See {@code AEA_PORTED_TYPES.md}.
 * <p>
 * Goal-based mobs get the new goals in their own goal/target selectors. Brain-based mobs (villagers, piglins,
 * frogs, wardens, ... — their selectors are empty) get a private pair of selectors that this power ticks;
 * {@code "remove": ["brain"]} switches their brain off while the power is active.
 */
public final class MobAiPower extends PowerType<MobAiPower.Config> {
    public static final class_2960 ID = new class_2960("aea", "mob_ai");
    static final PowerTypeGate GATE = new PowerTypeGate(ID);
    /** undos requested while a goal selector may be iterating (power revoked from inside an action) */
    private static final ArrayDeque<Runnable> DEFERRED = new ArrayDeque<>();

    public record Config(List<String> remove, List<GoalSpecs.Spec> goals, List<GoalSpecs.Spec> targets, Optional<BiEntityCondition> neverTarget,
                         Optional<Double> attackDamage, int reapplyInterval) {
        boolean removesBrain() {
            for (String r : remove) {
                String k = r.toLowerCase(Locale.ROOT);
                if (k.equals("brain") || k.equals("all")) {
                    return true;
                }
            }
            return false;
        }

        boolean needsAttackDamage() {
            if (attackDamage.isPresent()) {
                return true;
            }
            for (GoalSpecs.Spec spec : goals) {
                if (spec.melee()) {
                    return true;
                }
            }
            return false;
        }
    }

    static <A> MapCodec<A> strictOr(String name, Codec<A> codec, A fallback) {
        return LoggedOptionalField.strict(name, codec).xmap(o -> o.orElse(fallback), Optional::of);
    }

    @Override
    public MapCodec<Config> configCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
            strictOr("remove", SingleOrList.of(Codec.STRING), List.<String>of()).forGetter(Config::remove),
            strictOr("goals", GoalSpecs.GOAL.listOf(), List.<GoalSpecs.Spec>of()).forGetter(Config::goals),
            strictOr("targets", GoalSpecs.TARGET.listOf(), List.<GoalSpecs.Spec>of()).forGetter(Config::targets),
            LoggedOptionalField.strict("never_target", BiEntityCondition.CODEC).forGetter(Config::neverTarget),
            Codec.DOUBLE.optionalFieldOf("attack_damage").forGetter(Config::attackDamage),
            Codec.INT.optionalFieldOf("reapply_interval", 20).forGetter(Config::reapplyInterval)
        ).apply(i, Config::new));
    }

    // ------------------------------------------------------------- lifecycle

    private static MobAiState state(class_1308 mob) {
        return ((MobAiState.Holder) mob).aea$mobAiState();
    }

    @Override
    public void tick(class_2960 powerId, Config cfg, PowerContainer holder) {
        if (!(holder.rawOwner() instanceof class_1308 mob) || !(mob.method_37908() instanceof class_3218) || holder.isSuppressed(powerId)) {
            return;
        }
        MobAiState state = state(mob);
        Applied applied = state.applied.get(powerId);
        if (applied != null && applied.cfg != cfg) {
            applied.undo(mob, state);
            state.applied.remove(powerId);
            applied = null;
        }
        if (applied == null) {
            applied = Applied.apply(mob, state, powerId, cfg);
            state.applied.put(powerId, applied);
        }
        applied.pendingUndo = false;
        applied.activeTick = mob.method_37908().method_8510();
        applied.tick(mob);
    }

    @Override
    public void tickStored(class_2960 powerId, Config cfg, PowerContainer holder) {
        if (!(holder.rawOwner() instanceof class_1308 mob) || !(mob.method_37908() instanceof class_3218)) {
            return;
        }
        MobAiState state = state(mob);
        Applied applied = state.applied.get(powerId);
        if (applied != null && applied.activeTick != mob.method_37908().method_8510()) {
            applied.undo(mob, state);
            state.applied.remove(powerId);
        }
    }

    @Override
    public void onRemoved(class_2960 powerId, Config cfg, PowerContainer holder, class_2960 source) {
        // another source still grants the power: keep its changes until the last one is gone
        if (holder.hasPower(powerId) || !(holder.rawOwner() instanceof class_1308 mob) || mob.method_37908().field_9236) {
            return;
        }
        MobAiState state = state(mob);
        Applied applied = state.applied.get(powerId);
        if (applied == null) {
            return;
        }
        // revocation can happen inside a goal's action, i.e. while the selector is iterating: undo at end of tick
        applied.pendingUndo = true;
        DEFERRED.add(() -> {
            if (applied.pendingUndo && state.applied.get(powerId) == applied) {
                applied.undo(mob, state);
                state.applied.remove(powerId);
            }
        });
    }

    /** Runs deferred undos (server thread, end of server tick). */
    static void runDeferred() {
        Runnable task;
        while ((task = DEFERRED.poll()) != null) {
            try {
                task.run();
            } catch (RuntimeException ignored) {
            }
        }
    }

    // ------------------------------------------------------- shared lookups

    /** Active mob_ai configs of a mob, cached for the current tick. */
    static List<Config> active(class_1308 mob) {
        if (!GATE.inUse() || mob.method_37908().field_9236) {
            return List.of();
        }
        MobAiState state = state(mob);
        long stamp = MobPathingPower.stamp(mob);
        if (state.aiStamp != stamp) {
            state.aiStamp = stamp;
            List<Config> found;
            try {
                found = PowerLookup.active(mob, ID, Config.class);
            } catch (RuntimeException e) {
                found = List.of();
            }
            state.ai = found;
        }
        return state.ai;
    }

    /** never_target: the mob may not target this entity at all (hooked into canTarget). */
    public static boolean neverTargets(class_1308 mob, class_1309 target) {
        List<Config> configs = active(mob);
        for (int i = 0; i < configs.size(); i++) {
            Optional<BiEntityCondition> never = configs.get(i).neverTarget();
            if (never.isPresent()) {
                try {
                    if (never.get().test(BiEntityCtx.of(mob, target, mob.method_37908()))) {
                        return true;
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
        return false;
    }

    /** "remove": ["brain"] — the mob's brain doesn't tick while the power is active. */
    public static boolean suppressesBrain(class_1308 mob) {
        List<Config> configs = active(mob);
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).removesBrain()) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------- applied

    static final class Applied {
        final Config cfg;
        final UUID modifierId;
        long activeTick;
        boolean pendingUndo;
        final boolean ownSelectors;
        final class_1355 goals;
        final class_1355 targets;
        final Set<class_1352> added = Collections.newSetFromMap(new IdentityHashMap<>());
        final List<Pair<Integer, class_1352>> removedGoals = new ArrayList<>();
        final List<Pair<Integer, class_1352>> removedTargets = new ArrayList<>();
        boolean damageModifier;
        boolean brainStopped;

        private Applied(Config cfg, class_2960 powerId, boolean ownSelectors, class_1355 goals, class_1355 targets) {
            this.cfg = cfg;
            this.modifierId = UUID.nameUUIDFromBytes(("aea:mob_ai/" + powerId).getBytes(StandardCharsets.UTF_8));
            this.ownSelectors = ownSelectors;
            this.goals = goals;
            this.targets = targets;
        }

        static Applied apply(class_1308 mob, MobAiState state, class_2960 powerId, Config cfg) {
            MobEntitySelectorsAccessor selectors = (MobEntitySelectorsAccessor) mob;
            class_1355 mobGoals = selectors.aea$goalSelector();
            class_1355 mobTargets = selectors.aea$targetSelector();
            if (state.brainMob == null || state.applied.isEmpty()) {
                state.brainMob = onlyAea(mobGoals, state) && onlyAea(mobTargets, state);
            }
            Applied applied;
            if (state.brainMob) {
                applied = new Applied(cfg, powerId, true, new class_1355(() -> mob.method_37908().method_16107()),
                    new class_1355(() -> mob.method_37908().method_16107()));
            } else {
                applied = new Applied(cfg, powerId, false, mobGoals, mobTargets);
                applied.removeVanilla(state);
            }
            for (GoalSpecs.Spec spec : cfg.goals()) {
                class_1352 goal = spec.create(mob);
                if (goal != null) {
                    applied.goals.method_6277(spec.priority(), goal);
                    applied.added.add(goal);
                    state.aeaGoals.add(goal);
                }
            }
            for (GoalSpecs.Spec spec : cfg.targets()) {
                class_1352 goal = spec.create(mob);
                if (goal != null) {
                    applied.targets.method_6277(spec.priority(), goal);
                    applied.added.add(goal);
                    state.aeaGoals.add(goal);
                }
            }
            applied.applyAttributes(mob, state);
            return applied;
        }

        private static boolean onlyAea(class_1355 selector, MobAiState state) {
            for (class_4135 prioritized : selector.method_35115()) {
                if (!state.aeaGoals.contains(prioritized.method_19058())) {
                    return false;
                }
            }
            return true;
        }

        void tick(class_1308 mob) {
            MobAiState state = state(mob);
            if (!ownSelectors && cfg.reapplyInterval() > 0 && !cfg.remove().isEmpty() && mob.field_6012 % cfg.reapplyInterval() == 0) {
                removeVanilla(state); // some mobs (e.g. clones switching weapons) re-add their goals
            }
            if (ownSelectors && !mob.method_5987() && mob.method_5805()) {
                if (mob.field_6012 % 2 == 0) {
                    targets.method_6275();
                    goals.method_6275();
                } else {
                    targets.method_38849(false);
                    goals.method_38849(false);
                }
            }
            if (cfg.removesBrain() && !brainStopped && mob.method_37908() instanceof class_3218 world) {
                brainStopped = true;
                try {
                    @SuppressWarnings("unchecked")
                    class_4095<class_1308> brain = (class_4095<class_1308>) mob.method_18868();
                    brain.method_18900(world, mob);
                } catch (RuntimeException ignored) {
                }
            }
        }

        private void removeVanilla(MobAiState state) {
            if (cfg.remove().isEmpty()) {
                return;
            }
            strip(goals, removedGoals, true, state);
            strip(targets, removedTargets, false, state);
        }

        private void strip(class_1355 selector, List<Pair<Integer, class_1352>> removed, boolean goalSelector, MobAiState state) {
            List<class_4135> doomed = null;
            for (class_4135 prioritized : selector.method_35115()) {
                class_1352 goal = prioritized.method_19058();
                if (state.aeaGoals.contains(goal)) {
                    continue; // never strip goals added by any aea:mob_ai power
                }
                for (String category : cfg.remove()) {
                    if (goalSelector ? GoalCategories.matchesGoal(category, goal) : GoalCategories.matchesTarget(category, goal)) {
                        if (doomed == null) {
                            doomed = new ArrayList<>();
                        }
                        doomed.add(prioritized);
                        break;
                    }
                }
            }
            if (doomed == null) {
                return;
            }
            for (class_4135 prioritized : doomed) {
                class_1352 goal = prioritized.method_19058();
                boolean known = false;
                for (Pair<Integer, class_1352> entry : removed) {
                    if (entry.getSecond() == goal) {
                        known = true;
                        break;
                    }
                }
                if (!known) {
                    removed.add(Pair.of(prioritized.method_19057(), goal));
                }
                selector.method_6280(goal);
            }
        }

        /** Gives peaceful mobs attack damage/knockback (kept afterwards), and applies attack_damage as a temporary modifier. */
        private void applyAttributes(class_1308 mob, MobAiState state) {
            if (!cfg.needsAttackDamage()) {
                return;
            }
            class_5131 attributes = mob.method_6127();
            Map<class_1320, class_1324> custom = ((AttributeContainerAccessor) attributes).aea$custom();
            if (!attributes.method_27306(class_5134.field_23721)) {
                class_1324 instance = new class_1324(class_5134.field_23721, i -> {
                });
                instance.method_6192(2.0);
                custom.put(class_5134.field_23721, instance);
                state.injectedAttackDamage = true;
            }
            if (!attributes.method_27306(class_5134.field_23722)) {
                class_1324 instance = new class_1324(class_5134.field_23722, i -> {
                });
                instance.method_6192(0.0);
                custom.put(class_5134.field_23722, instance);
            }
            if (cfg.attackDamage().isPresent()) {
                class_1324 instance = attributes.method_26842(class_5134.field_23721);
                if (instance != null) {
                    instance.method_6200(modifierId);
                    instance.method_26835(new class_1322(modifierId, "aea:mob_ai attack_damage",
                        cfg.attackDamage().get() - instance.method_6194(), class_1322.class_1323.field_6328));
                    damageModifier = true;
                }
            }
        }

        void undo(class_1308 mob, MobAiState state) {
            for (class_1352 goal : added) {
                goals.method_6280(goal);
                targets.method_6280(goal);
                state.aeaGoals.remove(goal);
            }
            added.clear();
            if (!ownSelectors) {
                restore(goals, removedGoals, true);
                restore(targets, removedTargets, false);
            }
            removedGoals.clear();
            removedTargets.clear();
            if (damageModifier) {
                class_1324 instance = mob.method_6127().method_26842(class_5134.field_23721);
                if (instance != null) {
                    instance.method_6200(modifierId);
                }
                damageModifier = false;
            }
            if (state.applied.size() <= 1) {
                state.brainMob = null;
            }
        }

        /** Puts stripped goals back, skipping ones the mob has re-added or replaced itself (e.g. a clone that switched weapons). */
        private static void restore(class_1355 selector, List<Pair<Integer, class_1352>> removed, boolean goalSelector) {
            List<class_1352> current = new ArrayList<>();
            for (class_4135 prioritized : selector.method_35115()) {
                current.add(prioritized.method_19058());
            }
            for (Pair<Integer, class_1352> entry : removed) {
                class_1352 goal = entry.getSecond();
                boolean present = false;
                boolean attack = goalSelector && (GoalCategories.matchesGoal("melee", goal) || GoalCategories.matchesGoal("ranged", goal));
                for (class_1352 other : current) {
                    if (other == goal || other.getClass() == goal.getClass()
                        || attack && (GoalCategories.matchesGoal("melee", other) || GoalCategories.matchesGoal("ranged", other))) {
                        present = true;
                        break;
                    }
                }
                if (!present) {
                    selector.method_6277(entry.getFirst(), goal);
                }
            }
        }
    }
}
