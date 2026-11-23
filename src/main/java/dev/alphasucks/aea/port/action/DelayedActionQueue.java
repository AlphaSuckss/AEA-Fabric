package dev.alphasucks.aea.port.action;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.attribution.PowerCause;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1297;
import net.minecraft.class_2960;

public final class DelayedActionQueue {
   public static final BooleanSupplier ALWAYS = () -> true;
   private static long currentTick;
   private static boolean loggedActionFailure;
   private static final Map<Long, List<DelayedActionQueue.Entry>> BUCKETS = new HashMap();

   private DelayedActionQueue() {
   }

   public static synchronized void schedule(int ticks, BooleanSupplier alive, Runnable action) {
      long fireTick = currentTick + Math.max(1, ticks);
      ((List)BUCKETS.computeIfAbsent(fireTick, k -> new ArrayList()))
         .add(new DelayedActionQueue.Entry(alive, action, PowerCause.holder(), PowerCause.powerId()));
   }

   public static synchronized void tick() {
      List<DelayedActionQueue.Entry> due = (List<DelayedActionQueue.Entry>)BUCKETS.remove(++currentTick);
      if (due != null) {
         for (int i = 0; i < due.size(); i++) {
            DelayedActionQueue.Entry entry = (DelayedActionQueue.Entry)due.get(i);

            try {
               if (entry.alive().getAsBoolean()) {
                  boolean attributed = PowerCause.push(entry.causeHolder(), entry.causePower());

                  try {
                     entry.action().run();
                  } finally {
                     if (attributed) {
                        PowerCause.pop();
                     }
                  }
               }
            } catch (Throwable t) {
               if (!loggedActionFailure) {
                  loggedActionFailure = true;
                  Apoli.LOGGER.error("[Apoli] A delayed action (apoli:delay / apoli:loop) threw; that chain was dropped. Later failures are not logged.", t);
               }
            }
         }
      }
   }

   public static synchronized void clear() {
      BUCKETS.clear();
      currentTick = 0L;
      loggedActionFailure = false;
   }

   private record Entry(BooleanSupplier alive, Runnable action, class_1297 causeHolder, class_2960 causePower) {
   }
}
