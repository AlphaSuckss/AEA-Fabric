package dev.alphasucks.aea.extra.mixin;

import io.github.apace100.apoli.power.Power;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Power.class, remap = false)
public interface PowerEntityAccessor {
    @Accessor("entity")
    class_1309 aea$entity();
}
