package dev.alphasucks.aea.port.data;

import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import org.jetbrains.annotations.Nullable;

public final class SlotStacks {
   public static final int SCOPE_SLOT = 0;
   public static final int SCOPE_EQUIPMENT = 1;
   public static final int SCOPE_ARMOR = 2;
   public static final int SCOPE_HANDS = 3;
   private static final class_1304[] EQUIPMENT = class_1304.values();
   private static final class_1304[] ARMOR = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   private static final class_1304[] HANDS = new class_1304[]{class_1304.field_6173, class_1304.field_6171};

   private SlotStacks() {
   }

   public static int scopeOf(String name) {
      return switch (name) {
         case "any", "all", "equipment" -> 1;
         case "armor", "armour" -> 2;
         case "hands", "held" -> 3;
         default -> -1;
      };
   }

   public static class_1304[] slotsOf(int scope) {
      return switch (scope) {
         case 2 -> ARMOR;
         case 3 -> HANDS;
         default -> EQUIPMENT;
      };
   }

   public static class_1799 get(@Nullable class_1297 entity, ItemSlot slot) {
      if (entity == null) {
         return class_1799.field_8037;
      }

      class_1304 equipment = equipmentOf(slot);
      if (equipment != null) {
         return entity instanceof class_1309 living ? living.method_6118(equipment) : class_1799.field_8037;
      }

      if (entity instanceof class_1657 player) {
         if (slot.isEnderChest()) {
            int index = slot.enderChestIndex();
            return index < player.method_7274().method_5439() ? player.method_7274().method_5438(index) : class_1799.field_8037;
         } else {
            int index = slot.toContainerIndex();
            return index >= 0 && index < player.method_31548().method_5439() ? player.method_31548().method_5438(index) : class_1799.field_8037;
         }
      } else {
         return class_1799.field_8037;
      }
   }

   @Nullable
   public static class_1304 equipmentOf(ItemSlot slot) {
      return switch (slot.index()) {
         case 98 -> class_1304.field_6173;
         case 99 -> class_1304.field_6171;
         case 100 -> class_1304.field_6166;
         case 101 -> class_1304.field_6172;
         case 102 -> class_1304.field_6174;
         case 103 -> class_1304.field_6169;
         default -> null;
      };
   }
}
