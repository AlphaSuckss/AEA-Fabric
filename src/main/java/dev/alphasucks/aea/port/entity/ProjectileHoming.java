package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.builtin.FireProjectilePower.Homing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_6025;
import org.jetbrains.annotations.Nullable;

public final class ProjectileHoming {
   public static final int NONE = -1;
   public static final int FINISHED = -2;
   private static final int SEARCH_INTERVAL = 4;
   private static final double KEEP_RANGE_FACTOR = 1.5;
   private static final class_243 UP = new class_243(0.0, 1.0, 0.0);
   private static final class_243 EAST = new class_243(1.0, 0.0, 0.0);
   private final Optional<BiEntityCondition> condition;
   private final int delay;
   private final int duration;
   private final double range;
   private final double minCos;
   private final float turnRate;
   private int age;
   private int nextSearch;

   public ProjectileHoming(Homing config, class_1297 shooter) {
      this.condition = config.bientityCondition();
      this.delay = Math.max(0, config.delay().evalInt(shooter));
      this.duration = Math.max(0, config.duration().evalInt(shooter));
      this.range = Math.max(0.0, config.range().eval(shooter));
      this.minCos = Math.cos(Math.toRadians(class_3532.method_15350(config.angle().eval(shooter), 0.0, 180.0)));
      this.turnRate = (float)Math.toRadians(Math.max(0.0, config.turnRate().eval(shooter)));
   }

   public float turnRate() {
      return this.turnRate;
   }

   public int update(CustomProjectileEntity projectile, int currentId) {
      int tick = this.age++;
      if (tick < this.delay) {
         return -1;
      }

      if (this.duration > 0 && tick >= this.delay + this.duration) {
         return -2;
      }

      class_1297 owner = projectile.method_24921();
      if (currentId >= 0) {
         class_1297 current = projectile.method_37908().method_8469(currentId);
         if (current != null && this.keeps(projectile, owner, current)) {
            return currentId;
         }
      }

      if (tick < this.nextSearch) {
         return -1;
      }

      this.nextSearch = tick + 4;
      class_1297 found = this.find(projectile, owner);
      return found == null ? -1 : found.method_5628();
   }

   private boolean keeps(CustomProjectileEntity projectile, @Nullable class_1297 owner, class_1297 target) {
      double keep = this.range * 1.5;
      return target.method_37908() == projectile.method_37908() && target.method_5858(projectile) <= keep * keep && this.accepts(projectile, owner, target);
   }

   @Nullable
   private class_1297 find(CustomProjectileEntity projectile, @Nullable class_1297 owner) {
      if (this.range <= 0.0) {
         return null;
      }

      class_243 origin = projectile.method_5829().method_1005();
      class_243 velocity = projectile.method_18798();
      double speed = velocity.method_1033();
      boolean cone = this.minCos > -1.0 && speed > 1.0E-6;
      List<class_1297> candidates = projectile.method_37908()
         .method_8333(projectile, new class_238(origin, origin).method_1014(this.range), entity -> entity instanceof class_1309);
      class_1297 best = null;
      double bestDistance = this.range * this.range;
      int i = 0;

      for (int n = candidates.size(); i < n; i++) {
         class_1297 candidate = (class_1297)candidates.get(i);
         class_243 offset = candidate.method_5829().method_1005().method_1020(origin);
         double distance = offset.method_1027();
         if (!(distance > bestDistance)
            && (!cone || !(distance > 1.0E-6) || !(offset.method_1026(velocity) < this.minCos * Math.sqrt(distance) * speed))
            && this.accepts(projectile, owner, candidate)) {
            best = candidate;
            bestDistance = distance;
         }
      }

      return best;
   }

   private boolean accepts(CustomProjectileEntity projectile, @Nullable class_1297 owner, class_1297 candidate) {
      if (candidate instanceof class_1309 living && living.method_5805() && !candidate.method_7325()) {
         if (owner != null) {
            if (candidate == owner || owner.method_5722(candidate)) {
               return false;
            }

            if (candidate instanceof class_6025 ownable) {
               UUID ownerId = ownable.method_6139();
               if (ownerId != null && ownerId.equals(owner.method_5667())) {
                  return false;
               }
            }
         }

         return !projectile.canTarget(candidate)
            ? false
            : this.condition.isEmpty()
               || ((BiEntityCondition)this.condition.get())
                  .test(BiEntityCtx.of((class_1297)(owner != null ? owner : projectile), candidate, projectile.method_37908()));
      } else {
         return false;
      }
   }

   public static void steer(class_1297 projectile, class_1297 target, float turnRate) {
      if (!(turnRate <= 0.0F)) {
         class_243 velocity = projectile.method_18798();
         double speed = velocity.method_1033();
         if (!(speed < 1.0E-6)) {
            class_243 toTarget = target.method_5829().method_1005().method_1020(projectile.method_5829().method_1005());
            double distance = toTarget.method_1033();
            if (!(distance < 1.0E-6)) {
               class_243 current = velocity.method_1021(1.0 / speed);
               class_243 desired = toTarget.method_1021(1.0 / distance);
               double angle = Math.acos(class_3532.method_15350(current.method_1026(desired), -1.0, 1.0));
               class_243 direction;
               if (angle <= turnRate) {
                  direction = desired;
               } else {
                  class_243 axis = current.method_1036(desired);
                  if (axis.method_1027() < 1.0E-12) {
                     axis = current.method_1036(Math.abs(current.field_1351) < 0.99 ? UP : EAST);
                  }

                  axis = axis.method_1029();
                  direction = current.method_1021(Math.cos(turnRate)).method_1019(axis.method_1036(current).method_1021(Math.sin(turnRate)));
               }

               projectile.method_18799(direction.method_1021(speed));
            }
         }
      }
   }
}
