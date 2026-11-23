package dev.alphasucks.aea.bridge;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BiomeCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.FluidCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.expr.ExprPeer;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2694;
import net.minecraft.class_2960;
import net.minecraft.class_3545;
import net.minecraft.class_3610;
import net.minecraft.class_6880;
import org.apache.commons.lang3.tuple.Triple;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Factories that expose Overgrown's action/condition types through Apoli 2.9.2's registries.
 * The JSON is decoded with the original DFU codec; the run/test logic gets Overgrown's
 * context records built from Apoli's argument types.
 */
public final class Bridges {
    private Bridges() {
    }

    private static <C> Supplier<MapCodec<C>> lazy(Supplier<MapCodec<C>> codec) {
        return Suppliers.memoize(codec::get);
    }

    @SuppressWarnings("unchecked")
    private static <C> C cfg(io.github.apace100.calio.data.SerializableData.Instance data) {
        return (C) data.get(CodecData.CFG);
    }

    /** A config that failed to sync to this client is null: the action does nothing. */
    private static <X, C> void run(ActionType<X, C> type, io.github.apace100.calio.data.SerializableData.Instance data, X ctx) {
        C config = cfg(data);
        if (config != null) {
            type.run(config, ctx);
        }
    }

    /** A config that failed to sync to this client is null: the condition is false. */
    private static <X, C> boolean test(ConditionType<X, C> type, io.github.apace100.calio.data.SerializableData.Instance data, X ctx) {
        C config = cfg(data);
        return config != null && type.test(config, ctx);
    }

    // ---------------------------------------------------------------- actions

    public static <C> ActionFactory<class_1297> entityAction(class_2960 id, ActionType<EntityCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ActionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, entity) -> {
            if (entity != null) {
                AeaContext.pushActor(entity);
                try {
                    run(type, data, EntityCtx.of(entity, entity.method_37908()));
                } finally {
                    AeaContext.popActor();
                }
            }
        });
    }

    public static <C> ActionFactory<class_3545<class_1297, class_1297>> biEntityAction(class_2960 id, ActionType<BiEntityCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ActionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, pair) -> {
            class_1297 actor = pair.method_15442();
            class_1297 target = pair.method_15441();
            if (actor == null && target == null) {
                return;
            }
            class_1297[] frame = ExprPeer.frame();
            class_1297 prevActor = frame[0];
            class_1297 prevTarget = frame[1];
            frame[0] = actor;
            frame[1] = target;
            AeaContext.pushActor(actor);
            try {
                run(type, data, BiEntityCtx.of(actor, target, worldOf(actor, target)));
            } finally {
                AeaContext.popActor();
                frame[0] = prevActor;
                frame[1] = prevTarget;
            }
        });
    }

    public static <C> ActionFactory<Triple<class_1937, class_2338, class_2350>> blockAction(class_2960 id, ActionType<BlockCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ActionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, triple) -> {
            class_1937 world = triple.getLeft();
            class_2338 pos = triple.getMiddle();
            if (world == null || pos == null) {
                return;
            }
            run(type, data, new BlockCtx(pos, world.method_8320(pos), world, AeaContext.actor(), null));
        });
    }

    public static <C> ActionFactory<class_3545<class_1937, class_1799>> itemAction(class_2960 id, ActionType<ItemCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ActionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, pair) -> {
            class_1799 stack = pair.method_15441();
            if (stack == null) {
                return;
            }
            class_1937 world = pair.method_15442() != null ? pair.method_15442() : AeaContext.anyWorld();
            run(type, data, new ItemCtx(stack, world, AeaContext.livingActor(), null));
        });
    }

    // ------------------------------------------------------------- conditions

    public static <C> ConditionFactory<class_1297> entityCondition(class_2960 id, ConditionType<EntityCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, entity) -> {
            if (entity == null) {
                return false;
            }
            AeaContext.pushActor(entity);
            try {
                return test(type, data, EntityCtx.of(entity, entity.method_37908()));
            } finally {
                AeaContext.popActor();
            }
        });
    }

    public static <C> ConditionFactory<class_3545<class_1297, class_1297>> biEntityCondition(class_2960 id, ConditionType<BiEntityCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, pair) -> {
            class_1297 actor = pair.method_15442();
            class_1297 target = pair.method_15441();
            class_1297[] frame = ExprPeer.frame();
            class_1297 prevActor = frame[0];
            class_1297 prevTarget = frame[1];
            frame[0] = actor;
            frame[1] = target;
            try {
                return test(type, data, BiEntityCtx.of(actor, target, worldOf(actor, target)));
            } finally {
                frame[0] = prevActor;
                frame[1] = prevTarget;
            }
        });
    }

    public static <C> ConditionFactory<class_2694> blockCondition(class_2960 id, ConditionType<BlockCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, cached) -> {
            class_1937 world = cached.method_11679() instanceof class_1937 w ? w : AeaContext.anyWorld();
            return test(type, data, new BlockCtx(cached.method_11683(), cached.method_11681(), world, AeaContext.actor(), null));
        });
    }

    public static <C> ConditionFactory<class_1799> itemCondition(class_2960 id, ConditionType<ItemCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)),
            (data, stack) -> stack != null && test(type, data, new ItemCtx(stack, AeaContext.anyWorld(), AeaContext.livingActor(), null)));
    }

    public static <C> ConditionFactory<class_3545<class_1282, Float>> damageCondition(class_2960 id, ConditionType<DamageCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, pair) -> {
            var target = AeaContext.damageTarget();
            class_1937 world = target != null ? target.method_37908() : AeaContext.anyWorld();
            // apoli:invulnerability (and some addons) test damage conditions with no amount: Pair(source, null)
            Float amount = pair.method_15441();
            return test(type, data, new DamageCtx(pair.method_15442(), target, world, amount != null ? amount : 0.0F));
        });
    }

    public static <C> ConditionFactory<class_6880<class_1959>> biomeCondition(class_2960 id, ConditionType<BiomeCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)), (data, biome) -> {
            class_1297 actor = AeaContext.actor();
            return test(type, data, new BiomeCtx(biome, actor != null ? actor.method_24515() : null, actor != null ? actor.method_37908() : AeaContext.anyWorld()));
        });
    }

    public static <C> ConditionFactory<class_3610> fluidCondition(class_2960 id, ConditionType<FluidCtx, C> type, Supplier<MapCodec<C>> codec) {
        return new ConditionFactory<>(id, new CodecData<>(id, lazy(codec)),
            (data, state) -> test(type, data, new FluidCtx(state, null, AeaContext.anyWorld())));
    }

    private static class_1937 worldOf(class_1297 a, class_1297 b) {
        if (a != null) {
            return a.method_37908();
        }
        return b != null ? b.method_37908() : AeaContext.anyWorld();
    }

    public static <A, B> Function<A, B> unsupported(String what) {
        return a -> {
            throw new UnsupportedOperationException(what);
        };
    }
}
