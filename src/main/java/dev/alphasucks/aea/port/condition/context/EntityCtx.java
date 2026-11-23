package dev.alphasucks.aea.port.condition.context;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;

public record EntityCtx(class_1297 entity, class_1937 level) {
   public static EntityCtx of(class_1297 entity, class_1937 level) {
      return new EntityCtx(entity, level);
   }

   public class_1309 living() {
      return this.entity instanceof class_1309 le ? le : null;
   }

   public class_1297 raw() {
      return this.entity;
   }
}
