package dev.alphasucks.aea.port.mixin.sound;

import java.util.List;
import net.minecraft.class_1111;
import net.minecraft.class_1146;
import net.minecraft.class_1148;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_1146.class)
public interface WeighedSoundEventsAccessor {
   @Accessor("field_5600")
   List<class_1148<class_1111>> apoli$getList();
}
