package dev.alphasucks.aea.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3545;
import org.jetbrains.annotations.Nullable;

/**
 * {@code aea:velocity_immunity}: while active, {@code apoli:add_velocity} (and its {@code origins:}/{@code sync:} aliases)
 * does nothing to this entity.
 * <pre>
 * {
 *   "type": "aea:velocity_immunity",
 *   "entity": true,              // block the entity action (default true)
 *   "bientity": true,            // block the bi-entity action when this entity is the target (default true)
 *   "bientity_condition": {...}, // optional: only block bi-entity pushes whose actor/target pair matches
 *   "condition": {...}           // optional: usual power condition
 * }
 * </pre>
 */
public class VelocityImmunityPower extends Power {
    public static final class_2960 ID = new class_2960("aea", "velocity_immunity");
    private static final class_2960 ADD_VELOCITY = new class_2960("apoli", "add_velocity");

    private final boolean blockEntity;
    private final boolean blockBiEntity;
    @Nullable
    private final Predicate<class_3545<class_1297, class_1297>> biEntityCondition;

    public VelocityImmunityPower(PowerType<?> type, class_1309 entity, boolean blockEntity, boolean blockBiEntity,
                                 @Nullable Predicate<class_3545<class_1297, class_1297>> biEntityCondition) {
        super(type, entity);
        this.blockEntity = blockEntity;
        this.blockBiEntity = blockBiEntity;
        this.biEntityCondition = biEntityCondition;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static PowerFactory<VelocityImmunityPower> createFactory() {
        return new PowerFactory<VelocityImmunityPower>(
            ID,
            new SerializableData()
                .add("entity", SerializableDataTypes.BOOLEAN, true)
                .add("bientity", SerializableDataTypes.BOOLEAN, true)
                .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            data -> (type, entity) -> new VelocityImmunityPower(
                type, entity,
                data.getBoolean("entity"),
                data.getBoolean("bientity"),
                (Predicate<class_3545<class_1297, class_1297>>) data.get("bientity_condition"))
        ).allowCondition();
    }

    public static void register() {
        if (!ApoliRegistries.POWER_FACTORY.method_10250(ID)) {
            class_2378.method_10230(ApoliRegistries.POWER_FACTORY, ID, createFactory());
        }
    }

    /** True when the action being run is apoli:add_velocity (aliases resolve to the same factory id). */
    public static boolean isAddVelocity(class_2960 actionId) {
        return ADD_VELOCITY.equals(actionId);
    }

    /** Called with the action's argument: an Entity for the entity action, a Pair(actor, target) for the bi-entity one. */
    public static boolean blocks(Object argument) {
        if (argument instanceof class_1297 entity) {
            return immune(entity, null, false);
        }
        if (argument instanceof class_3545<?, ?> pair && pair.method_15441() instanceof class_1297 target) {
            class_1297 actor = pair.method_15442() instanceof class_1297 a ? a : null;
            return immune(target, actor, true);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static boolean immune(class_1297 target, @Nullable class_1297 actor, boolean biEntity) {
        if (!(target instanceof class_1309)) {
            return false;
        }
        for (VelocityImmunityPower power : PowerHolderComponent.getPowers(target, VelocityImmunityPower.class)) {
            if (biEntity) {
                if (power.blockBiEntity && (power.biEntityCondition == null
                    || power.biEntityCondition.test(new class_3545<>(actor, target)))) {
                    return true;
                }
            } else if (power.blockEntity) {
                return true;
            }
        }
        return false;
    }
}
