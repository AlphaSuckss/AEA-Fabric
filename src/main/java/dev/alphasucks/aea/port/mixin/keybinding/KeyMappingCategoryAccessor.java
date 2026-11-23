package dev.alphasucks.aea.port.mixin.keybinding;

import java.util.Map;
import net.minecraft.class_304;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_304.class)
public interface KeyMappingCategoryAccessor {
   @Accessor("field_1656")
   static Map<String, Integer> apoli$getCategorySortOrder() {
      throw new AssertionError("Mixin not applied");
   }
}
