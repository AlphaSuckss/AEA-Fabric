package dev.alphasucks.aea.mobai.goal;

import net.minecraft.class_1314;
import net.minecraft.class_1379;
import net.minecraft.class_243;
import net.minecraft.class_5532;
import org.jetbrains.annotations.Nullable;

/** Vanilla wander goal with a configurable search box. */
public final class AeaWanderGoal extends class_1379 {
    private final int horizontal;
    private final int vertical;

    public AeaWanderGoal(class_1314 mob, double speed, int interval, int horizontal, int vertical) {
        super(mob, speed, Math.max(1, interval));
        this.horizontal = Math.max(1, horizontal);
        this.vertical = Math.max(1, vertical);
    }

    @Override
    @Nullable
    protected class_243 method_6302() {
        return class_5532.method_31510(this.field_6566, horizontal, vertical);
    }
}
