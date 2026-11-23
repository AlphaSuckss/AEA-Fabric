package dev.alphasucks.aea.mobai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.mobai.goal.AeaCustomGoal;
import dev.alphasucks.aea.mobai.goal.AeaFleeGoal;
import dev.alphasucks.aea.mobai.goal.AeaFollowGoal;
import dev.alphasucks.aea.mobai.goal.AeaLookAtGoal;
import dev.alphasucks.aea.mobai.goal.AeaRetaliateGoal;
import dev.alphasucks.aea.mobai.goal.AeaTargetGoal;
import dev.alphasucks.aea.mobai.goal.AeaWanderGoal;
import dev.alphasucks.aea.mobai.goal.GatedGoal;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1308;
import net.minecraft.class_1314;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;
import net.minecraft.class_1366;
import net.minecraft.class_1376;
import org.jetbrains.annotations.Nullable;

/** The goal and target entries of {@code aea:mob_ai}. */
public final class GoalSpecs {
    private GoalSpecs() {
    }

    /** One entry of "goals" or "targets". */
    public interface Spec {
        String type();

        int priority();

        Optional<EntityCondition> condition();

        /** Builds the goal for this mob, or null when the mob can't run it (e.g. it isn't a pathfinding mob). */
        @Nullable
        class_1352 build(class_1308 mob);

        default boolean melee() {
            return false;
        }

        @Nullable
        default class_1352 create(class_1308 mob) {
            class_1352 goal = build(mob);
            if (goal == null) {
                return null;
            }
            return condition().isPresent() ? new GatedGoal(goal, condition().get(), mob) : goal;
        }
    }

    private static <A> com.mojang.serialization.MapCodec<Optional<A>> opt(String name, Codec<A> codec) {
        return LoggedOptionalField.strict(name, codec);
    }

    // ------------------------------------------------------------------ goals

    public record Wander(int priority, double speed, int interval, int horizontal, int vertical, Optional<EntityCondition> condition) implements Spec {
        static final Codec<Wander> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 6).forGetter(Wander::priority),
            Codec.DOUBLE.optionalFieldOf("speed", 1.0).forGetter(Wander::speed),
            Codec.INT.optionalFieldOf("interval", 120).forGetter(Wander::interval),
            Codec.INT.optionalFieldOf("horizontal_range", 10).forGetter(Wander::horizontal),
            Codec.INT.optionalFieldOf("vertical_range", 7).forGetter(Wander::vertical),
            opt("condition", EntityCondition.CODEC).forGetter(Wander::condition)
        ).apply(i, Wander::new));

        public String type() {
            return "aea:wander";
        }

        public class_1352 build(class_1308 mob) {
            return mob instanceof class_1314 pathAware ? new AeaWanderGoal(pathAware, speed, interval, horizontal, vertical) : null;
        }
    }

    public record LookAround(int priority, Optional<EntityCondition> condition) implements Spec {
        static final Codec<LookAround> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 8).forGetter(LookAround::priority),
            opt("condition", EntityCondition.CODEC).forGetter(LookAround::condition)
        ).apply(i, LookAround::new));

        public String type() {
            return "aea:look_around";
        }

        public class_1352 build(class_1308 mob) {
            return new class_1376(mob);
        }
    }

    public record LookAt(int priority, double range, float chance, Optional<BiEntityCondition> bientityCondition, Optional<EntityCondition> condition)
        implements Spec {
        static final Codec<LookAt> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 7).forGetter(LookAt::priority),
            Codec.DOUBLE.optionalFieldOf("range", 8.0).forGetter(LookAt::range),
            Codec.FLOAT.optionalFieldOf("chance", 0.02F).forGetter(LookAt::chance),
            opt("bientity_condition", BiEntityCondition.CODEC).forGetter(LookAt::bientityCondition),
            opt("condition", EntityCondition.CODEC).forGetter(LookAt::condition)
        ).apply(i, LookAt::new));

        public String type() {
            return "aea:look_at";
        }

        public class_1352 build(class_1308 mob) {
            return new AeaLookAtGoal(mob, range, chance, bientityCondition);
        }
    }

    public record MeleeAttack(int priority, double speed, boolean pauseWhenIdle, Optional<EntityCondition> condition) implements Spec {
        static final Codec<MeleeAttack> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 2).forGetter(MeleeAttack::priority),
            Codec.DOUBLE.optionalFieldOf("speed", 1.0).forGetter(MeleeAttack::speed),
            Codec.BOOL.optionalFieldOf("pause_when_idle", false).forGetter(MeleeAttack::pauseWhenIdle),
            opt("condition", EntityCondition.CODEC).forGetter(MeleeAttack::condition)
        ).apply(i, MeleeAttack::new));

        public String type() {
            return "aea:melee_attack";
        }

        public boolean melee() {
            return true;
        }

        public class_1352 build(class_1308 mob) {
            return mob instanceof class_1314 pathAware ? new class_1366(pathAware, speed, pauseWhenIdle) : null;
        }
    }

    public record Follow(int priority, String target, Optional<BiEntityCondition> bientityCondition, double speed, double startDistance,
                         double stopDistance, double range, double teleportDistance, Optional<EntityCondition> condition) implements Spec {
        static final Codec<Follow> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 4).forGetter(Follow::priority),
            Codec.STRING.optionalFieldOf("target", "owner").forGetter(Follow::target),
            opt("bientity_condition", BiEntityCondition.CODEC).forGetter(Follow::bientityCondition),
            Codec.DOUBLE.optionalFieldOf("speed", 1.0).forGetter(Follow::speed),
            Codec.DOUBLE.optionalFieldOf("start_distance", 6.0).forGetter(Follow::startDistance),
            Codec.DOUBLE.optionalFieldOf("stop_distance", 2.5).forGetter(Follow::stopDistance),
            Codec.DOUBLE.optionalFieldOf("range", 16.0).forGetter(Follow::range),
            Codec.DOUBLE.optionalFieldOf("teleport_distance", 0.0).forGetter(Follow::teleportDistance),
            opt("condition", EntityCondition.CODEC).forGetter(Follow::condition)
        ).apply(i, Follow::new));

        public String type() {
            return "aea:follow";
        }

        public class_1352 build(class_1308 mob) {
            boolean owner = !"nearest".equalsIgnoreCase(target);
            return new AeaFollowGoal(mob, owner, bientityCondition, speed, startDistance, stopDistance, range, teleportDistance);
        }
    }

    public record Flee(int priority, double range, double speed, double fastSpeed, Optional<BiEntityCondition> bientityCondition,
                       Optional<EntityCondition> condition) implements Spec {
        static final Codec<Flee> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 1).forGetter(Flee::priority),
            Codec.DOUBLE.optionalFieldOf("range", 8.0).forGetter(Flee::range),
            Codec.DOUBLE.optionalFieldOf("speed", 1.0).forGetter(Flee::speed),
            Codec.DOUBLE.optionalFieldOf("fast_speed", 1.25).forGetter(Flee::fastSpeed),
            opt("bientity_condition", BiEntityCondition.CODEC).forGetter(Flee::bientityCondition),
            opt("condition", EntityCondition.CODEC).forGetter(Flee::condition)
        ).apply(i, Flee::new));

        public String type() {
            return "aea:flee";
        }

        public class_1352 build(class_1308 mob) {
            return mob instanceof class_1314 pathAware ? new AeaFleeGoal(pathAware, range, speed, fastSpeed, bientityCondition) : null;
        }
    }

    private static final Map<String, class_4134> CONTROLS = Map.of(
        "move", class_4134.field_18405, "look", class_4134.field_18406, "jump", class_4134.field_18407, "target", class_4134.field_18408);

    private static final Codec<class_4134> CONTROL = Codec.STRING.comapFlatMap(raw -> {
        class_4134 c = CONTROLS.get(raw.toLowerCase(Locale.ROOT));
        return c != null ? DataResult.success(c) : DataResult.error(() -> "Unknown goal control '" + raw + "' (use move, look, jump or target)");
    }, c -> CONTROLS.entrySet().stream().filter(e -> e.getValue() == c).map(Map.Entry::getKey).findFirst().orElse("move"));

    public record Custom(int priority, List<class_4134> controls, Optional<EntityCondition> startCondition, Optional<EntityCondition> continueCondition,
                         Optional<EntityAction> startAction, Optional<EntityAction> tickAction, Optional<EntityAction> stopAction, int interval,
                         int duration, int cooldown) implements Spec {
        static final Codec<Custom> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 3).forGetter(Custom::priority),
            CONTROL.listOf().optionalFieldOf("controls", List.of()).forGetter(Custom::controls),
            opt("start_condition", EntityCondition.CODEC).forGetter(Custom::startCondition),
            opt("continue_condition", EntityCondition.CODEC).forGetter(Custom::continueCondition),
            opt("start_action", EntityAction.CODEC).forGetter(Custom::startAction),
            opt("tick_action", EntityAction.CODEC).forGetter(Custom::tickAction),
            opt("stop_action", EntityAction.CODEC).forGetter(Custom::stopAction),
            Codec.INT.optionalFieldOf("interval", 1).forGetter(Custom::interval),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(Custom::duration),
            Codec.INT.optionalFieldOf("cooldown", 0).forGetter(Custom::cooldown)
        ).apply(i, Custom::new));

        public String type() {
            return "aea:custom";
        }

        public Optional<EntityCondition> condition() {
            return Optional.empty();
        }

        public class_1352 build(class_1308 mob) {
            EnumSet<class_4134> set = EnumSet.noneOf(class_4134.class);
            set.addAll(controls);
            return new AeaCustomGoal(mob, set, startCondition, continueCondition, startAction, tickAction, stopAction, interval, duration, cooldown);
        }
    }

    // ---------------------------------------------------------------- targets

    public record Target(int priority, Optional<Double> range, boolean mustSee, int interval, Optional<BiEntityCondition> bientityCondition,
                         Optional<EntityCondition> condition) implements Spec {
        static final Codec<Target> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 2).forGetter(Target::priority),
            Codec.DOUBLE.optionalFieldOf("range").forGetter(Target::range),
            Codec.BOOL.optionalFieldOf("must_see", true).forGetter(Target::mustSee),
            Codec.INT.optionalFieldOf("interval", 10).forGetter(Target::interval),
            opt("bientity_condition", BiEntityCondition.CODEC).forGetter(Target::bientityCondition),
            opt("condition", EntityCondition.CODEC).forGetter(Target::condition)
        ).apply(i, Target::new));

        public String type() {
            return "aea:target";
        }

        public class_1352 build(class_1308 mob) {
            return new AeaTargetGoal(mob, range, mustSee, interval, bientityCondition);
        }
    }

    public record Retaliate(int priority, Optional<BiEntityCondition> bientityCondition, boolean alertOthers, double alertRange,
                            Optional<EntityCondition> condition) implements Spec {
        static final Codec<Retaliate> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("priority", 1).forGetter(Retaliate::priority),
            opt("bientity_condition", BiEntityCondition.CODEC).forGetter(Retaliate::bientityCondition),
            Codec.BOOL.optionalFieldOf("alert_others", false).forGetter(Retaliate::alertOthers),
            Codec.DOUBLE.optionalFieldOf("alert_range", 10.0).forGetter(Retaliate::alertRange),
            opt("condition", EntityCondition.CODEC).forGetter(Retaliate::condition)
        ).apply(i, Retaliate::new));

        public String type() {
            return "aea:retaliate";
        }

        public class_1352 build(class_1308 mob) {
            return new AeaRetaliateGoal(mob, bientityCondition, alertOthers, alertRange);
        }
    }

    // --------------------------------------------------------------- dispatch

    private static final Map<String, Codec<? extends Spec>> GOALS = Map.of(
        "wander", Wander.CODEC, "look_around", LookAround.CODEC, "look_at", LookAt.CODEC, "melee_attack", MeleeAttack.CODEC,
        "follow", Follow.CODEC, "flee", Flee.CODEC, "custom", Custom.CODEC);

    private static final Map<String, Codec<? extends Spec>> TARGETS = Map.of(
        "target", Target.CODEC, "retaliate", Retaliate.CODEC);

    public static final Codec<Spec> GOAL = dispatch(GOALS, "goal");
    public static final Codec<Spec> TARGET = dispatch(TARGETS, "target");

    private static String key(String type) {
        String t = type.toLowerCase(Locale.ROOT);
        return t.startsWith("aea:") ? t.substring(4) : t;
    }

    private static Codec<Spec> dispatch(Map<String, Codec<? extends Spec>> types, String what) {
        Codec<String> typeCodec = Codec.STRING.comapFlatMap(raw -> types.containsKey(key(raw))
                ? DataResult.success("aea:" + key(raw))
                : DataResult.error(() -> "Unknown mob_ai " + what + " type '" + raw + "'. Known: aea:" + String.join(", aea:", types.keySet())),
            s -> s);
        return typeCodec.dispatch("type", Spec::type, t -> types.get(key(t)));
    }
}
