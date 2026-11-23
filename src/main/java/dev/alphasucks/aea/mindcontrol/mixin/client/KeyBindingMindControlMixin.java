package dev.alphasucks.aea.mindcontrol.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.mindcontrol.client.MindControlKeys;
import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Keeps a mind-controlled player's own key and mouse presses away from the parts the controller has taken over.
 * Runs on key events only, not every tick.
 */
@Mixin(class_304.class)
public abstract class KeyBindingMindControlMixin {
    @ModifyExpressionValue(method = "method_1420", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object aea$filterClick(Object binding) {
        return MindControlKeys.filter(binding, true);
    }

    @ModifyExpressionValue(method = "method_1416", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static Object aea$filterSet(Object binding) {
        return MindControlKeys.filter(binding, false);
    }
}
