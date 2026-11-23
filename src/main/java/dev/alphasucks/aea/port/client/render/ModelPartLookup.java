package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.render.model.ExtraModelParts;
import dev.alphasucks.aea.port.data.BodyPart;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;

public final class ModelPartLookup {
   private static final ThreadLocal<List<class_630>> SCRATCH = ThreadLocal.withInitial(() -> new ArrayList(8));

   private ModelPartLookup() {
   }

   public static void resolveInto(class_572<?> model, BodyPart part, List<class_630> parts) {
      if (part.isEverything()) {
         allPartsInto(model, parts);
      } else {
         int mask = part.models();
         if (mask != 0) {
            if ((mask & 1) != 0) {
               parts.add(model.field_3398);
            }

            if ((mask & 2) != 0) {
               parts.add(model.field_3394);
            }

            if ((mask & 4) != 0) {
               parts.add(model.field_3391);
            }

            if ((mask & 16) != 0) {
               parts.add(model.field_3401);
            }

            if ((mask & 64) != 0) {
               parts.add(model.field_27433);
            }

            if ((mask & 256) != 0) {
               parts.add(model.field_3392);
            }

            if ((mask & 1024) != 0) {
               parts.add(model.field_3397);
            }

            if (model instanceof class_591<?> player) {
               if ((mask & 8) != 0) {
                  parts.add(player.field_3483);
               }

               if ((mask & 32) != 0) {
                  parts.add(player.field_3486);
               }

               if ((mask & 128) != 0) {
                  parts.add(player.field_3484);
               }

               if ((mask & 512) != 0) {
                  parts.add(player.field_3479);
               }

               if ((mask & 2048) != 0) {
                  parts.add(player.field_3482);
               }
            }
         }

         if ((part.isCustom() || part.isGroup()) && model instanceof ExtraModelParts extra) {
            extra.collectExtraParts(part.key(), parts);
         }
      }
   }

   public static boolean affects(class_572<?> model, BodyPart part, class_630 target) {
      List<class_630> parts = (List<class_630>)SCRATCH.get();
      parts.clear();
      resolveInto(model, part, parts);
      boolean found = false;

      for (int i = 0; i < parts.size(); i++) {
         if (parts.get(i) == target) {
            found = true;
            break;
         }
      }

      parts.clear();
      return found;
   }

   public static void allPartsInto(class_572<?> model, List<class_630> parts) {
      parts.add(model.field_3398);
      parts.add(model.field_3394);
      parts.add(model.field_3391);
      parts.add(model.field_3401);
      parts.add(model.field_27433);
      parts.add(model.field_3392);
      parts.add(model.field_3397);
      if (model instanceof class_591<?> player) {
         parts.add(player.field_3483);
         parts.add(player.field_3486);
         parts.add(player.field_3484);
         parts.add(player.field_3479);
         parts.add(player.field_3482);
      }

      if (model instanceof ExtraModelParts extra) {
         extra.collectExtraParts(parts);
      }
   }

   public static void limbPosesInto(class_572<?> model, float[] poses) {
      read(model.field_3398, poses, 0);
      read(model.field_3391, poses, 9);
      read(model.field_3401, poses, 18);
      read(model.field_27433, poses, 27);
      read(model.field_3392, poses, 36);
      read(model.field_3397, poses, 45);
   }

   public static void read(class_630 part, float[] pose, int at) {
      pose[at + 0] = part.field_3657;
      pose[at + 1] = part.field_3656;
      pose[at + 2] = part.field_3655;
      pose[at + 3] = part.field_3654;
      pose[at + 4] = part.field_3675;
      pose[at + 5] = part.field_3674;
      pose[at + 6] = part.field_37938;
      pose[at + 7] = part.field_37939;
      pose[at + 8] = part.field_37940;
   }

   public static void write(float[] pose, int at, class_630 part) {
      part.field_3657 = pose[at + 0];
      part.field_3656 = pose[at + 1];
      part.field_3655 = pose[at + 2];
      part.field_3654 = pose[at + 3];
      part.field_3675 = pose[at + 4];
      part.field_3674 = pose[at + 5];
      part.field_37938 = pose[at + 6];
      part.field_37939 = pose[at + 7];
      part.field_37940 = pose[at + 8];
   }
}
