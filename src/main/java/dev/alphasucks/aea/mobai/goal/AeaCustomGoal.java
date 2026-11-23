package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.class_1308;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;

/**
 * A goal built from conditions and actions (all run on the mob). Claims the given controls while running,
 * so lower-priority goals that need the same controls pause.
 */
public final class AeaCustomGoal extends class_1352 {
    private final class_1308 mob;
    private final Optional<EntityCondition> startCondition;
    private final Optional<EntityCondition> continueCondition;
    private final Optional<EntityAction> startAction;
    private final Optional<EntityAction> tickAction;
    private final Optional<EntityAction> stopAction;
    private final int interval;
    private final int duration;
    private final int cooldown;
    private int ticks;
    private long readyAt;

    public AeaCustomGoal(class_1308 mob, EnumSet<class_4134> controls, Optional<EntityCondition> startCondition,
                         Optional<EntityCondition> continueCondition, Optional<EntityAction> startAction,
                         Optional<EntityAction> tickAction, Optional<EntityAction> stopAction, int interval, int duration, int cooldown) {
        this.mob = mob;
        this.startCondition = startCondition;
        this.continueCondition = continueCondition;
        this.startAction = startAction;
        this.tickAction = tickAction;
        this.stopAction = stopAction;
        this.interval = Math.max(1, interval);
        this.duration = duration;
        this.cooldown = Math.max(0, cooldown);
        this.method_6265(controls);
    }

    private EntityCtx ctx() {
        return EntityCtx.of(mob, mob.method_37908());
    }

    private boolean test(Optional<EntityCondition> condition) {
        if (condition.isEmpty()) {
            return true;
        }
        try {
            return condition.get().test(ctx());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void run(Optional<EntityAction> action) {
        if (action.isPresent()) {
            try {
                action.get().run(ctx());
            } catch (RuntimeException ignored) {
            }
        }
    }

    @Override
    public boolean method_6264() {
        return mob.method_37908().method_8510() >= readyAt && test(startCondition);
    }

    @Override
    public boolean method_6266() {
        if (duration > 0 && ticks >= duration) {
            return false;
        }
        return test(continueCondition.isPresent() ? continueCondition : startCondition);
    }

    @Override
    public void method_6269() {
        ticks = 0;
        run(startAction);
    }

    @Override
    public void method_6270() {
        run(stopAction);
        readyAt = mob.method_37908().method_8510() + cooldown;
    }

    @Override
    public boolean method_38846() {
        return true;
    }

    @Override
    public void method_6268() {
        if (ticks++ % interval == 0) {
            run(tickAction);
        }
    }
}
