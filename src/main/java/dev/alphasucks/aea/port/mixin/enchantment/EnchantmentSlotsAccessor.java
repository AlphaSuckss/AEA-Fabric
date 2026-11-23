package dev.alphasucks.aea.port.mixin.enchantment;

import net.minecraft.class_1304;
import net.minecraft.class_1887;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1887.class)
public interface EnchantmentSlotsAccessor {
   @Accessor("field_9086")
   class_1304[] apoli$getSlots();
}
