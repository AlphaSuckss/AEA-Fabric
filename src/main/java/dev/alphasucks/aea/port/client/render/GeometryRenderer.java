package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.model.CustomModel;
import dev.alphasucks.aea.port.client.model.CustomModel.Bone;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.GeometryRender;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_5603;
import net.minecraft.class_572;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_742;
import org.jetbrains.annotations.Nullable;

public final class GeometryRenderer {
   private static final String[] LIMB_KEYS = new String[]{"head", "body", "rightarm", "leftarm", "rightleg", "leftleg"};

   private GeometryRenderer() {
   }

   public static void resetAll(CustomModel model) {
      Bone[] all = model.bones();

      for (int i = 0; i < all.length; i++) {
         all[i].reset();
      }
   }

   public static void syncPlayer(CustomModel custom, class_591<class_742> live, class_591<class_742> rest) {
      resetAll(custom);
      syncBone(custom, "head", live.field_3398, rest.field_3398);
      syncBone(custom, "hat", live.field_3394, rest.field_3394);
      syncBone(custom, "body", live.field_3391, rest.field_3391);
      syncBone(custom, "rightarm", live.field_3401, rest.field_3401);
      syncBone(custom, "leftarm", live.field_27433, rest.field_27433);
      syncBone(custom, "rightleg", live.field_3392, rest.field_3392);
      syncBone(custom, "leftleg", live.field_3397, rest.field_3397);
   }

   public static void syncHumanoid(CustomModel custom, class_572<?> live) {
      class_591<class_742> rest = PlayerRestPose.get();
      resetAll(custom);
      syncBone(custom, "head", live.field_3398, rest.field_3398);
      syncBone(custom, "hat", live.field_3394, rest.field_3394);
      syncBone(custom, "body", live.field_3391, rest.field_3391);
      syncBone(custom, "rightarm", live.field_3401, rest.field_3401);
      syncBone(custom, "leftarm", live.field_27433, rest.field_27433);
      syncBone(custom, "rightleg", live.field_3392, rest.field_3392);
      syncBone(custom, "leftleg", live.field_3397, rest.field_3397);
   }

   public static void syncNamed(CustomModel custom, Map<String, class_630> parts) {
      resetAll(custom);
      if (!parts.isEmpty()) {
         for (Entry<String, class_630> entry : parts.entrySet()) {
            Bone[] bound = custom.bones((String)entry.getKey());
            if (bound.length != 0) {
               class_630 live = (class_630)entry.getValue();
               class_5603 rest = live.method_41921();

               for (int i = 0; i < bound.length; i++) {
                  poseFromInitial(bound[i], live, rest);
               }
            }
         }
      }
   }

   private static void poseFromInitial(Bone bone, class_630 live, class_5603 rest) {
      bone.reset();
      class_630 part = bone.part;
      part.field_37938 = live.field_37938;
      part.field_37939 = live.field_37939;
      part.field_37940 = live.field_37940;
      part.field_3657 = part.field_3657 + (live.field_3657 - rest.field_27702);
      part.field_3656 = part.field_3656 + (live.field_3656 - rest.field_27703);
      part.field_3655 = part.field_3655 + (live.field_3655 - rest.field_27704);
      part.field_3654 = part.field_3654 + (live.field_3654 - rest.field_27705);
      part.field_3675 = part.field_3675 + (live.field_3675 - rest.field_27706);
      part.field_3674 = part.field_3674 + (live.field_3674 - rest.field_27707);
   }

   public static void syncBone(CustomModel model, String normalizedName, @Nullable class_630 live, @Nullable class_630 rest) {
      if (live != null) {
         Bone[] bound = model.bones(normalizedName);

         for (int i = 0; i < bound.length; i++) {
            pose(bound[i], live, rest);
         }
      }
   }

   private static void pose(Bone bone, class_630 live, @Nullable class_630 rest) {
      bone.reset();
      class_630 part = bone.part;
      part.field_37938 = live.field_37938;
      part.field_37939 = live.field_37939;
      part.field_37940 = live.field_37940;
      if (rest == null) {
         part.field_3654 = part.field_3654 + live.field_3654;
         part.field_3675 = part.field_3675 + live.field_3675;
         part.field_3674 = part.field_3674 + live.field_3674;
      } else {
         part.field_3657 = part.field_3657 + (live.field_3657 - rest.field_3657);
         part.field_3656 = part.field_3656 + (live.field_3656 - rest.field_3656);
         part.field_3655 = part.field_3655 + (live.field_3655 - rest.field_3655);
         part.field_3654 = part.field_3654 + (live.field_3654 - rest.field_3654);
         part.field_3675 = part.field_3675 + (live.field_3675 - rest.field_3675);
         part.field_3674 = part.field_3674 + (live.field_3674 - rest.field_3674);
      }
   }

   public static void applyVisibility(CustomModel model, List<BodyPart> bodyParts) {
      Bone[] all = model.bones();
      if (bodyParts.isEmpty()) {
         for (int i = 0; i < all.length; i++) {
            all[i].part.field_3665 = true;
         }
      } else {
         for (int i = 0; i < all.length; i++) {
            all[i].part.field_3665 = false;
         }

         for (int i = 0; i < bodyParts.size(); i++) {
            BodyPart part = (BodyPart)bodyParts.get(i);
            if (part.isEverything()) {
               for (int j = 0; j < all.length; j++) {
                  all[j].part.field_3665 = true;
               }

               return;
            }

            show(model, part.key());
            if (part.isGroup()) {
               int limbs = part.limbs();

               for (int limb = 0; limb < LIMB_KEYS.length; limb++) {
                  if ((limbs & 1 << limb) != 0) {
                     show(model, LIMB_KEYS[limb]);
                     if (limb == 0) {
                        show(model, "hat");
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean nestedInAnother(Bone[] bound, int index, class_630[] ancestors) {
      if (ancestors.length == 0) {
         return false;
      }

      for (int j = 0; j < bound.length; j++) {
         if (j != index) {
            class_630 other = bound[j].part;

            for (int k = 0; k < ancestors.length; k++) {
               if (ancestors[k] == other) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean visible(class_630[] parts) {
      for (int i = 0; i < parts.length; i++) {
         if (!parts[i].field_3665) {
            return false;
         }
      }

      return true;
   }

   private static void show(CustomModel model, String key) {
      Bone[] bound = model.bones(key);

      for (int j = 0; j < bound.length; j++) {
         bound[j].part.field_3665 = true;
      }
   }

   public static void draw(GeometryRender render, CustomModel model, class_4587 pose, class_4597 buffers, int light) {
      draw(render, model, pose, buffers, light, null);
   }

   public static void draw(GeometryRender render, CustomModel model, class_4587 pose, class_4597 buffers, int light, @Nullable class_1297 subject) {
      class_4588 consumer = buffers.getBuffer(
         OverlayRenderTypes.forGeometry(
            render.mode(), DynamicTextures.resolve(render.texture(), subject), ModelPartAnimator.ageInTicks(subject), render.scrollSpeed()
         )
      );
      Vector offset = render.offset();
      boolean shifted = offset.x() != 0.0F || offset.y() != 0.0F || offset.z() != 0.0F;
      boolean scaled = render.scale() != 1.0F;
      if (shifted || scaled) {
         pose.method_22903();
         if (shifted) {
            pose.method_46416(offset.x(), -offset.y(), -offset.z());
         }

         if (scaled) {
            pose.method_22905(render.scale(), render.scale(), render.scale());
         }
      }

      model.root().method_22699(pose, consumer, light, class_4608.field_21444, render.red(), render.green(), render.blue(), render.alpha());
      if (shifted || scaled) {
         pose.method_22909();
      }
   }

   public static void drawSlot(
      GeometryRender render, CustomModel model, String normalizedName, float alphaScale, class_4587 pose, class_4597 buffers, int light
   ) {
      drawSlot(render, model, normalizedName, alphaScale, pose, buffers, light, null);
   }

   public static void drawSlot(
      GeometryRender render,
      CustomModel model,
      String normalizedName,
      float alphaScale,
      class_4587 pose,
      class_4597 buffers,
      int light,
      @Nullable class_1297 subject
   ) {
      drawSlot(render, model, normalizedName, alphaScale, pose, buffers, light, subject, null);
   }

   public static void drawSlot(
      GeometryRender render,
      CustomModel model,
      String normalizedName,
      float alphaScale,
      class_4587 pose,
      class_4597 buffers,
      int light,
      @Nullable class_1297 subject,
      @Nullable class_630 anchor
   ) {
      Bone[] bound = model.bones(normalizedName);
      if (bound.length != 0) {
         class_4588 consumer = buffers.getBuffer(
            OverlayRenderTypes.forGeometry(
               render.mode(), DynamicTextures.resolve(render.texture(), subject), ModelPartAnimator.ageInTicks(subject), render.scrollSpeed()
            )
         );
         boolean scaled = render.scale() != 1.0F;
         if (scaled) {
            pose.method_22903();
            pose.method_22905(render.scale(), render.scale(), render.scale());
         }

         for (int i = 0; i < bound.length; i++) {
            Bone bone = bound[i];
            class_630[] ancestors = bone.ancestors();
            if (visible(ancestors) && !nestedInAnother(bound, i, ancestors)) {
               class_630 part = bone.part;
               if (anchor == null) {
                  part.method_22699(pose, consumer, light, class_4608.field_21444, render.red(), render.green(), render.blue(), render.alpha() * alphaScale);
               } else {
                  float x = part.field_3657;
                  float y = part.field_3656;
                  float z = part.field_3655;
                  float xRot = part.field_3654;
                  float yRot = part.field_3675;
                  float zRot = part.field_3674;
                  part.field_3657 = 0.0F;
                  part.field_3656 = 0.0F;
                  part.field_3655 = 0.0F;
                  part.field_3654 = xRot - bone.restXRot();
                  part.field_3675 = yRot - bone.restYRot();
                  part.field_3674 = zRot - bone.restZRot();
                  pose.method_22903();
                  anchor.method_22703(pose);
                  part.method_22699(pose, consumer, light, class_4608.field_21444, render.red(), render.green(), render.blue(), render.alpha() * alphaScale);
                  pose.method_22909();
                  part.field_3657 = x;
                  part.field_3656 = y;
                  part.field_3655 = z;
                  part.field_3654 = xRot;
                  part.field_3675 = yRot;
                  part.field_3674 = zRot;
               }
            }
         }

         if (scaled) {
            pose.method_22909();
         }
      }
   }
}
