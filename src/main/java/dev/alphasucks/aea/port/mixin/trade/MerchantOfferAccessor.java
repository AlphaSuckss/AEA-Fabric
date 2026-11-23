package dev.alphasucks.aea.port.mixin.trade;

import net.minecraft.class_1914;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1914.class)
public interface MerchantOfferAccessor {
   @Accessor("field_9147")
   void apoli$setUses(int var1);
}
