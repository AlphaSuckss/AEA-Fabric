package dev.alphasucks.aea.bridge;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Context Apoli 2.9.2 does not pass to item/block/damage conditions, which Overgrown's
 * contexts expect (holder entity, world, damage target). Filled in by AEA's mixins and by
 * the bridge factories while they run.
 */
public final class AeaContext {
    private static final Object NONE = new Object();
    private static final ThreadLocal<Deque<Object>> ACTORS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<class_1309>> DAMAGE_TARGETS = ThreadLocal.withInitial(ArrayDeque::new);
    @Nullable
    private static volatile MinecraftServer server;
    private static Supplier<class_1937> clientWorld = () -> null;

    private AeaContext() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    public static void setClientWorldSupplier(Supplier<class_1937> supplier) {
        clientWorld = supplier;
    }

    @Nullable
    public static MinecraftServer server() {
        return server;
    }

    public static void pushActor(@Nullable class_1297 entity) {
        ACTORS.get().push(entity == null ? NONE : entity);
    }

    public static void popActor() {
        Deque<Object> d = ACTORS.get();
        if (!d.isEmpty()) {
            d.pop();
        }
    }

    @Nullable
    public static class_1297 actor() {
        Object e = ACTORS.get().peek();
        return e instanceof class_1297 entity ? entity : null;
    }

    @Nullable
    public static class_1309 livingActor() {
        return actor() instanceof class_1309 l ? l : null;
    }

    public static void pushDamageTarget(class_1309 target) {
        DAMAGE_TARGETS.get().push(target);
    }

    public static void popDamageTarget() {
        Deque<class_1309> d = DAMAGE_TARGETS.get();
        if (!d.isEmpty()) {
            d.pop();
        }
    }

    @Nullable
    public static class_1309 damageTarget() {
        return DAMAGE_TARGETS.get().peek();
    }

    /** Best-effort world for contexts Apoli does not provide one for. */
    @Nullable
    public static class_1937 anyWorld() {
        class_1297 a = actor();
        if (a != null) {
            return a.method_37908();
        }
        MinecraftServer s = server;
        if (s != null && s.method_18854()) {
            return s.method_30002();
        }
        return clientWorld.get();
    }
}
