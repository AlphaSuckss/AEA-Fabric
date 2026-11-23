package dev.alphasucks.aea.port.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.class_2203;
import net.minecraft.class_2483;
import net.minecraft.class_2487;
import net.minecraft.class_2514;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_2203.class_2209;
import org.jetbrains.annotations.Nullable;

public final class NbtPathValue {
   public static final int FIRST = 0;
   public static final int SUM = 1;
   public static final int MAX = 2;
   public static final int MIN = 3;
   public static final int COUNT = 4;
   private static final class_2203 ARGUMENT = new class_2203();

   private NbtPathValue() {
   }

   public static int aggregatorOf(String name) {
      return switch (name) {
         case "first" -> 0;
         case "sum" -> 1;
         case "max" -> 2;
         case "min" -> 3;
         case "count", "size" -> 4;
         default -> -1;
      };
   }

   @Nullable
   public static class_2209 parse(String path) {
      try {
         StringReader reader = new StringReader(path);
         class_2209 parsed = ARGUMENT.method_9362(reader);
         return reader.canRead() ? null : parsed;
      } catch (CommandSyntaxException e) {
         return null;
      }
   }

   public static double read(@Nullable class_2520 root, class_2209 path, int aggregator) {
      if (root == null) {
         return 0.0;
      }

      int size = path.method_9374(root);
      if (aggregator != 4 && size != 0) {
         List<class_2520> found;
         try {
            found = path.method_9366(root);
         } catch (CommandSyntaxException e) {
            return 0.0;
         }

         if (aggregator == 0) {
            return valueOf((class_2520)found.get(0));
         }

         double result = aggregator == 2 ? Double.NEGATIVE_INFINITY : (aggregator == 3 ? Double.POSITIVE_INFINITY : 0.0);

         for (int i = 0; i < found.size(); i++) {
            double value = valueOf((class_2520)found.get(i));
            switch (aggregator) {
               case 1:
                  result += value;
                  break;
               case 2:
                  result = Math.max(result, value);
                  break;
               case 3:
                  result = Math.min(result, value);
            }
         }

         return result;
      } else {
         return size;
      }
   }

   public static double valueOf(class_2520 tag) {
      if (tag instanceof class_2514 numeric) {
         return numeric.method_10697();
      } else if (tag instanceof class_2519 string) {
         return string.method_10714().length();
      } else if (tag instanceof class_2483<?> collection) {
         return collection.size();
      } else {
         return tag instanceof class_2487 compound ? compound.method_10546() : 0.0;
      }
   }
}
