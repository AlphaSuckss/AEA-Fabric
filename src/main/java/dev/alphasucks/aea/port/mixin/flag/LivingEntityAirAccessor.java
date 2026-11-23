package dev.alphasucks.aea.port.mixin.flag;

import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_1309.class)
public interface LivingEntityAirAccessor {
   @Invoker("method_6064")
   int apoli$increaseAirSupply(int var1);

   @Invoker("method_6130")
   int apoli$decreaseAirSupply(int var1);
}
