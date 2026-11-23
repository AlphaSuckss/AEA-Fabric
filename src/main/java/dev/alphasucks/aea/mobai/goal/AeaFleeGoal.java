package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.class_11;
import net.minecraft.class_1309;
import net.minecraft.class_1314;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;
import net.minecraft.class_243;
import net.minecraft.class_5532;
import org.jetbrains.annotations.Nullable;

/** Run away from the nearest entity that matches a bi-entity condition (mob = actor). */
public final class AeaFleeGoal extends class_1352 {
    private final class_1314 mob;
    private final double range;
    private final double speed;
    private final double fastSpeed;
    private final Optional<BiEntityCondition> condition;
    @Nullable
    private class_1309 threat;
    @Nullable
    private class_11 path;

    public AeaFleeGoal(class_1314 mob, double range, double speed, double fastSpeed, Optional<BiEntityCondition> condition) {
        this.mob = mob;
        this.range = range;
        this.speed = speed;
        this.fastSpeed = fastSpeed;
        this.condition = condition;
        this.method_6265(EnumSet.of(class_4134.field_18405));
    }

    @Override
    public boolean method_6264() {
        threat = Goals.nearest(mob, range, e -> Goals.test(condition, mob, e));
        if (threat == null) {
            return false;
        }
        class_243 away = class_5532.method_31511(mob, 16, 7, threat.method_19538());
        if (away == null || threat.method_5649(away.field_1352, away.field_1351, away.field_1350) < threat.method_5858(mob)) {
            return false;
        }
        path = mob.method_5942().method_6352(away.field_1352, away.field_1351, away.field_1350, 0);
        return path != null;
    }

    @Override
    public boolean method_6266() {
        return threat != null && threat.method_5805() && !mob.method_5942().method_6357();
    }

    @Override
    public void method_6269() {
        mob.method_5942().method_6334(path, speed);
    }

    @Override
    public void method_6270() {
        threat = null;
        path = null;
    }

    @Override
    public void method_6268() {
        if (threat != null) {
            mob.method_5942().method_6344(mob.method_5858(threat) < 49.0 ? fastSpeed : speed);
        }
    }
}
