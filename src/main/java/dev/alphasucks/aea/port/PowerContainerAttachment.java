package dev.alphasucks.aea.port;

import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import net.minecraft.class_1297;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/** AEA: Overgrown's attachment-backed container is replaced by a view over Apoli 2.9.2's component. */
public final class PowerContainerAttachment {
    private PowerContainerAttachment() {
    }

    public static void setClientLookup(Function<class_1297, PowerContainer> lookup) {
    }

    @Nullable
    public static PowerContainer get(class_1297 entity) {
        return PowerContainerImpl.of(entity);
    }

    @Nullable
    public static PowerContainer getOrCreate(class_1297 entity) {
        return PowerContainerImpl.of(entity);
    }
}
