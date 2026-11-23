package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;
import org.jetbrains.annotations.Nullable;

/** Look at the nearest living entity that matches a bi-entity condition (mob = actor). */
public final class AeaLookAtGoal extends class_1352 {
    private final class_1308 mob;
    private final double range;
    private final float chance;
    private final Optional<BiEntityCondition> condition;
    @Nullable
    private class_1309 target;
    private int lookTime;

    public AeaLookAtGoal(class_1308 mob, double range, float chance, Optional<BiEntityCondition> condition) {
        this.mob = mob;
        this.range = range;
        this.chance = chance;
        this.condition = condition;
        this.method_6265(EnumSet.of(class_4134.field_18406));
    }

    @Override
    public boolean method_6264() {
        if (mob.method_6051().method_43057() >= chance) {
            return false;
        }
        target = Goals.nearest(mob, range, e -> Goals.test(condition, mob, e));
        return target != null;
    }

    @Override
    public boolean method_6266() {
        return target != null && target.method_5805() && mob.method_5858(target) <= range * range && lookTime > 0;
    }

    @Override
    public void method_6269() {
        lookTime = 40 + mob.method_6051().method_43048(40);
    }

    @Override
    public void method_6270() {
        target = null;
    }

    @Override
    public void method_6268() {
        if (target != null && target.method_5805()) {
            mob.method_5988().method_20248(target.method_23317(), target.method_23320(), target.method_23321());
            lookTime--;
        }
    }
}
