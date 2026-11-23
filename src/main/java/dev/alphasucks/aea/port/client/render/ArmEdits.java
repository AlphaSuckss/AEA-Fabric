package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.ModelPartTransformation;
import dev.alphasucks.aea.port.data.PoseMath;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import dev.alphasucks.aea.port.data.ModelPartTransformation.Type;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1309;
import net.minecraft.class_3532;

public final class ArmEdits {
   private static final int RIGHT_EDITED = 1;
   private static final int RIGHT_HELD = 2;
   private static final int RIGHT_REACHING = 4;
   private static final int LEFT_EDITED = 8;
   private static final int LEFT_HELD = 16;
   private static final int LEFT_REACHING = 32;

   private ArmEdits() {
   }

   public static int of(class_1309 entity, boolean firstPerson) {
      List<Slot> slots = ModelPartAnimator.update(entity);
      if (slots.isEmpty()) {
         return 0;
      }

      int state = 0;

      for (int i = 0; i < slots.size(); i++) {
         Slot slot = (Slot)slots.get(i);
         float weight = slot.weight();
         if (!(weight <= 0.0F) && slot.rendersIn(firstPerson)) {
            ModelPartTransformation transformation = slot.transformation();
            if (PoseMath.isSpatial(transformation.type())) {
               BodyPart part = transformation.bodyPart().sided(entity);
               if (part.models() != 0) {
                  int limbs = part.limbs();
                  boolean right = (limbs & 4) != 0;
                  boolean left = (limbs & 8) != 0;
                  if (right || left) {
                     boolean reaching = transformation.type() == Type.REACH;
                     boolean held = reaching
                        && (transformation.overrideAnimation() || !firstPerson)
                        && class_3532.method_15363(slot.value(), 0.0F, 1.0F) * weight > 0.0F;
                     if (right) {
                        state |= 1 | (reaching ? 4 : 0) | (held ? 2 : 0);
                     }

                     if (left) {
                        state |= 8 | (reaching ? 32 : 0) | (held ? 16 : 0);
                     }
                  }
               }
            }
         }
      }

      return state;
   }

   public static boolean edited(int state, class_1306 arm) {
      return (state & (arm == class_1306.field_6183 ? 1 : 8)) != 0;
   }

   public static boolean held(int state, class_1306 arm) {
      return (state & (arm == class_1306.field_6183 ? 2 : 16)) != 0;
   }

   public static boolean reaching(int state, class_1306 arm) {
      return (state & (arm == class_1306.field_6183 ? 4 : 32)) != 0;
   }

   public static class_1306 armFor(class_1309 entity, class_1268 hand) {
      return hand == class_1268.field_5808 ? entity.method_6068() : entity.method_6068().method_5928();
   }
}
