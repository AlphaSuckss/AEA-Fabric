package dev.alphasucks.aea.port.mixin.block;

import net.minecraft.class_1838;
import net.minecraft.class_3965;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_1838.class)
public interface UseOnContextAccessor {
   @Invoker("method_30344")
   class_3965 apoli$getHitResult();
}
