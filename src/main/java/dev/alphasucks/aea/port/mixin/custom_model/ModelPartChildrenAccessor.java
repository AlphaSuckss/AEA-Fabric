package dev.alphasucks.aea.port.mixin.custom_model;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_630;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_630.class)
@Environment(EnvType.CLIENT)
public interface ModelPartChildrenAccessor {
   @Accessor("field_3661")
   Map<String, class_630> apoli$children();
}
