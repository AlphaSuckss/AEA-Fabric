package dev.alphasucks.aea.port.entity;

import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.builtin.ScareMobsPower;
import dev.alphasucks.aea.port.power.builtin.ScareMobsPower.Config;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.class_11;
import net.minecraft.class_1309;
import net.minecraft.class_1314;
import net.minecraft.class_1352;
import net.minecraft.class_243;
import net.minecraft.class_5532;
import net.minecraft.class_1352.class_4134;
import org.jetbrains.annotations.Nullable;

public final class ScareMobsGoal extends class_1352 {
   private final class_1314 mob;
   @Nullable
   private class_1309 scary;
   private double speed;
   @Nullable
   private class_11 fleePath;

   public ScareMobsGoal(class_1314 mob) {
      this.mob = mob;
      this.method_6265(EnumSet.of(class_4134.field_18405));
   }

   @Override
   public boolean method_6264() {
      List<class_1309> holders = ScareMobsPower.holders(this.mob.method_37908());
      if (holders.isEmpty()) {
         return false;
      }

      this.scary = this.findScary(holders);
      if (this.scary == null) {
         return false;
      }

      class_243 away = class_5532.method_31511(this.mob, 16, 7, this.scary.method_19538());
      if (away == null) {
         return false;
      }

      if (this.scary.method_5649(away.field_1352, away.field_1351, away.field_1350) < this.scary.method_5858(this.mob)) {
         return false;
      }

      this.fleePath = this.mob.method_5942().method_6352(away.field_1352, away.field_1351, away.field_1350, 0);
      return this.fleePath != null;
   }

   @Override
   public boolean method_6266() {
      return this.scary != null && this.scary.method_5805() && !this.mob.method_5942().method_6357();
   }

   @Override
   public void method_6269() {
      this.mob.method_5942().method_6334(this.fleePath, this.speed);
   }

   @Override
   public void method_6270() {
      this.scary = null;
      this.fleePath = null;
   }

   @Nullable
   private class_1309 findScary(List<class_1309> holders) {
      class_1309 best = null;
      double bestDistance = Double.MAX_VALUE;

      for (int i = 0; i < holders.size(); i++) {
         class_1309 candidate = (class_1309)holders.get(i);
         if (candidate != this.mob && candidate.method_37908() == this.mob.method_37908()) {
            double distance = candidate.method_5858(this.mob);
            if (!(distance >= bestDistance)) {
               double appliedSpeed = this.scareSpeed(candidate, distance);
               if (!(appliedSpeed <= 0.0)) {
                  best = candidate;
                  bestDistance = distance;
                  this.speed = appliedSpeed;
               }
            }
         }
      }

      return best;
   }

   private double scareSpeed(class_1309 candidate, double distanceSqr) {
      double[] speed = new double[]{0.0};
      PowerLookup.forEach(
         candidate,
         ApoliIds.SCARE_MOBS,
         Config.class,
         cfg -> {
            if (!(speed[0] > 0.0)) {
               if (!(distanceSqr > cfg.radius() * cfg.radius())) {
                  if (!cfg.bientityCondition().isPresent()
                     || ((BiEntityCondition)cfg.bientityCondition().get()).test(new BiEntityCtx(candidate, this.mob, candidate.method_37908()))) {
                     speed[0] = cfg.speed();
                  }
               }
            }
         }
      );
      return speed[0];
   }
}
