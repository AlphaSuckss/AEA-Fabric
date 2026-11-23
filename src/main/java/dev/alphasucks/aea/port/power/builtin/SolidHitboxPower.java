package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class SolidHitboxPower extends PowerType<SolidHitboxPower.Config> {
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.SOLID_HITBOX);

   @Override
   public MapCodec<SolidHitboxPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("pushable", false).forGetter(SolidHitboxPower.Config::pushable),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(SolidHitboxPower.Config::bientityCondition)
            )
            .apply(i, SolidHitboxPower.Config::new)
      );
   }

   public static boolean isSolid(class_1297 holder) {
      return matching(holder, null) != null;
   }

   public static boolean isSolidFor(class_1297 holder, @Nullable class_1297 other) {
      return matching(holder, other) != null;
   }

   public static boolean isPushable(class_1297 holder) {
      SolidHitboxPower.Config cfg = matching(holder, null);
      return cfg == null || cfg.pushable();
   }

   @Nullable
   private static SolidHitboxPower.Config matching(class_1297 holder, @Nullable class_1297 other) {
      if (!IN_USE.inUse()) {
         return null;
      }

      PowerContainer container = PowerContainer.of(holder);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.SOLID_HITBOX);
         if (powers.isEmpty()) {
            return null;
         }

         EntityCtx selfCtx = null;
         BiEntityCtx pairCtx = null;
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof SolidHitboxPower.Config cfg) {
                  if (power.condition().isPresent()) {
                     if (selfCtx == null) {
                        selfCtx = EntityCtx.of(holder, holder.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(selfCtx)) {
                        continue;
                     }
                  }

                  if (cfg.bientityCondition().isEmpty() || other == null) {
                     return cfg;
                  }

                  if (pairCtx == null) {
                     pairCtx = new BiEntityCtx(holder, other, holder.method_37908());
                  }

                  if (((BiEntityCondition)cfg.bientityCondition().get()).test(pairCtx)) {
                     return cfg;
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public record Config(boolean pushable, Optional<BiEntityCondition> bientityCondition) {
   }
}
