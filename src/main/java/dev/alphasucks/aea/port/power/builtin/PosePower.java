package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.access.ModifiedPoseHolder;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.ArmPoseReference;
import dev.alphasucks.aea.port.data.EntityPoses;
import dev.alphasucks.aea.port.data.Hand;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_4050;
import org.jetbrains.annotations.Nullable;

public final class PosePower extends PowerType<PosePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("pose");
   private static final PowerTypeGate IN_USE = new PowerTypeGate(CANONICAL);

   @Override
   public MapCodec<PosePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               EntityPoses.CODEC.optionalFieldOf("entity_pose").forGetter(PosePower.Config::entityPose),
               ArmPoseReference.CODEC.optionalFieldOf("arm_pose").forGetter(PosePower.Config::armPose),
               Hand.LIST_CODEC.optionalFieldOf("hands", Hand.BOTH).forGetter(PosePower.Config::hands),
               Codec.INT.optionalFieldOf("priority", 0).forGetter(PosePower.Config::priority)
            )
            .apply(i, PosePower.Config::new)
      );
   }

   @Nullable
   public static PosePower.Config chooseBest(class_1297 entity) {
      if (!IN_USE.inUse()) {
         return null;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         PosePower.Config best = null;
         EntityCtx ctx = null;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof PosePower.Config cfg && cfg.hasPose() && (best == null || cfg.priority() > best.priority())) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  best = cfg;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   public static boolean hasEntityPose(class_1297 entity, class_4050 pose) {
      return entity instanceof ModifiedPoseHolder holder && pose.equals(holder.apoli$getModifiedEntityPose());
   }

   public record Config(Optional<class_4050> entityPose, Optional<ArmPoseReference> armPose, List<Hand> hands, int priority) {
      public boolean hasPose() {
         return this.entityPose.isPresent() || this.armPose.isPresent();
      }
   }
}
