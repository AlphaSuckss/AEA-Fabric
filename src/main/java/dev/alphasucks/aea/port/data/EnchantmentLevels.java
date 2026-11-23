package dev.alphasucks.aea.port.data;

import net.minecraft.class_1799;
import net.minecraft.class_1887;
import net.minecraft.class_1890;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

public final class EnchantmentLevels {
   private final class_2960 id;
   private volatile class_1887 enchantment;

   private EnchantmentLevels(class_2960 id) {
      this.id = id;
   }

   public static EnchantmentLevels of(class_2960 id) {
      return new EnchantmentLevels(id);
   }

   public int levelIn(@Nullable class_1937 level, class_1799 stack) {
      if (level != null && !stack.method_7960()) {
         class_1887 resolved = this.enchantment;
         if (resolved == null) {
            resolved = class_7923.field_41176.method_10223(this.id);
            if (resolved == null) {
               return 0;
            }

            this.enchantment = resolved;
         }

         return class_1890.method_8225(resolved, stack);
      } else {
         return 0;
      }
   }
}
