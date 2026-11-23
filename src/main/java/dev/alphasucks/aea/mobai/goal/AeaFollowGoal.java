package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;
import net.minecraft.class_6025;
import org.jetbrains.annotations.Nullable;

/**
 * Follow the owner (tamed mobs, clones, minions) or the nearest entity matching a bi-entity condition.
 * Starts when farther than start_distance, stops at stop_distance, optionally teleports when very far.
 */
public final class AeaFollowGoal extends class_1352 {
    private final class_1308 mob;
    private final boolean owner;
    private final Optional<BiEntityCondition> condition;
    private final double speed;
    private final double startDistance;
    private final double stopDistance;
    private final double range;
    private final double teleportDistance;
    @Nullable
    private class_1309 leader;
    private int repath;

    public AeaFollowGoal(class_1308 mob, boolean owner, Optional<BiEntityCondition> condition, double speed, double startDistance,
                         double stopDistance, double range, double teleportDistance) {
        this.mob = mob;
        this.owner = owner;
        this.condition = condition;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.range = range;
        this.teleportDistance = teleportDistance;
        this.method_6265(EnumSet.of(class_4134.field_18405, class_4134.field_18406));
    }

    @Nullable
    private class_1309 findLeader() {
        if (owner) {
            class_1297 o = mob instanceof class_6025 ownable ? ownable.method_35057() : null;
            if (o instanceof class_1309 living && living.method_5805() && living.method_37908() == mob.method_37908()
                && Goals.test(condition, mob, living)) {
                return living;
            }
            return null;
        }
        return Goals.nearest(mob, range, e -> Goals.test(condition, mob, e));
    }

    @Override
    public boolean method_6264() {
        class_1309 found = findLeader();
        if (found == null || mob.method_5858(found) < startDistance * startDistance) {
            return false;
        }
        leader = found;
        return true;
    }

    @Override
    public boolean method_6266() {
        if (leader == null || !leader.method_5805() || leader.method_37908() != mob.method_37908()) {
            return false;
        }
        double d = mob.method_5858(leader);
        return d > stopDistance * stopDistance && (owner || d <= range * range * 4.0);
    }

    @Override
    public void method_6269() {
        repath = 0;
    }

    @Override
    public void method_6270() {
        leader = null;
        mob.method_5942().method_6340();
    }

    @Override
    public void method_6268() {
        if (leader == null) {
            return;
        }
        mob.method_5988().method_6226(leader, 10.0F, mob.method_5978());
        if (--repath > 0) {
            return;
        }
        repath = 10;
        if (teleportDistance > 0.0 && mob.method_5858(leader) >= teleportDistance * teleportDistance && !mob.method_5765()) {
            mob.method_5808(leader.method_23317(), leader.method_23318(), leader.method_23321(), mob.method_36454(), mob.method_36455());
            mob.method_5942().method_6340();
            return;
        }
        mob.method_5942().method_6335(leader, speed);
    }
}
