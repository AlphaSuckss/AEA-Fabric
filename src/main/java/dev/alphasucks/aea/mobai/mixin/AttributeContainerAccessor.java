package dev.alphasucks.aea.mobai.mixin;

import java.util.Map;
import net.minecraft.class_1320;
import net.minecraft.class_1324;
import net.minecraft.class_5131;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_5131.class)
public interface AttributeContainerAccessor {
    @Accessor("field_23709")
    Map<class_1320, class_1324> aea$custom();
}
