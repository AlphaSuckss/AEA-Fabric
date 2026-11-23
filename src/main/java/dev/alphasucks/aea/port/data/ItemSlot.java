package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;

public record ItemSlot(String name, int index) {
   public static final int OFFHAND = 99;
   public static final int MAINHAND = 98;
   public static final int FEET = 100;
   public static final int LEGS = 101;
   public static final int CHEST = 102;
   public static final int HEAD = 103;
   public static final Codec<ItemSlot> CODEC = Codec.STRING.comapFlatMap(ItemSlot::parse, ItemSlot::name);

   public static DataResult<ItemSlot> parse(String s) {
      String trimmed = s.trim();
      int dot = trimmed.indexOf(46);
      String head = dot < 0 ? trimmed : trimmed.substring(0, dot);
      String tail = dot < 0 ? "" : trimmed.substring(dot + 1);

      return switch (head) {
         case "weapon" -> tail.isEmpty() || tail.equals("mainhand")
            ? DataResult.success(new ItemSlot(s, 98))
            : (tail.equals("offhand") ? DataResult.success(new ItemSlot(s, 99)) : DataResult.error(() -> "Unknown weapon slot: " + s));
         case "armor" -> {
            switch (tail) {
               case "head":
                  yield DataResult.success(new ItemSlot(s, 103));
               case "chest":
                  yield DataResult.success(new ItemSlot(s, 102));
               case "legs":
                  yield DataResult.success(new ItemSlot(s, 101));
               case "feet":
                  yield DataResult.success(new ItemSlot(s, 100));
               default:
                  yield DataResult.error(() -> "Unknown armor slot: " + s);
            }
         }
         case "hotbar" -> parseInt(tail, 0, 8, n -> new ItemSlot(s, n));
         case "inventory" -> parseInt(tail, 0, 26, n -> new ItemSlot(s, n + 9));
         case "container" -> parseInt(tail, 0, 53, n -> new ItemSlot(s, n));
         case "enderchest" -> parseInt(tail, 0, 26, n -> new ItemSlot(s, n + 200));
         case "horse" -> {
            switch (tail) {
               case "saddle":
                  yield DataResult.success(new ItemSlot(s, 400));
               case "armor":
                  yield DataResult.success(new ItemSlot(s, 401));
               default:
                  yield parseInt(tail, 0, 14, n -> new ItemSlot(s, n + 500));
            }
         }
         case "villager" -> parseInt(tail, 0, 7, n -> new ItemSlot(s, n + 300));
         default -> DataResult.error(() -> "Unknown slot: " + s);
      };
   }

   private static DataResult<ItemSlot> parseInt(String s, int min, int max, ItemSlot.SlotBuilder b) {
      try {
         int n = Integer.parseInt(s);
         return n >= min && n <= max ? DataResult.success(b.build(n)) : DataResult.error(() -> "Slot index out of range: " + s);
      } catch (NumberFormatException e) {
         return DataResult.error(() -> "Invalid slot index: " + s);
      }
   }

   public class_1799 get(class_1657 player) {
      class_1661 inv = player.method_31548();
      return inv.method_5438(this.toContainerIndex());
   }

   public int toContainerIndex() {
      if (this.index == 98) {
         return -1;
      } else if (this.index == 99) {
         return 40;
      } else {
         return this.index >= 100 && this.index <= 103 ? 36 + (this.index - 100) : this.index;
      }
   }

   public boolean isEnderChest() {
      return this.index >= 200 && this.index <= 226;
   }

   public int enderChestIndex() {
      return this.index - 200;
   }

   private interface SlotBuilder {
      ItemSlot build(int var1);
   }
}
