package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.client.render.model.ExtraModelParts;
import dev.alphasucks.aea.port.compat.entitymodelfeatures.EntityModelFeaturesCompat;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.power.builtin.CustomModelRenderPower.ResolvedLayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_630;
import org.jetbrains.annotations.Nullable;

public final class TextureOverlays {
   private static final ThreadLocal<List<class_630>> PARTS = ThreadLocal.withInitial(() -> new ArrayList(8));

   private TextureOverlays() {
   }

   public static void render(
      class_583<?> model,
      ResolvedLayer layer,
      class_2960 texture,
      float alphaScale,
      float ageInTicks,
      @Nullable class_1297 subject,
      class_4587 pose,
      class_4597 buffers,
      int light
   ) {
      float alpha = layer.alpha() * alphaScale;
      class_4588 consumer = buffers.getBuffer(OverlayRenderTypes.forMode(layer.mode(), texture, ageInTicks, layer.scrollSpeed()));
      boolean scaled = layer.scale() != 1.0F;
      if (scaled) {
         pose.method_22903();
         pose.method_22905(layer.scale(), layer.scale(), layer.scale());
      }

      List<class_630> parts = layer.wholeModel() ? null : partsOf(model, layer.bodyParts(), subject);
      ModelPartNames.Named named = parts != null && !(model instanceof class_572) && !(model instanceof ExtraModelParts) ? ModelPartNames.named(model) : null;
      EntityModelFeaturesCompat.holdPose();

      try {
         if (parts == null) {
            model.method_2828(pose, consumer, light, class_4608.field_21444, layer.red(), layer.green(), layer.blue(), alpha);
         } else {
            for (int i = 0; i < parts.size(); i++) {
               class_630 part = (class_630)parts.get(i);
               if (parts.indexOf(part) == i) {
                  class_630[] chain = named == null ? null : named.chain(part);
                  if (chain != null && chain.length != 0) {
                     pose.method_22903();

                     for (int c = 0; c < chain.length; c++) {
                        chain[c].method_22703(pose);
                     }

                     part.method_22699(pose, consumer, light, class_4608.field_21444, layer.red(), layer.green(), layer.blue(), alpha);
                     pose.method_22909();
                  } else {
                     part.method_22699(pose, consumer, light, class_4608.field_21444, layer.red(), layer.green(), layer.blue(), alpha);
                  }
               }
            }

            parts.clear();
         }
      } finally {
         EntityModelFeaturesCompat.releasePose();
      }

      if (scaled) {
         pose.method_22909();
      }
   }

   @Nullable
   private static List<class_630> partsOf(class_583<?> model, List<BodyPart> bodyParts, @Nullable class_1297 subject) {
      List<class_630> parts = (List<class_630>)PARTS.get();
      parts.clear();
      if (model instanceof class_572<?> humanoid) {
         for (int i = 0; i < bodyParts.size(); i++) {
            ModelPartLookup.resolveInto(humanoid, ((BodyPart)bodyParts.get(i)).sided(subject), parts);
         }

         return parts;
      } else if (!(model instanceof ExtraModelParts extra)) {
         Map<String, class_630> named = ModelPartNames.of(model);
         if (named.isEmpty()) {
            return null;
         }

         for (int i = 0; i < bodyParts.size(); i++) {
            BodyPart part = (BodyPart)bodyParts.get(i);
            if (part.isEverything()) {
               return null;
            }

            class_630 found = (class_630)named.get(part.key());
            if (found != null) {
               parts.add(found);
            }
         }

         return parts;
      } else {
         for (int i = 0; i < bodyParts.size(); i++) {
            extra.collectExtraParts(((BodyPart)bodyParts.get(i)).key(), parts);
         }

         return parts;
      }
   }

   public static void renderArm(
      ResolvedLayer layer,
      class_2960 texture,
      float alphaScale,
      float ageInTicks,
      class_630 arm,
      class_630 sleeve,
      class_4587 pose,
      class_4597 buffers,
      int light
   ) {
      float alpha = layer.alpha() * alphaScale;
      class_4588 consumer = buffers.getBuffer(OverlayRenderTypes.forMode(layer.mode(), texture, ageInTicks, layer.scrollSpeed()));
      boolean scaled = layer.scale() != 1.0F;
      if (scaled) {
         pose.method_22903();
         pose.method_22905(layer.scale(), layer.scale(), layer.scale());
      }

      EntityModelFeaturesCompat.holdPose();

      try {
         arm.method_22699(pose, consumer, light, class_4608.field_21444, layer.red(), layer.green(), layer.blue(), alpha);
         sleeve.method_22699(pose, consumer, light, class_4608.field_21444, layer.red(), layer.green(), layer.blue(), alpha);
      } finally {
         EntityModelFeaturesCompat.releasePose();
      }

      if (scaled) {
         pose.method_22909();
      }
   }

   public static boolean affectsArm(class_572<?> model, ResolvedLayer layer, @Nullable class_1297 subject, class_630 arm, class_630 sleeve) {
      if (layer.wholeModel()) {
         return true;
      }

      List<BodyPart> bodyParts = layer.bodyParts();

      for (int i = 0; i < bodyParts.size(); i++) {
         BodyPart part = ((BodyPart)bodyParts.get(i)).sided(subject);
         if (ModelPartLookup.affects(model, part, arm) || ModelPartLookup.affects(model, part, sleeve)) {
            return true;
         }
      }

      return false;
   }
}
