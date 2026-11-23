package dev.alphasucks.aea.port.mixin.disguise;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3887;
import net.minecraft.class_922;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_922.class)
@Environment(EnvType.CLIENT)
public interface LivingEntityRendererAddLayerAccessor {
   @Invoker("method_4046")
   boolean apoli$addLayer(class_3887<?, ?> var1);
}
