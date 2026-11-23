package dev.alphasucks.aea.port.dev;

import dev.alphasucks.aea.port.data.Shape;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2390;
import net.minecraft.class_243;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class DevParticles {
   private static final class_2390 BLUE = new class_2390(new Vector3f(0.25F, 0.55F, 1.0F), 0.75F);
   private static final class_2390 GREEN = new class_2390(new Vector3f(0.35F, 0.95F, 0.4F), 0.75F);
   private static final int RING_STEPS = 48;
   private static final int SLANTS = 8;
   private static final int MAX_POINTS = 600;
   private static final int RAY_POINTS = 1200;
   private static final int TICK_BUDGET = 4000;
   private static final int REFRESH_PERIOD = 10;
   private static final double SHAPE_DENSITY = 2.0;
   private static final double RAY_DENSITY = 4.0;
   private static final double RAY_STEP = 0.25;
   private static final double REACH_STEP = 0.6;
   private static final double LEAD = 0.5;
   private static final double MARK_RADIUS = 0.2;
   private static final double BLOCK_HALF = 0.51;
   private static final int MAX_TRACKED = 4096;
   private static long budgetTick = Long.MIN_VALUE;
   private static int tickBudget;
   private static final Map<DevParticles.Drawn, Long> LAST_DRAWN = new HashMap();

   private DevParticles() {
   }

   public static boolean due(class_1937 level) {
      return DevMode.any() && level.method_8510() % 10L == 0L;
   }

   public static boolean due(class_1937 level, class_1297 subject, Object config) {
      return DevMode.any() && dueFor(level, subject.method_5628(), -1, config);
   }

   public static boolean due(class_1937 level, class_1297 actor, class_1297 target, Object config) {
      return DevMode.any() && dueFor(level, actor.method_5628(), target.method_5628(), config);
   }

   static void forget() {
      LAST_DRAWN.clear();
   }

   private static boolean dueFor(class_1937 level, int first, int second, Object config) {
      if (!(level instanceof class_3218)) {
         return false;
      }

      long now = level.method_8510();
      DevParticles.Drawn key = new DevParticles.Drawn(first, second, config);
      Long last = (Long)LAST_DRAWN.get(key);
      if (last != null && now >= last && now - last < 10L) {
         return false;
      }

      if (LAST_DRAWN.size() >= 4096) {
         LAST_DRAWN.clear();
      }

      LAST_DRAWN.put(key, now);
      return true;
   }

   public static void outlineShape(class_3218 level, class_243 origin, Shape shape, double rx, double ry, double rz) {
      emitShape(level, origin, shape, rx, ry, rz, BLUE);
   }

   public static void outlineCondition(class_3218 level, class_243 origin, Shape shape, double rx, double ry, double rz) {
      emitShape(level, origin, shape, rx, ry, rz, GREEN);
   }

   public static void outlineBox(class_3218 level, class_238 box) {
      emitShape(level, box.method_1005(), Shape.CUBE, box.method_17939() * 0.5, box.method_17940() * 0.5, box.method_17941() * 0.5, BLUE);
   }

   private static void emitShape(class_3218 level, class_243 origin, Shape shape, double rx, double ry, double rz, class_2390 color) {
      DevParticles.Emitter out = emitter(level, color, 600);
      if (out != null) {
         switch (shape.solid()) {
            case CUBE:
               box(out, origin, rx, ry, rz, 2.0);
               break;
            case SPHERE:
               ellipsoid(out, origin, rx, ry, rz);
               break;
            case STAR:
               star(out, origin, rx, ry, rz);
               break;
            case CONE:
               cone(out, origin, rx, ry, rz);
         }
      }
   }

   public static void ray(class_3218 level, DevParticles.Ray kind, class_243 origin, class_243 dir, double stop, double reach) {
      List<class_3222> watchers = DevMode.watchers(level);
      if (!watchers.isEmpty()) {
         double end = Math.max(stop, reach);
         trace(level, watchers, kind, origin, dir, stop, end);
         ring(new DevParticles.Emitter(level, watchers, kind.faint, 1200), origin.method_1019(dir.method_1021(end)), dir, 0.2);
      }
   }

   public static void segment(class_3218 level, DevParticles.Ray kind, class_243 from, class_243 to, double stop) {
      List<class_3222> watchers = DevMode.watchers(level);
      if (!watchers.isEmpty()) {
         class_243 along = to.method_1020(from);
         double length = along.method_1033();
         if (!(length < 1.0E-4)) {
            trace(level, watchers, kind, from, along.method_1021(1.0 / length), Math.min(stop, length), length);
         }
      }
   }

   public static void detection(
      class_3218 level, DevParticles.Ray kind, class_243 origin, class_243 dir, double length, double rx, double ry, double rz, double coneAngle
   ) {
      if (!(length <= 0.0)) {
         DevParticles.Emitter out = emitter(level, kind.faint, 1200);
         if (out != null) {
            if (coneAngle >= 0.0) {
               sector(out, origin, dir, length, coneAngle);
            } else if (Math.max(rx, Math.max(ry, rz)) > 0.0) {
               beam(out, origin, dir, length, rx, ry, rz);
            } else {
               ring(out, origin.method_1019(dir.method_1021(length)), dir, 0.2);
            }
         }
      }
   }

   public static void mark(class_3218 level, DevParticles.Ray kind, class_2338 pos, boolean passed) {
      DevParticles.Emitter out = emitter(level, passed ? kind.solid : kind.faint, 1200);
      if (out != null) {
         box(out, class_243.method_24953(pos), 0.51, 0.51, 0.51, 4.0);
      }
   }

   public static void mark(class_3218 level, DevParticles.Ray kind, class_1297 entity, boolean passed) {
      DevParticles.Emitter out = emitter(level, passed ? kind.solid : kind.faint, 1200);
      if (out != null) {
         class_238 bounds = entity.method_5829().method_1014(0.02);
         box(out, bounds.method_1005(), bounds.method_17939() * 0.5, bounds.method_17940() * 0.5, bounds.method_17941() * 0.5, 4.0);
      }
   }

   private static void trace(class_3218 level, List<class_3222> watchers, DevParticles.Ray kind, class_243 origin, class_243 dir, double stop, double end) {
      double cut = Math.max(0.0, stop);
      line(new DevParticles.Emitter(level, watchers, kind.solid, 1200), origin, dir, Math.min(0.5, cut), cut, 0.25);
      if (!(end - cut < 0.3)) {
         line(new DevParticles.Emitter(level, watchers, kind.faint, 1200), origin, dir, cut + 0.6, end, 0.6);
      }
   }

   private static void line(DevParticles.Emitter out, class_243 origin, class_243 dir, double from, double to, double step) {
      double t = from;

      while (t < to) {
         out.at(origin.method_1019(dir.method_1021(t)));
         t += step;
      }

      out.at(origin.method_1019(dir.method_1021(to)));
   }

   private static void beam(DevParticles.Emitter out, class_243 origin, class_243 dir, double length, double rx, double ry, double rz) {
      class_243 start = origin.method_1019(dir.method_1021(Math.min(0.5, length)));
      class_243 end = origin.method_1019(dir.method_1021(length));
      box(out, start, rx, ry, rz, 4.0);
      box(out, end, rx, ry, rz, 4.0);

      for (int ex = -1; ex <= 1; ex += 2) {
         for (int ey = -1; ey <= 1; ey += 2) {
            for (int ez = -1; ez <= 1; ez += 2) {
               if (!overlapsTwin(dir, ex, ey, ez)) {
                  edge(out, start.method_1031(ex * rx, ey * ry, ez * rz), end.method_1031(ex * rx, ey * ry, ez * rz), 4.0);
               }
            }
         }
      }
   }

   private static boolean overlapsTwin(class_243 dir, int ex, int ey, int ez) {
      return Math.abs(dir.field_1352) > 0.999 && ex * dir.field_1352 > 0.0
         || Math.abs(dir.field_1351) > 0.999 && ey * dir.field_1351 > 0.0
         || Math.abs(dir.field_1350) > 0.999 && ez * dir.field_1350 > 0.0;
   }

   private static void sector(DevParticles.Emitter out, class_243 origin, class_243 dir, double range, double halfAngle) {
      double theta = Math.toRadians(Math.max(0.0, Math.min(180.0, halfAngle)));
      class_243 side = perpendicular(dir);
      class_243 other = dir.method_1036(side);
      class_243 rimCentre = origin.method_1019(dir.method_1021(Math.cos(theta) * range));
      double rimRadius = Math.sin(theta) * range;
      int rimSteps = 8 * Math.max(3, Math.min(16, (int)Math.ceil((Math.PI * 2) * rimRadius * 4.0 / 8.0)));
      int arcSteps = Math.max(6, Math.min(48, (int)Math.ceil(theta * range * 4.0)));

      for (int i = 0; i < rimSteps; i++) {
         double a = (Math.PI * 2) * i / rimSteps;
         class_243 spoke = side.method_1021(Math.cos(a)).method_1019(other.method_1021(Math.sin(a)));
         class_243 rim = rimCentre.method_1019(spoke.method_1021(rimRadius));
         out.at(rim);
         if (i % (rimSteps / 8) == 0) {
            class_243 slant = rim.method_1020(origin);
            double slantLength = slant.method_1033();
            if (slantLength > 0.5) {
               edge(out, origin.method_1019(slant.method_1021(0.5 / slantLength)), rim, 4.0);
            }

            for (int s = 1; s < arcSteps; s++) {
               double alpha = theta * (1.0 - (double)s / arcSteps);
               out.at(origin.method_1019(dir.method_1021(Math.cos(alpha) * range)).method_1019(spoke.method_1021(Math.sin(alpha) * range)));
            }
         }
      }

      out.at(origin.method_1019(dir.method_1021(range)));
   }

   private static void ring(DevParticles.Emitter out, class_243 centre, class_243 normal, double radius) {
      class_243 side = perpendicular(normal);
      class_243 other = normal.method_1036(side);
      int steps = Math.max(8, Math.min(48, (int)(radius * 24.0)));

      for (int i = 0; i < steps; i++) {
         double a = (Math.PI * 2) * i / steps;
         out.at(centre.method_1019(side.method_1021(Math.cos(a) * radius)).method_1019(other.method_1021(Math.sin(a) * radius)));
      }
   }

   private static class_243 perpendicular(class_243 dir) {
      class_243 up = Math.abs(dir.field_1351) > 0.99 ? new class_243(1.0, 0.0, 0.0) : new class_243(0.0, 1.0, 0.0);
      return dir.method_1036(up).method_1029();
   }

   private static void box(DevParticles.Emitter out, class_243 c, double rx, double ry, double rz, double density) {
      for (int ex = -1; ex <= 1; ex += 2) {
         for (int ez = -1; ez <= 1; ez += 2) {
            edge(out, c.method_1031(ex * rx, -ry, ez * rz), c.method_1031(ex * rx, ry, ez * rz), density);
         }
      }

      for (int ey = -1; ey <= 1; ey += 2) {
         for (int ez = -1; ez <= 1; ez += 2) {
            edge(out, c.method_1031(-rx, ey * ry, ez * rz), c.method_1031(rx, ey * ry, ez * rz), density);
         }

         for (int ex = -1; ex <= 1; ex += 2) {
            edge(out, c.method_1031(ex * rx, ey * ry, -rz), c.method_1031(ex * rx, ey * ry, rz), density);
         }
      }
   }

   private static void ellipsoid(DevParticles.Emitter out, class_243 c, double rx, double ry, double rz) {
      for (int i = 0; i < 48; i++) {
         double a = (Math.PI * 2) * i / 48.0;
         double cos = Math.cos(a);
         double sin = Math.sin(a);
         out.at(c.method_1031(cos * rx, sin * ry, 0.0));
         out.at(c.method_1031(cos * rx, 0.0, sin * rz));
         out.at(c.method_1031(0.0, cos * ry, sin * rz));
      }
   }

   private static void star(DevParticles.Emitter out, class_243 c, double rx, double ry, double rz) {
      class_243[] axes = new class_243[]{
         c.method_1031(rx, 0.0, 0.0),
         c.method_1031(-rx, 0.0, 0.0),
         c.method_1031(0.0, ry, 0.0),
         c.method_1031(0.0, -ry, 0.0),
         c.method_1031(0.0, 0.0, rz),
         c.method_1031(0.0, 0.0, -rz)
      };

      for (int x = 0; x < 2; x++) {
         for (int y = 2; y < 4; y++) {
            for (int z = 4; z < 6; z++) {
               edge(out, axes[x], axes[y], 2.0);
               edge(out, axes[y], axes[z], 2.0);
               edge(out, axes[z], axes[x], 2.0);
            }
         }
      }
   }

   private static void cone(DevParticles.Emitter out, class_243 c, double rx, double ry, double rz) {
      class_243 top = c.method_1031(0.0, ry, 0.0);

      for (int i = 0; i < 48; i++) {
         double a = (Math.PI * 2) * i / 48.0;
         class_243 rim = top.method_1031(Math.cos(a) * rx, 0.0, Math.sin(a) * rz);
         out.at(rim);
         if (i % 6 == 0) {
            edge(out, c, rim, 2.0);
         }
      }
   }

   private static void edge(DevParticles.Emitter out, class_243 from, class_243 to, double density) {
      double length = from.method_1022(to);
      int steps = (int)Math.max(2.0, Math.min(20.0 * density, length * density));

      for (int i = 0; i <= steps; i++) {
         out.at(from.method_1019(to.method_1020(from).method_1021((double)i / steps)));
      }
   }

   @Nullable
   private static DevParticles.Emitter emitter(class_3218 level, class_2390 options, int budget) {
      List<class_3222> watchers = DevMode.watchers(level);
      return watchers.isEmpty() ? null : new DevParticles.Emitter(level, watchers, options, budget);
   }

   private static boolean spend(class_3218 level) {
      long now = level.method_8510();
      if (now != budgetTick) {
         budgetTick = now;
         tickBudget = 4000;
      }

      return tickBudget-- > 0;
   }

   private record Drawn(int first, int second, Object config) {
      public boolean equals(Object other) {
         return other instanceof DevParticles.Drawn drawn && drawn.first == this.first && drawn.second == this.second && drawn.config == this.config;
      }

      public int hashCode() {
         return 31 * (31 * this.first + this.second) + System.identityHashCode(this.config);
      }
   }

   private static final class Emitter {
      private final class_3218 level;
      private final List<class_3222> watchers;
      private final class_2390 options;
      private int budget;

      Emitter(class_3218 level, List<class_3222> watchers, class_2390 options, int budget) {
         this.level = level;
         this.watchers = watchers;
         this.options = options;
         this.budget = budget;
      }

      void at(class_243 pos) {
         if (this.budget-- > 0) {
            if (DevParticles.spend(this.level)) {
               for (int i = 0; i < this.watchers.size(); i++) {
                  this.level
                     .method_14166((class_3222)this.watchers.get(i), this.options, true, pos.field_1352, pos.field_1351, pos.field_1350, 1, 0.0, 0.0, 0.0, 0.0);
               }
            }
         }
      }
   }

   public enum Ray {
      ACTION(new Vector3f(1.0F, 0.2F, 0.2F)),
      CONDITION(new Vector3f(1.0F, 0.85F, 0.15F));

      private final class_2390 solid;
      private final class_2390 faint;

      Ray(Vector3f colour) {
         this.solid = new class_2390(colour, 1.0F);
         this.faint = new class_2390(colour, 0.65F);
      }
   }
}
