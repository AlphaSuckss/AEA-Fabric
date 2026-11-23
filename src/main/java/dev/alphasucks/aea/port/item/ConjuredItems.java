package dev.alphasucks.aea.port.item;

import net.minecraft.class_1799;
import net.minecraft.class_2487;

public final class ConjuredItems {
   public static final String TAG = "apoli:conjured";
   public static final String LOCK_TAG = "apoli:conjured_locked";

   private ConjuredItems() {
   }

   public static void mark(class_1799 stack, boolean locked) {
      class_2487 tag = stack.method_7948();
      tag.method_10556("apoli:conjured", true);
      if (locked) {
         tag.method_10556("apoli:conjured_locked", true);
      }
   }

   public static boolean isConjured(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      }

      class_2487 tag = stack.method_7969();
      return tag != null && tag.method_10545("apoli:conjured");
   }

   public static boolean isLocked(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      }

      class_2487 tag = stack.method_7969();
      return tag != null && tag.method_10545("apoli:conjured_locked");
   }
}
