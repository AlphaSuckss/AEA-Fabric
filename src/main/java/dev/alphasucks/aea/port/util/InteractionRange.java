package dev.alphasucks.aea.port.util;

import dev.alphasucks.aea.port.attribute.ApoliAttributes;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;

public final class InteractionRange {
   private static final double CREATIVE_BLOCK_BONUS = 0.5;
   private static final double CREATIVE_ENTITY_BONUS = 3.0;

   private InteractionRange() {
   }

   public static double block(class_1297 entity) {
      return entity instanceof class_1309 living ? ApoliAttributes.blockInteractionRange(living) + (creative(living) ? 0.5 : 0.0) : 4.5;
   }

   public static double entity(class_1297 entity) {
      return entity instanceof class_1309 living ? ApoliAttributes.entityInteractionRange(living) + (creative(living) ? 3.0 : 0.0) : 3.0;
   }

   public static float block(class_1297 source, Optional<Float> own, Optional<Float> shared) {
      if (own.isPresent()) {
         return (Float)own.get();
      } else {
         return shared.isPresent() ? (Float)shared.get() : (float)block(source);
      }
   }

   public static float entity(class_1297 source, Optional<Float> own, Optional<Float> shared) {
      if (own.isPresent()) {
         return (Float)own.get();
      } else {
         return shared.isPresent() ? (Float)shared.get() : (float)entity(source);
      }
   }

   private static boolean creative(class_1309 entity) {
      return entity instanceof class_1657 player && player.method_31549().field_7477;
   }
}
