package dev.alphasucks.aea.port.power;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * AEA: "which powers does this source grant". On Origins 1.10.2 a power source is usually an
 * origin id, so the origin's powers are returned; anything else yields null like Overgrown's.
 */
public final class PowerSources {
    private PowerSources() {
    }

    @Nullable
    public static Set<class_2960> powersOf(class_2960 sourceId) {
        if (OriginRegistry.contains(sourceId)) {
            Origin origin = OriginRegistry.get(sourceId);
            Set<class_2960> out = new LinkedHashSet<>();
            for (io.github.apace100.apoli.power.PowerType<?> t : origin.getPowerTypes()) {
                out.add(t.getIdentifier());
            }
            return out.isEmpty() ? null : out;
        }
        return null;
    }

    public static Set<class_2960> powersOfAll(Collection<class_2960> sourceIds) {
        Set<class_2960> out = new LinkedHashSet<>();
        for (class_2960 id : sourceIds) {
            Set<class_2960> p = powersOf(id);
            if (p != null) {
                out.addAll(p);
            }
        }
        return out;
    }
}
