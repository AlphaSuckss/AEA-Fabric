package dev.alphasucks.aea.bridge;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.power.PowerType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.PowerFactory;
import net.minecraft.class_1309;
import net.minecraft.class_2960;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/** Registers one of Overgrown's power types as an Apoli 2.9.2 power factory. */
public final class BridgePowerFactory<C> extends PowerFactory<BridgedPower<C>> {
    private static final class_2960 UNKNOWN_SOURCE = new class_2960("aea", "unknown");

    private final class_2960 canonicalTypeId;
    private final PowerType<C> opoliType;
    private final boolean ticks;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public BridgePowerFactory(class_2960 id, class_2960 canonicalTypeId, PowerType<C> type, Supplier<MapCodec<C>> codec) {
        super(id, new CodecData<>(id, codec), data -> (powerType, entity) -> null);
        this.canonicalTypeId = canonicalTypeId;
        this.opoliType = type;
        this.ticks = overrides(type, "tick") || overrides(type, "tickStored");
        BridgePowerFactory<C> self = this;
        this.factoryConstructor = data -> (powerType, entity) -> new BridgedPower<>(powerType, entity, self, (C) data.get(CodecData.CFG));
        this.allowCondition();
    }

    private static boolean overrides(Object type, String name) {
        for (Class<?> c = type.getClass(); c != null && c != PowerType.class && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(name) && !m.isBridge()) {
                    return true;
                }
            }
        }
        return false;
    }

    public class_2960 canonicalTypeId() {
        return canonicalTypeId;
    }

    public PowerType<C> opoliType() {
        return opoliType;
    }

    public boolean ticks() {
        return ticks;
    }

    class_2960 lastSource(class_1309 entity, io.github.apace100.apoli.power.PowerType<?> type) {
        return PowerHolderComponent.KEY.maybeGet(entity).map(c -> {
            List<class_2960> sources = c.getSources(type);
            return sources.isEmpty() ? UNKNOWN_SOURCE : sources.get(sources.size() - 1);
        }).orElse(UNKNOWN_SOURCE);
    }
}
