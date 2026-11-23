package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.mixin.flag.LivingEntityAirAccessor;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_2338;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_3486;

public final class WaterBreathingPower extends PowerType<WaterBreathingPower.Config> {
   private static final int DROWN_AT = -20;
   private static final float DROWN_DAMAGE = 2.0F;

   @Override
   public MapCodec<WaterBreathingPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.BOOL.optionalFieldOf("suffocate_outside_water", false).forGetter(WaterBreathingPower.Config::suffocateOutsideWater))
            .apply(i, WaterBreathingPower.Config::new)
      );
   }

   public static boolean canBreatheUnderwater(class_1309 entity) {
      PowerContainer container = PowerContainer.of(entity);
      if (container == null || container.isEmpty()) {
         return false;
      } else {
         return container.powersOfType(ApoliIds.WATER_BREATHING).isEmpty() ? false : PowerLookup.hasActive(entity, ApoliIds.WATER_BREATHING);
      }
   }

   public static boolean suffocatesOutsideWater(class_1309 entity) {
      return PowerLookup.anyActive(entity, ApoliIds.WATER_BREATHING, WaterBreathingPower.Config.class, WaterBreathingPower.Config::suffocateOutsideWater);
   }

   public static void tick(class_1309 entity) {
      if (entity.method_5805() && suffocatesOutsideWater(entity)) {
         LivingEntityAirAccessor air = (LivingEntityAirAccessor)entity;
         if (!shouldSuffocate(entity)) {
            int current = entity.method_5669();
            if (current < entity.method_5748()) {
               entity.method_5855(air.apoli$increaseAirSupply(current));
            }
         } else {
            int refunded = air.apoli$increaseAirSupply(0);
            if (isInRain(entity)) {
               entity.method_5855(entity.method_5669() - refunded);
            } else {
               entity.method_5855(air.apoli$decreaseAirSupply(entity.method_5669()) - refunded);
               if (entity.method_5669() <= -20) {
                  entity.method_5855(0);
                  if (entity.method_37908().method_8608()) {
                     class_243 motion = entity.method_18798();

                     for (int i = 0; i < 8; i++) {
                        double dx = entity.method_6051().method_43058() - entity.method_6051().method_43058();
                        double dy = entity.method_6051().method_43058() - entity.method_6051().method_43058();
                        double dz = entity.method_6051().method_43058() - entity.method_6051().method_43058();
                        entity.method_37908()
                           .method_8406(
                              class_2398.field_11247,
                              entity.method_23317() + dx,
                              entity.method_23318() + dy,
                              entity.method_23321() + dz,
                              motion.field_1352,
                              motion.field_1351,
                              motion.field_1350
                           );
                     }
                  } else {
                     entity.method_5643(entity.method_48923().method_48824(), 2.0F);
                  }
               }
            }
         }
      }
   }

   private static boolean shouldSuffocate(class_1309 entity) {
      return !entity.method_5777(class_3486.field_15517) && !entity.method_6059(class_1294.field_5923) && !entity.method_6059(class_1294.field_5927);
   }

   private static boolean isInRain(class_1309 entity) {
      class_2338 pos = entity.method_24515();
      return entity.method_37908().method_8520(pos)
         || entity.method_37908().method_8520(class_2338.method_49637(pos.method_10263(), entity.method_5829().field_1325, pos.method_10260()));
   }

   public record Config(boolean suffocateOutsideWater) {
   }
}
