package dev.alphasucks.aea.port.compat.icarus;

import dev.alphasucks.aea.port.client.disguise.ClientDisguiseManager;
import dev.alphasucks.aea.port.client.render.AttachmentParts;
import dev.alphasucks.aea.port.client.render.HandRenderPass;
import dev.alphasucks.aea.port.client.render.ModelColorState;
import dev.alphasucks.aea.port.client.render.ModelPartAnimator;
import dev.alphasucks.aea.port.client.render.ModelPartEdits;
import dev.alphasucks.aea.port.data.BodyAttachments;
import dev.alphasucks.aea.port.data.ModelPartTimeline.Slot;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_630;

@Environment(EnvType.CLIENT)
public final class IcarusWingParts {
   private static final float[] COLOURS = AttachmentParts.newColours();
   private static final int RIGHT = BodyAttachments.index(1) * 5;
   private static final int LEFT = BodyAttachments.index(2) * 5;
   private float[] rightOriginal;
   private float[] leftOriginal;
   private boolean edited;

   public void restore(class_630 rightWing, class_630 leftWing) {
      if (this.edited) {
         ModelPartEdits.restore(rightWing, this.rightOriginal);
         ModelPartEdits.restore(leftWing, this.leftOriginal);
         this.edited = false;
      }
   }

   public void edit(class_1309 entity, class_630 rightWing, class_630 leftWing) {
      if (this.rightOriginal == null) {
         this.rightOriginal = ModelPartEdits.snapshot(rightWing);
         this.leftOriginal = ModelPartEdits.snapshot(leftWing);
      }

      if (apply(entity, rightWing, leftWing, this.rightOriginal, this.leftOriginal)) {
         this.edited = true;
      }
   }

   private static boolean apply(class_1309 entity, class_630 rightWing, class_630 leftWing, float[] rightOriginal, float[] leftOriginal) {
      boolean edited = false;
      List<Slot> slots = ModelPartAnimator.update(entity);
      if (!slots.isEmpty()) {
         boolean handPass = HandRenderPass.active();

         for (int i = 0; i < slots.size(); i++) {
            Slot slot = (Slot)slots.get(i);
            if (!(slot.weight() <= 0.0F) && slot.rendersIn(handPass)) {
               int bits = slot.transformation().bodyPart().attachments();
               if ((bits & 1) != 0) {
                  ModelPartEdits.apply(rightWing, slot, rightOriginal);
                  edited = true;
               }

               if ((bits & 2) != 0) {
                  ModelPartEdits.apply(leftWing, slot, leftOriginal);
                  edited = true;
               }
            }
         }
      }

      if (AttachmentParts.colours(ClientDisguiseManager.powerSource(entity), COLOURS)) {
         if (AttachmentParts.tinted(COLOURS, RIGHT)) {
            ModelColorState.attach(rightWing, COLOURS, RIGHT);
         }

         if (AttachmentParts.tinted(COLOURS, LEFT)) {
            ModelColorState.attach(leftWing, COLOURS, LEFT);
         }
      }

      return edited;
   }
}
