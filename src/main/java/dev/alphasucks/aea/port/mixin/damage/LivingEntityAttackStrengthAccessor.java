package dev.alphasucks.aea.port.mixin.damage;

import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1309.class)
public interface LivingEntityAttackStrengthAccessor {
   @Accessor("field_6273")
   int apoli$getAttackStrengthTicker();

   @Accessor("field_6273")
   void apoli$setAttackStrengthTicker(int var1);
}
