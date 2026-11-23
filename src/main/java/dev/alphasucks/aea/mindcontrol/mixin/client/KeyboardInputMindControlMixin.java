package dev.alphasucks.aea.mindcontrol.mixin.client;

import dev.alphasucks.aea.mindcontrol.client.MindControlKeys;
import net.minecraft.class_743;
import net.minecraft.class_744;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While controlling, your keys drive the victim, not your spectating self (and sneak must not stop spectating). */
@Mixin(class_743.class)
public abstract class KeyboardInputMindControlMixin {
    @Inject(method = "method_3129", at = @At("TAIL"))
    private void aea$stillWhileControlling(boolean slow, float slowFactor, CallbackInfo ci) {
        if (!MindControlKeys.controlling()) return;
        class_744 input = (class_744) (Object) this;
        input.field_3910 = input.field_3909 = input.field_3908 = input.field_3906 = input.field_3904 = input.field_3903 = false;
        input.field_3905 = input.field_3907 = 0;
    }
}
