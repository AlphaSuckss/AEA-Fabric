package dev.alphasucks.aea.mindcontrol.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.alphasucks.aea.mindcontrol.MindControlled;
import net.minecraft.class_1308;
import net.minecraft.class_1333;
import net.minecraft.class_1334;
import net.minecraft.class_1335;
import net.minecraft.class_1355;
import net.minecraft.class_1408;
import net.minecraft.class_1413;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * While a player controls a mob, its AI step does nothing: no senses, goals, pathing, brain or move/look/jump control.
 * {@code MindControl} sets its movement instead. Each call is skipped on its own, so nothing is allocated per tick.
 */
@Mixin(class_1308.class)
public abstract class MobMindControlMixin implements MindControlled {
    @Unique private boolean aea$mindControlled;

    @Override
    public boolean aea$mindControlled() {
        return aea$mindControlled;
    }

    @Override
    public void aea$setMindControlled(boolean controlled) {
        aea$mindControlled = controlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1413;method_6370()V"))
    private boolean aea$sensing(class_1413 cache) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1355;method_38849(Z)V"))
    private boolean aea$runningGoals(class_1355 selector, boolean everyTick) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1355;method_6275()V"))
    private boolean aea$goals(class_1355 selector) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1408;method_6360()V"))
    private boolean aea$navigation(class_1408 navigation) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1308;method_5958()V"))
    private boolean aea$customAi(class_1308 mob) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1335;method_6240()V"))
    private boolean aea$move(class_1335 control) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1333;method_6231()V"))
    private boolean aea$look(class_1333 control) {
        return !aea$mindControlled;
    }

    @WrapWithCondition(method = "method_6023", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1334;method_6234()V"))
    private boolean aea$jump(class_1334 control) {
        return !aea$mindControlled;
    }
}
