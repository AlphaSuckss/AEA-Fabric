package dev.alphasucks.aea.port.data;

import net.minecraft.class_2561;
import net.minecraft.class_5250;

public final class ItemNames {
   private ItemNames() {
   }

   public static class_2561 upright(class_2561 name) {
      if (name.method_10866().method_10966()) {
         return name;
      }

      class_5250 copy = name.method_27661();
      copy.method_10862(copy.method_10866().method_10978(false));
      return copy;
   }
}
