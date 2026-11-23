package dev.alphasucks.aea.port.mixin.keybinding;

import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_304.class)
public interface KeyMappingClickCountAccessor {
   @Accessor("field_1661")
   int apoli$getClickCount();
}
