package dev.alphasucks.aea.port.attribution;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.compat.ModCompat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class PowerCause {
   public static final boolean ACTIVE = ModCompat.LEDGER || ModCompat.INDEXOR;
   public static final int MAX_LABEL_LENGTH = 30;
   private static final int DEPTH_LIMIT = 16;
   private static final class_1297[] HOLDERS = new class_1297[16];
   private static final class_2960[] POWERS = new class_2960[16];
   private static final List<PowerCause.Describer> DESCRIBERS = new CopyOnWriteArrayList();
   private static int depth;
   private static volatile boolean tracking = ACTIVE;

   public static void armAttribution() {
      tracking = true;
   }

   public static boolean tracking() {
      return tracking;
   }

   private PowerCause() {
   }

   public static void bootstrap() {
      if (ACTIVE) {
         Apoli.LOGGER
            .info(
               "[Apoli] Block-logger compatibility active ({}): world edits made by a power are labelled with the power (and its origin) that caused them.",
               ModCompat.LEDGER ? "Ledger" : "Indexor"
            );
      }
   }

   public static void addDescriber(PowerCause.Describer describer) {
      DESCRIBERS.add(describer);
   }

   public static boolean push(@Nullable class_1297 holder, @Nullable class_2960 powerId) {
      if (!tracking) {
         return false;
      }

      if (holder == null || powerId == null || depth >= 16) {
         return false;
      }

      if (!(holder.method_37908() instanceof class_3218)) {
         return false;
      }

      HOLDERS[depth] = holder;
      POWERS[depth] = powerId;
      depth++;
      return true;
   }

   public static void pop() {
      if (depth != 0) {
         depth--;
         HOLDERS[depth] = null;
         POWERS[depth] = null;
      }
   }

   public static void clear() {
      while (depth > 0) {
         pop();
      }
   }

   @Nullable
   public static class_1297 holder() {
      return depth == 0 ? null : HOLDERS[depth - 1];
   }

   @Nullable
   public static class_2960 powerId() {
      return depth == 0 ? null : POWERS[depth - 1];
   }

   @Nullable
   public static String label() {
      if (ACTIVE && depth != 0) {
         class_1297 holder = HOLDERS[depth - 1];
         class_2960 powerId = POWERS[depth - 1];
         if (holder != null && powerId != null) {
            for (PowerCause.Describer describer : DESCRIBERS) {
               String described = describer.describe(holder, powerId);
               if (described != null && !described.isEmpty()) {
                  return fit(described);
               }
            }

            return fit(powerId.method_12832());
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static String fit(String raw) {
      int length = raw.length();
      return length <= 30 ? raw : raw.substring(length - 30);
   }

   @FunctionalInterface
   public interface Describer {
      @Nullable
      String describe(class_1297 var1, class_2960 var2);
   }
}
