package dev.alphasucks.aea.mindcontrol.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.mindcontrol.MindControl;
import net.minecraft.class_1283;
import net.minecraft.class_1309;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** "X was slain by Y (controlled by Z)" when Y was mind controlled with {@code kill_credit} on. Death only, so cold. */
@Mixin(class_1283.class)
public abstract class CombatTrackerMindControlMixin {
    @Shadow @Final private class_1309 field_5877;

    @ModifyReturnValue(method = "method_5548", at = @At("RETURN"))
    private class_2561 aea$controlledBy(class_2561 original) {
        class_1309 killer = field_5877.method_6124();
        class_3222 controller = MindControl.creditedController(killer);
        if (controller == null || controller == killer) return original;
        return original.method_27661().method_10852(class_2561.method_43469("death.aea.mind_control.controlled_by", controller.method_5476()));
    }
}
