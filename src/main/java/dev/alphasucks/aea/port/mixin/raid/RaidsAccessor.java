package dev.alphasucks.aea.port.mixin.raid;

import java.util.Map;
import net.minecraft.class_2338;
import net.minecraft.class_3218;
import net.minecraft.class_3765;
import net.minecraft.class_3767;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_3767.class)
public interface RaidsAccessor {
   @Accessor("field_16639")
   Map<Integer, class_3765> apoli$raidMap();

   @Invoker("method_16532")
   class_3765 apoli$getOrCreateRaid(class_3218 var1, class_2338 var2);
}
