package dev.alphasucks.aea.port.sound;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_3419;
import net.minecraft.class_5321;
import org.jetbrains.annotations.Nullable;

public final class SoundTracker {
   public static final int UNKNOWN_RANGE = -1;
   private static final int CAPACITY = 64;
   private static final Set<class_2960> WATCHED = ConcurrentHashMap.newKeySet();
   private static volatile boolean watching;
   private static final SoundTracker.Log SERVER = new SoundTracker.Log();
   private static final SoundTracker.Log CLIENT = new SoundTracker.Log();

   private SoundTracker() {
   }

   public static boolean watching() {
      return watching;
   }

   public static void watch(class_2960 sound) {
      if (WATCHED.add(sound)) {
         watching = true;
      }
   }

   public static boolean watched(class_2960 sound) {
      return WATCHED.contains(sound);
   }

   public static void record(class_1937 level, class_2960 sound, class_3419 source, double x, double y, double z, float range) {
      if (watching && WATCHED.contains(sound)) {
         SoundTracker.Log log = level.method_8608() ? CLIENT : SERVER;
         int slot = log.next;
         log.sounds[slot] = sound;
         log.sources[slot] = source;
         log.dimensions[slot] = level.method_27983();
         log.x[slot] = x;
         log.y[slot] = y;
         log.z[slot] = z;
         log.ranges[slot] = range;
         log.times[slot] = level.method_8510();
         log.next = slot + 1 == 64 ? 0 : slot + 1;
      }
   }

   public static boolean playing(class_1297 listener, class_2960 sound, @Nullable class_3419 source, int duration, double range) {
      if (!watching) {
         return false;
      }

      class_1937 level = listener.method_37908();
      SoundTracker.Log log = level.method_8608() ? CLIENT : SERVER;
      class_5321<class_1937> dimension = level.method_27983();
      long now = level.method_8510();
      double ex = listener.method_23317();
      double ey = listener.method_23320();
      double ez = listener.method_23321();
      boolean explicitRange = range >= 0.0;
      double explicitSquared = range * range;

      for (int i = 0; i < 64; i++) {
         class_2960 candidate = log.sounds[i];
         if (candidate != null && candidate.equals(sound) && log.dimensions[i] == dimension) {
            long elapsed = now - log.times[i];
            if (elapsed >= 0L && elapsed < duration && (source == null || log.sources[i] == source)) {
               double limitSquared;
               if (explicitRange) {
                  limitSquared = explicitSquared;
               } else {
                  if (log.ranges[i] < 0.0F) {
                     return true;
                  }

                  limitSquared = (double)log.ranges[i] * log.ranges[i];
               }

               double dx = log.x[i] - ex;
               double dy = log.y[i] - ey;
               double dz = log.z[i] - ez;
               if (dx * dx + dy * dy + dz * dz <= limitSquared) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static final class Log {
      private final class_2960[] sounds = new class_2960[64];
      private final class_3419[] sources = new class_3419[64];
      private final class_5321<class_1937>[] dimensions = new class_5321[64];
      private final double[] x = new double[64];
      private final double[] y = new double[64];
      private final double[] z = new double[64];
      private final float[] ranges = new float[64];
      private final long[] times = new long[64];
      private int next;
   }
}
