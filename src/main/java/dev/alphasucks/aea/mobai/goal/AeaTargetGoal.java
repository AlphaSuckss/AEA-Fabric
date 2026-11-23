package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;
import net.minecraft.class_5134;
import org.jetbrains.annotations.Nullable;

/** Target the nearest living entity that matches a bi-entity condition (mob = actor, candidate = target). */
public final class AeaTargetGoal extends class_1352 {
    private static final int UNSEEN_MEMORY = 60;
    private final class_1308 mob;
    private final Optional<Double> range;
    private final boolean mustSee;
    private final int interval;
    private final Optional<BiEntityCondition> condition;
    @Nullable
    private class_1309 target;
    private int unseen;
    private int recheck;

    public AeaTargetGoal(class_1308 mob, Optional<Double> range, boolean mustSee, int interval, Optional<BiEntityCondition> condition) {
        this.mob = mob;
        this.range = range;
        this.mustSee = mustSee;
        this.interval = Math.max(1, interval);
        this.condition = condition;
        this.method_6265(EnumSet.of(class_4134.field_18408));
    }

    private double range() {
        if (range.isPresent()) {
            return range.get();
        }
        try {
            return mob.method_26825(class_5134.field_23717);
        } catch (RuntimeException e) {
            return 16.0;
        }
    }

    private boolean valid(class_1309 candidate) {
        return mob.method_18395(candidate) && Goals.test(condition, mob, candidate)
            && (!mustSee || mob.method_5985().method_6369(candidate));
    }

    @Override
    public boolean method_6264() {
        if (interval > 1 && mob.method_6051().method_43048(interval) != 0) {
            return false;
        }
        target = Goals.nearest(mob, range(), this::valid);
        return target != null;
    }

    @Override
    public boolean method_6266() {
        class_1309 current = mob.method_5968();
        if (current == null || current != target || !current.method_5805() || !current.method_36608() || !mob.method_18395(current)) {
            return false;
        }
        double r = range();
        if (mob.method_5858(current) > r * r) {
            return false;
        }
        if (mustSee) {
            if (mob.method_5985().method_6369(current)) {
                unseen = 0;
            } else if (++unseen > UNSEEN_MEMORY) {
                return false;
            }
        }
        if (--recheck <= 0) {
            recheck = 20;
            return Goals.test(condition, mob, current);
        }
        return true;
    }

    @Override
    public void method_6269() {
        mob.method_5980(target);
        unseen = 0;
        recheck = 20;
    }

    @Override
    public void method_6270() {
        if (mob.method_5968() == target) {
            mob.method_5980(null);
        }
        target = null;
    }
}
