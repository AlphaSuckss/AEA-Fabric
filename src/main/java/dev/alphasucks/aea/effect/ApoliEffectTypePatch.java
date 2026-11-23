package dev.alphasucks.aea.effect;

import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.extra.mixin.ConditionFactoryAccessor;
import dev.alphasucks.aea.extra.mixin.PowerFactoryAccessor;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.EnumSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.class_1293;
import net.minecraft.class_1309;
import net.minecraft.class_4081;

/**
 * Adds {@code effect_type} to two of Apoli 2.9.2's own types, in place, so packs keep their type ids:
 * <ul>
 * <li>{@code apoli:effect_immunity}: also immune to every effect of those kinds ({@code inverted} applies to both).</li>
 * <li>{@code apoli:status_effect} (entity condition): {@code effect} becomes optional; without it the condition is
 * true when the entity has any effect of those kinds, with the same amplifier and duration limits.</li>
 * </ul>
 * Runs once after Apoli has registered its types, on both sides, so power data synced to players matches.
 */
public final class ApoliEffectTypePatch {
    private ApoliEffectTypePatch() {
    }

    private static boolean applied;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static synchronized void apply() {
        if (applied) {
            return;
        }
        applied = true;
        PowerFactory<?> immunity = ApoliRegistries.POWER_FACTORY.method_10223(io.github.apace100.apoli.Apoli.identifier("effect_immunity"));
        if (immunity != null) {
            immunity.getSerializableData().add("effect_type", EffectCategories.CALIO, null);
            PowerFactoryAccessor accessor = (PowerFactoryAccessor) immunity;
            Function original = accessor.aea$getFactoryConstructor();
            accessor.aea$setFactoryConstructor((Function) (Object data) -> {
                BiFunction inner = (BiFunction) original.apply(data);
                EnumSet<class_4081> categories = ((SerializableData.Instance) data).get("effect_type");
                if (categories == null || categories.isEmpty()) {
                    return inner;
                }
                EnumSet<class_4081> copy = EnumSet.copyOf(categories);
                return (BiFunction) (Object type, Object entity) -> {
                    Object power = inner.apply(type, entity);
                    if (power instanceof EffectCategoryHolder holder) {
                        holder.aea$setCategories(copy);
                    }
                    return power;
                };
            });
        } else {
            AEA.LOGGER.warn("[AEA] apoli:effect_immunity not found; effect_type is not available on it.");
        }

        ConditionFactory<?> status = ApoliRegistries.ENTITY_CONDITION.method_10223(io.github.apace100.apoli.Apoli.identifier("status_effect"));
        if (status != null) {
            SerializableData data = status.getSerializableData();
            data.add("effect", SerializableDataTypes.STATUS_EFFECT, null);
            data.add("effect_type", EffectCategories.CALIO, null);
            ConditionFactoryAccessor accessor = (ConditionFactoryAccessor) status;
            BiFunction original = accessor.aea$getCondition();
            accessor.aea$setCondition((BiFunction) (Object instance, Object entity) -> {
                SerializableData.Instance d = (SerializableData.Instance) instance;
                if (d.get("effect") != null) {
                    return original.apply(instance, entity);
                }
                return anyOfType(d, entity);
            });
        } else {
            AEA.LOGGER.warn("[AEA] apoli:status_effect not found; effect_type is not available on it.");
        }
    }

    private static Boolean anyOfType(SerializableData.Instance d, Object entity) {
        EnumSet<class_4081> categories = d.get("effect_type");
        if (categories == null || categories.isEmpty() || !(entity instanceof class_1309 living)) {
            return false;
        }
        int minAmplifier = d.getInt("min_amplifier");
        int maxAmplifier = d.getInt("max_amplifier");
        int minDuration = d.getInt("min_duration");
        int maxDuration = d.getInt("max_duration");
        for (class_1293 instance : living.method_6026()) {
            if (!categories.contains(instance.method_5579().method_18792())) {
                continue;
            }
            int amplifier = instance.method_5578();
            int duration = instance.method_5584();
            if (amplifier >= minAmplifier && amplifier <= maxAmplifier && duration >= minDuration && duration <= maxDuration) {
                return true;
            }
        }
        return false;
    }

    /** Unused; keeps the generic types of Apoli's power classes referenced for the compiler. */
    @SuppressWarnings("unused")
    private static Class<?>[] types() {
        return new Class<?>[] {Power.class, PowerType.class};
    }
}
