package dev.alphasucks.aea.port.mixin.effects;

import net.minecraft.class_1058;
import net.minecraft.class_2960;
import net.minecraft.class_4075;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_4075.class)
public interface TextureAtlasHolderInvoker {
   @Invoker("method_18667")
   class_1058 apoli$invokeGetSprite(class_2960 var1);
}
