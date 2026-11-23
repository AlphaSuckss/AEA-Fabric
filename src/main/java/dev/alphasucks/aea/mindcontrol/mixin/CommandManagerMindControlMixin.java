package dev.alphasucks.aea.mindcontrol.mixin;

import com.mojang.brigadier.ParseResults;
import dev.alphasucks.aea.mindcontrol.MindControl;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** A controller's commands run as their victim; a victim's own are blocked (the {@code commands} control). */
@Mixin(class_2170.class)
public abstract class CommandManagerMindControlMixin {
    @Inject(method = "method_9249", at = @At("HEAD"), cancellable = true)
    private void aea$mindControlCommand(ParseResults<class_2168> parse, String command, CallbackInfoReturnable<Integer> cir) {
        if (MindControl.onCommand(parse)) cir.setReturnValue(0);
    }
}
