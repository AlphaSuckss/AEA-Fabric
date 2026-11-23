package dev.alphasucks.aea.mobai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.mobai.MobPathingPower;
import net.minecraft.class_1308;
import net.minecraft.class_1408;
import net.minecraft.class_1409;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * airborne_pathing: vanilla land navigation refuses to plan or follow a path unless the mob stands on the
 * ground, so a bouncing mob froze until it stopped bouncing. With the option on, mid-air counts as a valid
 * position (the planner already starts mid-air paths from the block the mob will land on).
 */
@Mixin(class_1409.class)
public abstract class MobNavigationMixin extends class_1408 {
    protected MobNavigationMixin(class_1308 mob, class_1937 world) {
        super(mob, world);
    }

    @ModifyReturnValue(method = "method_6358", at = @At("RETURN"))
    private boolean aea$airborneIsValid(boolean original) {
        if (original || this.field_6684.method_5765()) {
            return original;
        }
        MobPathingPower.Config cfg = MobPathingPower.config(this.field_6684);
        return cfg != null && cfg.airbornePathing();
    }
}
