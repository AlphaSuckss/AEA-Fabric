package dev.alphasucks.aea.port.condition.context;

import java.util.function.Consumer;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import org.jetbrains.annotations.Nullable;

public record ItemCtx(class_1799 stack, class_1937 level, @Nullable class_1309 holder, @Nullable Consumer<class_1799> replacer) {
   public ItemCtx(class_1799 stack, class_1937 level, @Nullable class_1309 holder) {
      this(stack, level, holder, null);
   }

   public boolean replace(class_1799 replacement) {
      if (this.replacer == null) {
         return false;
      }

      this.replacer.accept(replacement);
      return true;
   }
}
