package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import org.jetbrains.annotations.Nullable;

final class Goals {
    private Goals() {
    }

    static boolean test(Optional<BiEntityCondition> condition, class_1308 mob, class_1309 other) {
        if (condition.isEmpty()) {
            return true;
        }
        try {
            return condition.get().test(BiEntityCtx.of(mob, other, mob.method_37908()));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Nearest living, in-game entity within range (not the mob itself) that passes the filter. */
    @Nullable
    static class_1309 nearest(class_1308 mob, double range, Predicate<class_1309> filter) {
        List<class_1309> found = mob.method_37908().method_8390(class_1309.class, mob.method_5829().method_1014(range),
            e -> e != mob && e.method_5805() && e.method_36608());
        class_1309 best = null;
        double bestDistance = range * range;
        for (class_1309 candidate : found) {
            double d = mob.method_5858(candidate);
            if (d <= bestDistance && filter.test(candidate)) {
                best = candidate;
                bestDistance = d;
            }
        }
        return best;
    }
}
