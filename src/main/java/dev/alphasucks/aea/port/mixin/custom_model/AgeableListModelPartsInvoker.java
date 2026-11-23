package dev.alphasucks.aea.port.mixin.custom_model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4592;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_4592.class)
@Environment(EnvType.CLIENT)
public interface AgeableListModelPartsInvoker {
   @Invoker("method_22946")
   Iterable<class_630> apoli$headParts();

   @Invoker("method_22948")
   Iterable<class_630> apoli$bodyParts();
}
