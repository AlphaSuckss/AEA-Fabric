package dev.alphasucks.aea.mindcontrol.mixin.client;

import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Raw access, so relayed keys are held as-is even when the player uses toggle sneak or toggle sprint. */
@Mixin(class_304.class)
public interface KeyBindingMindControlAccessor {
    @Accessor("field_1653")
    void aea$setPressedRaw(boolean pressed);

    @Accessor("field_1661")
    int aea$timesPressed();

    @Accessor("field_1661")
    void aea$setTimesPressed(int times);
}
