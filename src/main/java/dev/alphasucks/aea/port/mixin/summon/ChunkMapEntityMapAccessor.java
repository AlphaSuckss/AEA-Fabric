package dev.alphasucks.aea.port.mixin.summon;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.class_3898;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_3898.class)
public interface ChunkMapEntityMapAccessor {
   @Accessor("field_18242")
   Int2ObjectMap<?> apoli$entityMap();
}
