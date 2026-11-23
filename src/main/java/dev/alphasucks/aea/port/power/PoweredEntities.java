package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.bridge.BridgedPower;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3218;

import java.util.function.Consumer;

/** AEA: entities currently holding at least one ported power (server side). */
public final class PoweredEntities {
    private PoweredEntities() {
    }

    public static void register(class_1297 entity) {
    }

    public static void unregister(class_1297 entity) {
    }

    public static void clear() {
    }

    public static void forEach(Consumer<class_1297> action) {
        for (class_1309 e : BridgedPower.holders()) {
            if (!e.method_31481() && e.method_37908() instanceof class_3218) {
                action.accept(e);
            }
        }
    }
}
