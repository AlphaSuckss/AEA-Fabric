package dev.alphasucks.aea.redstone.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.redstone.RedstonePulses;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_8235;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds {@link RedstonePulses} to the two signal queries every other one is built on ({@code getEmittedRedstonePower}
 * and {@code getStrongRedstonePower}). These run constantly; the handler returns straight away while no pulse exists.
 */
@Mixin(class_8235.class)
public interface RedstoneViewPulseMixin {
    @ModifyReturnValue(method = "method_49808", at = @At("RETURN"))
    private int aea$pulseSignal(int original, class_2338 pos, class_2350 direction) {
        return RedstonePulses.signal((class_8235) this, pos, direction, original, false);
    }

    @ModifyReturnValue(method = "method_49805", at = @At("RETURN"))
    private int aea$pulseDirectSignal(int original, class_2338 pos, class_2350 direction) {
        return RedstonePulses.signal((class_8235) this, pos, direction, original, true);
    }
}
