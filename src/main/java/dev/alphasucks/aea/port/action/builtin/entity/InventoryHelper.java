package dev.alphasucks.aea.port.action.builtin.entity;

import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.ItemSlot;
import dev.alphasucks.aea.port.data.ProcessMode;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.class_1263;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;

public final class InventoryHelper {
   private InventoryHelper() {
   }

   public static int walk(
      class_1263 container,
      class_1309 entity,
      Optional<ItemCondition> itemCondition,
      Optional<ItemSlot> slot,
      Optional<List<ItemSlot>> slots,
      ProcessMode mode,
      int limit,
      BiConsumer<Integer, class_1799> visitor
   ) {
      return walk(container, entity, itemCondition, slot, slots, mode, limit, false, visitor);
   }

   public static int walk(
      class_1263 container,
      class_1309 entity,
      Optional<ItemCondition> itemCondition,
      Optional<ItemSlot> slot,
      Optional<List<ItemSlot>> slots,
      ProcessMode mode,
      int limit,
      boolean includeEmpty,
      BiConsumer<Integer, class_1799> visitor
   ) {
      int visited = 0;
      int max = container.method_5439();

      for (int idx = 0; idx < max && (limit <= 0 || visited < limit); idx++) {
         if (slotMatches(idx, entity, slot, slots)) {
            class_1799 stack = container.method_5438(idx);
            if ((!stack.method_7960() || includeEmpty)
               && (!itemCondition.isPresent() || ((ItemCondition)itemCondition.get()).test(new ItemCtx(stack, entity.method_37908(), entity)))) {
               if (mode == ProcessMode.ITEMS) {
                  int count = stack.method_7947();

                  for (int j = 0; j < count; j++) {
                     visitor.accept(idx, stack);
                     if (limit > 0 && ++visited >= limit) {
                        break;
                     }
                  }
               } else {
                  visitor.accept(idx, stack);
                  visited++;
               }
            }
         }
      }

      return visited;
   }

   private static boolean slotMatches(int containerIndex, class_1309 entity, Optional<ItemSlot> slot, Optional<List<ItemSlot>> slots) {
      if (slot.isEmpty() && slots.isEmpty()) {
         return true;
      }

      if (slot.isPresent() && resolveIndex((ItemSlot)slot.get(), entity) == containerIndex) {
         return true;
      }

      if (slots.isPresent()) {
         for (ItemSlot s : (List<ItemSlot>)slots.get()) {
            if (resolveIndex(s, entity) == containerIndex) {
               return true;
            }
         }
      }

      return false;
   }

   private static int resolveIndex(ItemSlot slot, class_1309 entity) {
      return slot.index() == 98 && entity instanceof class_1657 player ? player.method_31548().field_7545 : slot.toContainerIndex();
   }
}
