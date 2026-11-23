package dev.alphasucks.aea.mobai.goal;

import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.EnumSet;
import net.minecraft.class_1308;
import net.minecraft.class_1352;
import net.minecraft.class_1352.class_4134;

/** Runs a goal only while an entity condition (tested on the mob) holds. */
public final class GatedGoal extends class_1352 {
    private final class_1352 delegate;
    private final EntityCondition condition;
    private final class_1308 mob;

    public GatedGoal(class_1352 delegate, EntityCondition condition, class_1308 mob) {
        this.delegate = delegate;
        this.condition = condition;
        this.mob = mob;
    }

    public class_1352 delegate() {
        return delegate;
    }

    private boolean allowed() {
        try {
            return condition.test(EntityCtx.of(mob, mob.method_37908()));
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean method_6264() {
        return allowed() && delegate.method_6264();
    }

    @Override
    public boolean method_6266() {
        return allowed() && delegate.method_6266();
    }

    @Override
    public boolean method_6267() {
        return delegate.method_6267();
    }

    @Override
    public void method_6269() {
        delegate.method_6269();
    }

    @Override
    public void method_6270() {
        delegate.method_6270();
    }

    @Override
    public boolean method_38846() {
        return delegate.method_38846();
    }

    @Override
    public void method_6268() {
        delegate.method_6268();
    }

    @Override
    public void method_6265(EnumSet<class_4134> controls) {
        delegate.method_6265(controls);
    }

    @Override
    public EnumSet<class_4134> method_6271() {
        return delegate.method_6271();
    }

    @Override
    public String toString() {
        return "aea:gated(" + delegate + ")";
    }
}
