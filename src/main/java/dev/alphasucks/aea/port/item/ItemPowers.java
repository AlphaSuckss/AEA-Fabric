package dev.alphasucks.aea.port.item;

import dev.alphasucks.aea.port.data.EquipmentSlot;
import java.util.LinkedHashSet;
import java.util.OptionalInt;
import java.util.Set;
import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/**
 * Overgrown's item power NBT: {@code Powers: [{Id: "ns:power", Slot: "mainhand", Hidden?, Negative?, Value?}]}.
 * AEA: Apoli 2.9.2 keeps its own entries in the same list as {@code {Power: "ns:power", Slot, Hidden, Negative}};
 * those are granted by Apoli itself (source {@code apoli:<slot>}) and are skipped here, except by {@link #remove}.
 */
public final class ItemPowers {
   public static final String POWERS = "Powers";
   /** AEA: Apoli 2.9.2's key for the power id of its own entries. */
   public static final String APOLI_POWER = "Power";
   public static final String ID = "Id";
   public static final String SLOT = "Slot";
   public static final String HIDDEN = "Hidden";
   public static final String NEGATIVE = "Negative";
   public static final String VALUE = "Value";

   private ItemPowers() {
   }

   @Nullable
   public static class_2499 read(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_2487 tag = stack.method_7969();
         if (tag != null && tag.method_10573("Powers", 9)) {
            class_2499 list = tag.method_10554("Powers", 10);
            return list.isEmpty() ? null : list;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static Set<class_2960> powerIdsForSlot(class_1799 stack, EquipmentSlot slot) {
      class_2499 list = read(stack);
      if (list == null) {
         return Set.of();
      }

      String wantedSlot = slot.method_15434();
      Set<class_2960> out = null;

      for (class_2520 entry : list) {
         // AEA: entries without an Id are Apoli 2.9.2's (tryParse("") would yield minecraft:)
         if (entry instanceof class_2487 c && c.method_10573("Id", 8) && wantedSlot.equals(c.method_10558("Slot"))) {
            class_2960 id = class_2960.method_12829(c.method_10558("Id"));
            if (id != null) {
               if (out == null) {
                  out = new LinkedHashSet();
               }

               out.add(id);
            }
         }
      }

      return out == null ? Set.of() : out;
   }

   public static void add(class_1799 stack, class_2960 power, EquipmentSlot slot, boolean hidden, boolean negative) {
      String id = power.toString();
      String slotName = slot.method_15434();
      class_2487 tag = stack.method_7948();
      class_2499 list = tag.method_10554("Powers", 10);

      for (class_2520 entry : list) {
         if (entry instanceof class_2487 c && id.equals(c.method_10558("Id")) && slotName.equals(c.method_10558("Slot"))) {
            return;
         }
      }

      class_2487 e = new class_2487();
      e.method_10582("Id", id);
      e.method_10582("Slot", slotName);
      if (hidden) {
         e.method_10556("Hidden", true);
      }

      if (negative) {
         e.method_10556("Negative", true);
      }

      list.add(e);
      tag.method_10566("Powers", list);
   }

   public static OptionalInt readValue(class_1799 stack, class_2960 power, EquipmentSlot slot) {
      class_2499 list = read(stack);
      if (list == null) {
         return OptionalInt.empty();
      }

      String id = power.toString();
      String slotName = slot.method_15434();

      for (class_2520 entry : list) {
         if (entry instanceof class_2487 c && id.equals(c.method_10558("Id")) && slotName.equals(c.method_10558("Slot"))) {
            return c.method_10573("Value", 3) ? OptionalInt.of(c.method_10550("Value")) : OptionalInt.empty();
         }
      }

      return OptionalInt.empty();
   }

   public static void saveValue(class_1799 stack, class_2960 power, EquipmentSlot slot, int value) {
      if (stack != null && !stack.method_7960()) {
         class_2487 tag = stack.method_7969();
         if (tag != null && tag.method_10573("Powers", 9)) {
            String id = power.toString();
            String slotName = slot.method_15434();

            for (class_2520 entry : tag.method_10554("Powers", 10)) {
               if (entry instanceof class_2487 c && id.equals(c.method_10558("Id")) && slotName.equals(c.method_10558("Slot"))) {
                  c.method_10569("Value", value);
                  return;
               }
            }
         }
      }
   }

   public static void remove(class_1799 stack, class_2960 power, @Nullable EquipmentSlot slot) {
      class_2487 tag = stack.method_7969();
      if (tag != null && tag.method_10573("Powers", 9)) {
         String id = power.toString();
         String slotName = slot == null ? null : slot.method_15434();
         class_2499 list = tag.method_10554("Powers", 10);
         // AEA: also drops Apoli 2.9.2 entries for the power, so removal works whichever format added it
         list.removeIf(
            entry -> entry instanceof class_2487 c
               && (id.equals(c.method_10558("Id")) || id.equals(c.method_10558(APOLI_POWER)))
               && (slotName == null || slotName.equals(c.method_10558("Slot")))
         );
         if (list.isEmpty()) {
            tag.method_10551("Powers");
            if (tag.method_33133()) {
               stack.method_7980(null);
            }
         } else {
            tag.method_10566("Powers", list);
         }
      }
   }

   @Nullable
   public static EquipmentSlot slotByName(String name) {
      for (EquipmentSlot s : EquipmentSlot.values()) {
         if (s.method_15434().equals(name)) {
            return s;
         }
      }

      return null;
   }
}
