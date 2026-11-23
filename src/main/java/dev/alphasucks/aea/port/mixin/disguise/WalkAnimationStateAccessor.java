package dev.alphasucks.aea.port.mixin.disguise;

import net.minecraft.class_8080;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_8080.class)
public interface WalkAnimationStateAccessor {
   @Accessor("field_42109")
   float apoli$getSpeedOld();

   @Accessor("field_42109")
   void apoli$setSpeedOld(float var1);

   @Accessor("field_42111")
   void apoli$setPosition(float var1);
}
