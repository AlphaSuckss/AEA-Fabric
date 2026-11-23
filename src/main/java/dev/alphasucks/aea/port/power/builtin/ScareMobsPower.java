package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.data.Shape;
import dev.alphasucks.aea.port.dev.DevParticles;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PoweredEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class ScareMobsPower extends PowerType<ScareMobsPower.Config> {
   private static final List<class_1309> FOUND = new ArrayList(4);
   private static long scannedTick = Long.MIN_VALUE;

   public static List<class_1309> holders(class_1937 level) {
      long now = level.method_8510();
      if (now == scannedTick) {
         return FOUND;
      }

      scannedTick = now;
      FOUND.clear();
      PoweredEntities.forEach(ScareMobsPower::collect);
      if (DevParticles.due(level)) {
         outlineHolders(level);
      }

      return FOUND;
   }

   private static void outlineHolders(class_1937 level) {
      for (int i = 0; i < FOUND.size(); i++) {
         class_1309 holder = (class_1309)FOUND.get(i);
         if (holder.method_37908() instanceof class_3218 devLevel) {
            PowerLookup.forEach(
               holder,
               ApoliIds.SCARE_MOBS,
               ScareMobsPower.Config.class,
               cfg -> DevParticles.outlineShape(devLevel, holder.method_19538(), Shape.SPHERE, cfg.radius(), cfg.radius(), cfg.radius())
            );
         }
      }
   }

   private static void collect(class_1297 entity) {
      if (entity instanceof class_1309 living && living.method_5805()) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            List<class_2960> powers = container.powersOfType(ApoliIds.SCARE_MOBS);

            for (int i = 0; i < powers.size(); i++) {
               if (!container.isSuppressed((class_2960)powers.get(i))) {
                  FOUND.add(living);
                  return;
               }
            }
         }
      }
   }

   @Override
   public MapCodec<ScareMobsPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ScareMobsPower.Config::bientityCondition),
               Codec.DOUBLE.optionalFieldOf("radius", 6.0).forGetter(ScareMobsPower.Config::radius),
               Codec.DOUBLE.optionalFieldOf("speed", 1.0).forGetter(ScareMobsPower.Config::speed)
            )
            .apply(i, ScareMobsPower.Config::new)
      );
   }

   public record Config(Optional<BiEntityCondition> bientityCondition, double radius, double speed) {
   }
}
