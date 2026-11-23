package dev.alphasucks.aea.port.mixin.keybinding;

import net.minecraft.class_304;
import net.minecraft.class_315;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_315.class)
public interface OptionsAccessor {
   @Mutable
   @Accessor("field_1839")
   void apoli$setKeyMappings(class_304[] var1);
}
