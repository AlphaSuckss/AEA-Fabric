package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.data.ArmPoseReference;
import net.minecraft.class_572.class_573;

public final class ArmPoseReferenceClient {
   private ArmPoseReferenceClient() {
   }

   public static class_573 toVanilla(ArmPoseReference ref) {
      return switch (ref) {
         case EMPTY -> class_573.field_3409;
         case ITEM -> class_573.field_3410;
         case BLOCK -> class_573.field_3406;
         case BOW_AND_ARROW -> class_573.field_3403;
         case THROW_SPEAR -> class_573.field_3407;
         case CROSSBOW_CHARGE -> class_573.field_3405;
         case CROSSBOW_HOLD -> class_573.field_3408;
         case SPYGLASS -> class_573.field_27434;
         case TOOT_HORN -> class_573.field_39071;
         case BRUSH -> class_573.field_42877;
      };
   }
}
