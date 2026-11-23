package dev.alphasucks.aea.port.power.builtin;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.class_52;

public final class LootTableReplacement {
   private static final ThreadLocal<LootTableReplacement.State> STATE = ThreadLocal.withInitial(LootTableReplacement.State::new);
   private static final AtomicInteger ACTIVE = new AtomicInteger();

   private LootTableReplacement() {
   }

   public static boolean isActive() {
      return ACTIVE.get() > 0;
   }

   public static boolean isStacked(class_52 table) {
      if (!isActive()) {
         return false;
      }

      List<class_52> marked = ((LootTableReplacement.State)STATE.get()).marked;

      for (int i = 0; i < marked.size(); i++) {
         if (marked.get(i) == table) {
            return true;
         }
      }

      return false;
   }

   public static void begin(class_52 original, List<class_52> replacements) {
      LootTableReplacement.State state = (LootTableReplacement.State)STATE.get();
      state.frames.add(state.stack.size());
      state.frames.add(state.marked.size());
      state.frames.add(state.backtrack.size());
      state.stack.add(original);
      state.marked.add(original);

      for (int i = 0; i < replacements.size(); i++) {
         state.stack.add((class_52)replacements.get(i));
         state.marked.add((class_52)replacements.get(i));
      }

      ACTIVE.incrementAndGet();
   }

   public static void end() {
      LootTableReplacement.State state = (LootTableReplacement.State)STATE.get();
      int last = state.frames.size();
      truncate(state.backtrack, state.frames.removeInt(last - 1));
      truncate(state.marked, state.frames.removeInt(last - 2));
      truncate(state.stack, state.frames.removeInt(last - 3));
      ACTIVE.decrementAndGet();
   }

   public static class_52 peek() {
      if (!isActive()) {
         return class_52.field_948;
      }

      List<class_52> stack = ((LootTableReplacement.State)STATE.get()).stack;
      return stack.isEmpty() ? class_52.field_948 : (class_52)stack.get(stack.size() - 1);
   }

   public static void enter() {
      if (isActive()) {
         LootTableReplacement.State state = (LootTableReplacement.State)STATE.get();
         state.backtrack.add(state.stack.isEmpty() ? null : (class_52)state.stack.remove(state.stack.size() - 1));
      }
   }

   public static void exit() {
      if (isActive()) {
         LootTableReplacement.State state = (LootTableReplacement.State)STATE.get();
         if (!state.backtrack.isEmpty()) {
            class_52 restored = (class_52)state.backtrack.remove(state.backtrack.size() - 1);
            if (restored != null) {
               state.stack.add(restored);
            }
         }
      }
   }

   private static void truncate(List<class_52> list, int size) {
      while (list.size() > size) {
         list.remove(list.size() - 1);
      }
   }

   private static final class State {
      private final List<class_52> stack = new ArrayList();
      private final List<class_52> marked = new ArrayList();
      private final List<class_52> backtrack = new ArrayList();
      private final IntArrayList frames = new IntArrayList();
   }
}
