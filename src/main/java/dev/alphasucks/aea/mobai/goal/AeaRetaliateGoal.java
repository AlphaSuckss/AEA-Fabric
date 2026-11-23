package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;
import org.jetbrains.annotations.Nullable;

/** Target whatever last hurt the mob (if it matches the bi-entity condition); optionally alert nearby mobs of the same type. */
public final class AeaRetaliateGoal extends class_1352 {
    private final class_1308 mob;
    private final Optional<BiEntityCondition> condition;
    private final boolean alertOthers;
    private final double alertRange;
    private int lastHurt;
    @Nullable
    private class_1309 target;

    public AeaRetaliateGoal(class_1308 mob, Optional<BiEntityCondition> condition, boolean alertOthers, double alertRange) {
        this.mob = mob;
        this.condition = condition;
        this.alertOthers = alertOthers;
        this.alertRange = alertRange;
        this.method_6265(EnumSet.of(class_4134.field_18408));
    }

    @Override
    public boolean method_6264() {
        int hurtAt = mob.method_6117();
        class_1309 attacker = mob.method_6065();
        if (hurtAt == lastHurt || attacker == null || !attacker.method_5805() || !attacker.method_36608() || !mob.method_18395(attacker)
            || !Goals.test(condition, mob, attacker)) {
            return false;
        }
        target = attacker;
        return true;
    }

    @Override
    public boolean method_6266() {
        class_1309 current = mob.method_5968();
        return current != null && current == target && current.method_5805() && current.method_36608() && mob.method_18395(current)
            && mob.method_5858(current) <= 32.0 * 32.0;
    }

    @Override
    public void method_6269() {
        lastHurt = mob.method_6117();
        mob.method_5980(target);
        if (alertOthers && target != null) {
            for (class_1308 ally : mob.method_37908().method_8390(class_1308.class, mob.method_5829().method_1014(alertRange),
                e -> e != mob && e.method_5864() == mob.method_5864() && e.method_5968() == null && e.method_5805())) {
                if (ally.method_18395(target)) {
                    ally.method_5980(target);
                }
            }
        }
    }

    @Override
    public void method_6270() {
        if (mob.method_5968() == target) {
            mob.method_5980(null);
        }
        target = null;
    }
}
