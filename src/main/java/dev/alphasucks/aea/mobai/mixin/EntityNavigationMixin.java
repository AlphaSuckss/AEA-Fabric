package dev.alphasucks.aea.mobai.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.mobai.MobPathingPower;
import net.minecraft.class_11;
import net.minecraft.class_1308;
import net.minecraft.class_1408;
import net.minecraft.class_2382;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.class_2338;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.At;

/**
 * airborne_pathing: a path point normally only counts as reached when the mob is within 1 block of its height.
 * A bouncing mob flies over its points, so while airborne it counts as reached when the mob is above it
 * (up to airborne_reach blocks).
 */
@Mixin(class_1408.class)
public abstract class EntityNavigationMixin implements dev.alphasucks.aea.mobai.Repathable {
    @Shadow
    @Final
    protected class_1308 field_6684;

    @Shadow
    protected class_11 field_6681;

    @Shadow
    protected double field_6668;

    @Shadow
    @Nullable
    private class_2338 field_20293;

    @Shadow
    private int field_20294;

    @Shadow
    @Nullable
    public abstract class_11 method_6348(class_2338 target, int distance);

    @Shadow
    public abstract boolean method_6334(@Nullable class_11 path, double speed);

    @Shadow
    protected abstract boolean method_6358();

    /**
     * repath_interval: plan a fresh route to the current destination now (no 20-tick throttle, and not skipped by
     * mods that disable vanilla's recomputePath).
     */
    @Override
    public void aea$repath() {
        if (this.field_20293 == null || this.field_6681 == null || this.field_6681.method_46() || !this.method_6358()) {
            return;
        }
        double speed = this.field_6668;
        class_2338 target = this.field_20293;
        int distance = this.field_20294;
        class_11 previous = this.field_6681;
        this.field_6681 = null;
        class_11 path = this.method_6348(target, distance);
        if (path == null) {
            this.field_6681 = previous; // keep going on the old route rather than stopping
            return;
        }
        this.method_6334(path, speed);
    }

    @ModifyExpressionValue(method = "method_6339", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D", ordinal = 1))
    private double aea$airborneReach(double dy) {
        if (dy < 1.0 || this.field_6684.method_24828() || this.field_6681 == null) {
            return dy;
        }
        MobPathingPower.Config cfg = MobPathingPower.config(this.field_6684);
        if (cfg == null || !cfg.airbornePathing()) {
            return dy;
        }
        class_2382 node = this.field_6681.method_31032();
        double above = this.field_6684.method_23318() - node.method_10264();
        return above >= -0.5 && above <= cfg.airborneReach() ? 0.0 : dy;
    }
}
