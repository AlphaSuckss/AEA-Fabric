package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class EmissivePower extends PowerType<EmissivePower.Config> {
   @Override
   public MapCodec<EmissivePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.intRange(0, 15).optionalFieldOf("luminance", 15).forGetter(EmissivePower.Config::luminance),
               Codec.BOOL.optionalFieldOf("water_sensitive", false).forGetter(EmissivePower.Config::waterSensitive),
               Codec.BOOL.optionalFieldOf("self_lit", true).forGetter(EmissivePower.Config::selfLit)
            )
            .apply(i, EmissivePower.Config::new)
      );
   }

   public static int luminanceOf(@Nullable class_1297 entity) {
      return scan(entity, false);
   }

   public static int selfLitLuminanceOf(@Nullable class_1297 entity) {
      return scan(entity, true);
   }

   public static boolean isWaterSensitive(@Nullable class_1297 entity) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.EMISSIVE);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         int i = 0;

         while (true) {
            if (i >= powers.size()) {
               return false;
            }

            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof EmissivePower.Config cfg && cfg.waterSensitive) {
                  if (!power.condition().isPresent()) {
                     break;
                  }

                  if (ctx == null) {
                     ctx = EntityCtx.of(entity, entity.method_37908());
                  }

                  if (((EntityCondition)power.condition().get()).test(ctx)) {
                     break;
                  }
               }
            }

            i++;
         }

         return true;
      } else {
         return false;
      }
   }

   private static int scan(@Nullable class_1297 entity, boolean selfLitOnly) {
      if (entity == null) {
         return 0;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.EMISSIVE);
         if (powers.isEmpty()) {
            return 0;
         }

         int best = 0;
         EntityCtx ctx = null;

         for (int i = 0; i < powers.size(); i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null && power.config() instanceof EmissivePower.Config cfg && cfg.luminance > best && (!selfLitOnly || cfg.selfLit)) {
                  if (power.condition().isPresent()) {
                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (!((EntityCondition)power.condition().get()).test(ctx)) {
                        continue;
                     }
                  }

                  best = cfg.luminance;
                  if (best >= 15) {
                     break;
                  }
               }
            }
         }

         return best;
      } else {
         return 0;
      }
   }

   public record Config(int luminance, boolean waterSensitive, boolean selfLit) {
   }
}
