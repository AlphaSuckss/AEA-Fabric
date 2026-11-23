package dev.alphasucks.aea.port.client.render;

import dev.alphasucks.aea.port.data.ModelPartTransformation;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import dev.alphasucks.aea.port.power.builtin.ModelColorPower.PartColor;
import dev.alphasucks.aea.port.power.builtin.ModelColorPower.Tint;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class AttachmentParts {
   public static final int STRIDE = 5;
   private static final int PAIRED_EARS = 24;
   private static final int PAIRED_WINGS = 3;

   private AttachmentParts() {
   }

   public static float[] newColours() {
      float[] colours = new float[100];
      reset(colours);
      return colours;
   }

   public static int hiddenMask(@Nullable class_1309 entity) {
      if (entity == null) {
         return 0;
      }

      List<Slot> slots = ModelPartAnimator.update(entity);
      if (slots.isEmpty()) {
         return 0;
      }

      boolean handPass = HandRenderPass.active();
      int hidden = 0;

      for (int i = 0; i < slots.size(); i++) {
         Slot slot = (Slot)slots.get(i);
         if (!(slot.weight() < 0.5F) && slot.rendersIn(handPass)) {
            ModelPartTransformation transformation = slot.transformation();
            int bits = transformation.bodyPart().attachments();
            if (bits != 0) {
               boolean on = slot.value() != 0.0F;
               switch (transformation.type()) {
                  case VISIBLE:
                     hidden = on ? hidden & ~bits : hidden | bits;
                     break;
                  case HIDDEN:
                     hidden = on ? hidden | bits : hidden & ~bits;
               }
            }
         }
      }

      if ((hidden & 24) == 24) {
         hidden |= 32;
      }

      if ((hidden & 3) == 3) {
         hidden |= 4;
      }

      return hidden;
   }

   public static boolean colours(@Nullable class_1297 source, float[] out) {
      List<PartColor> parts = ModelColorState.partColors(source);
      boolean tinted = false;

      for (int i = 0; i < parts.size(); i++) {
         PartColor colour = (PartColor)parts.get(i);
         int bits = colour.part().attachments();
         if (bits != 0) {
            if (!tinted) {
               reset(out);
               tinted = true;
            }

            Tint tint = colour.tint();
            float red = tint.red(source);
            float green = tint.green(source);
            float blue = tint.blue(source);
            float alpha = tint.alpha(source);
            boolean whiten = tint.whitens(red, green, blue);

            while (bits != 0) {
               int at = Integer.numberOfTrailingZeros(bits) * 5;
               bits &= bits - 1;
               out[at] *= red;
               out[at + 1] = out[at + 1] * green;
               out[at + 2] = out[at + 2] * blue;
               out[at + 3] = out[at + 3] * alpha;
               if (whiten) {
                  out[at + 4] = 1.0F;
               }
            }
         }
      }

      return tinted;
   }

   public static boolean tinted(float[] colours, int at) {
      return colours[at] != 1.0F || colours[at + 1] != 1.0F || colours[at + 2] != 1.0F || colours[at + 3] != 1.0F || colours[at + 4] != 0.0F;
   }

   private static void reset(float[] colours) {
      for (int at = 0; at < colours.length; at += 5) {
         colours[at] = 1.0F;
         colours[at + 1] = 1.0F;
         colours[at + 2] = 1.0F;
         colours[at + 3] = 1.0F;
         colours[at + 4] = 0.0F;
      }
   }
}
