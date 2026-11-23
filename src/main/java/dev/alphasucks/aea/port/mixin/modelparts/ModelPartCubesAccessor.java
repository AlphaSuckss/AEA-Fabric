package dev.alphasucks.aea.port.mixin.modelparts;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_630;
import net.minecraft.class_630.class_628;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_630.class)
@Environment(EnvType.CLIENT)
public interface ModelPartCubesAccessor {
   @Accessor("field_3663")
   List<class_628> apoli$modelCubes();
}
