package io.github.apace100.originsclasses.util;

import com.google.common.collect.Sets;
import io.github.apace100.originsclasses.mixin.CombinedEntryAccessor;
import io.github.apace100.originsclasses.mixin.ItemEntryAccessor;
import io.github.apace100.originsclasses.mixin.LootPoolAccessor;
import io.github.apace100.originsclasses.mixin.LootTableAccessor;
import io.github.apace100.originsclasses.mixin.TagEntryAccessor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_2960;
import net.minecraft.class_55;
import net.minecraft.class_5819;
import net.minecraft.class_60;
import net.minecraft.class_69;
import net.minecraft.class_77;
import net.minecraft.class_79;
import net.minecraft.class_7923;
import net.minecraft.class_8490;
import net.minecraft.class_91;
import net.minecraft.server.MinecraftServer;

public class ItemUtil {
   private static final Set<class_1792> OBTAINABLE = new HashSet();
   private static class_1792[] OBTAINABLE_ARRAY = new class_1792[0];
   private static boolean isObtainableSetBuilt = false;

   public static class_1799 createMerchantItemStack(class_1792 item, class_5819 random) {
      class_1799 stack = new class_1799(item);
      if (item.method_7870(stack) && random.method_43057() < 0.5) {
         class_1890.method_8233(random, stack, 1 + random.method_43048(30), random.method_43056());
      }

      return stack;
   }

   public static boolean isObtainable(MinecraftServer server, class_1792 item) {
      buildObtainableSet(server);
      return OBTAINABLE.contains(item);
   }

   @org.jetbrains.annotations.Nullable
   public static class_1792 getRandomObtainableItem(MinecraftServer server, class_5819 random, Set<class_1792> exclude) {
      buildObtainableSet(server);
      if (exclude != null && !exclude.isEmpty()) {
         Set<class_1792> possibles = Sets.difference(OBTAINABLE, exclude);
         class_1792[] items = new class_1792[possibles.size()];
         items = (class_1792[])possibles.toArray(items);
         return items.length == 0 ? null : items[random.method_43048(items.length)];
      } else {
         return OBTAINABLE_ARRAY.length == 0 ? null : OBTAINABLE_ARRAY[random.method_43048(OBTAINABLE_ARRAY.length)];
      }
   }

   /** AEA: the set used to be built once per JVM; rebuild after /reload and for each new world. */
   public static synchronized void invalidate() {
      OBTAINABLE.clear();
      OBTAINABLE_ARRAY = new class_1792[0];
      isObtainableSetBuilt = false;
   }

   public static synchronized void buildObtainableSet(MinecraftServer server) {
      if (!isObtainableSetBuilt) {
         class_60 manager = server.method_3857();

         for (class_2960 id : manager.method_51193(class_8490.field_44498)) {
            LootTableAccessor table = (LootTableAccessor)manager.getElement(class_8490.field_44498, id);
            if (table == null || table.getPools() == null) {
               continue;
            }
            class_55[] pools = table.getPools();
            Queue<class_79> entryQueue = new LinkedList();

            for (class_55 pool : pools) {
               class_79[] entries = ((LootPoolAccessor)pool).getEntries();
               entryQueue.addAll(Arrays.asList(entries));
            }

            while (!entryQueue.isEmpty()) {
               class_79 entry = (class_79)entryQueue.remove();
               if (entry instanceof class_77) {
                  class_1792 item = ((ItemEntryAccessor)entry).getItem();
                  if (item != null && item != net.minecraft.class_1802.field_8162) {
                     OBTAINABLE.add(item);
                  }
               } else if (entry instanceof class_91) {
                  OBTAINABLE.addAll(TagUtil.getAllEntries(class_7923.field_41178, ((TagEntryAccessor)entry).getName()));
               } else if (entry instanceof class_69) {
                  entryQueue.addAll(Arrays.asList(((CombinedEntryAccessor)entry).getChildren()));
               }
            }
         }

         OBTAINABLE_ARRAY = new class_1792[OBTAINABLE.size()];
         OBTAINABLE_ARRAY = (class_1792[])OBTAINABLE.toArray(OBTAINABLE_ARRAY);
         isObtainableSetBuilt = true;
      }
   }
}
