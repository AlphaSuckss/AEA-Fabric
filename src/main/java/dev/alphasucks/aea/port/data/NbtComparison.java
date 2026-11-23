package dev.alphasucks.aea.port.data;

import net.minecraft.class_2483;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2514;
import net.minecraft.class_2520;

public final class NbtComparison {
   private NbtComparison() {
   }

   public static boolean matches(class_2520 expected, class_2520 actual) {
      if (expected == actual) {
         return true;
      }

      if (expected == null) {
         return true;
      }

      if (actual == null) {
         return false;
      }

      if (expected instanceof class_2487 expectedCompound) {
         if (actual instanceof class_2487 actualCompound) {
            for (String key : expectedCompound.method_10541()) {
               if (!matches(expectedCompound.method_10580(key), actualCompound.method_10580(key))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else if (!(expected instanceof class_2499 expectedList)) {
         if (expected instanceof class_2514 expectedNum) {
            if (actual instanceof class_2514 actualNum) {
               return isIntegral(expected) && isIntegral(actual)
                  ? expectedNum.method_10699() == actualNum.method_10699()
                  : expectedNum.method_10697() == actualNum.method_10697();
            } else {
               return false;
            }
         } else {
            return expected.equals(actual);
         }
      } else if (!(actual instanceof class_2499 actualList)) {
         return false;
      } else {
         if (expectedList.isEmpty()) {
            return actualList.isEmpty();
         }

         for (int i = 0; i < expectedList.size(); i++) {
            class_2520 expectedElement = expectedList.method_10534(i);
            boolean found = false;

            for (int j = 0; j < actualList.size(); j++) {
               if (matches(expectedElement, actualList.method_10534(j))) {
                  found = true;
                  break;
               }
            }

            if (!found) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean sameValue(class_2520 a, class_2520 b) {
      if (a == b) {
         return true;
      }

      if (a == null || b == null) {
         return false;
      }

      if (a instanceof class_2487 aCompound) {
         if (b instanceof class_2487 bCompound && aCompound.method_10546() == bCompound.method_10546()) {
            for (String key : aCompound.method_10541()) {
               if (!sameValue(aCompound.method_10580(key), bCompound.method_10580(key))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else if (a instanceof class_2483<?> aList) {
         if (b instanceof class_2483<?> bList && aList.size() == bList.size()) {
            for (int i = 0; i < aList.size(); i++) {
               if (!sameValue((class_2520)aList.get(i), (class_2520)bList.get(i))) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else if (a instanceof class_2514 aNum) {
         if (b instanceof class_2514 bNum) {
            return isIntegral(a) && isIntegral(b) ? aNum.method_10699() == bNum.method_10699() : aNum.method_10697() == bNum.method_10697();
         } else {
            return false;
         }
      } else {
         return a.equals(b);
      }
   }

   private static boolean isIntegral(class_2520 tag) {
      byte id = tag.method_10711();
      return id == 1 || id == 2 || id == 3 || id == 4;
   }
}
