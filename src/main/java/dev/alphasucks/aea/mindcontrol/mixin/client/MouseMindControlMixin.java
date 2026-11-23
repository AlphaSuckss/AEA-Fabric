package dev.alphasucks.aea.mindcontrol.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.alphasucks.aea.mindcontrol.client.MindControlKeys;
import net.minecraft.class_312;
import net.minecraft.class_746;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_312.class)
public abstract class MouseMindControlMixin {
    /** A controlled player's mouse doesn't turn them when the controller has their look. */
    @WrapWithCondition(method = "method_1606", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5872(DD)V"))
    private boolean aea$keepLook(class_746 player, double yaw, double pitch) {
        return MindControlKeys.mayTurn();
    }

    /** The wheel picks the victim's hotbar slot while controlling, and does nothing for a victim whose hotbar is taken. */
    @Inject(method = "method_1598", at = @At("HEAD"), cancellable = true)
    private void aea$scroll(long window, double x, double y, CallbackInfo ci) {
        if (MindControlKeys.scroll(y)) ci.cancel();
    }
}
