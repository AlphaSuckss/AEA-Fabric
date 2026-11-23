package dev.alphasucks.aea.port.client.render.model;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.client.render.HandRenderPass;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3532;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_591;
import net.minecraft.class_630;

public class CentaurPlayerModel<T extends class_1309> extends class_591<T> implements ExtraModelParts, ExtraTexturedModel {
   public static final class_2960 HORSE_TEXTURE = Apoli.id("textures/entity/centaur/horse.png");
   private static final float TORSO_Y = -7.0F;
   private static final float TORSO_Z = -2.0F;
   private static final float TAIL_PITCH = (float)Math.toRadians(32.5);
   private static final float LEG_SWING = 0.55F;
   private static final float TAIL_SWING = 0.12F;
   private static final float MANE_SWING = 0.06F;
   private static final float GAIT = 0.6662F;
   public final class_630 horseBody;
   public final class_630 horseMane;
   public final class_630 horseTail;
   public final class_630 frontLeftLeg;
   public final class_630 frontRightLeg;
   public final class_630 backLeftLeg;
   public final class_630 backRightLeg;
   private final List<class_630> horseParts;
   private final List<class_630> riderParts;

   public CentaurPlayerModel(class_630 root, boolean slim) {
      super(root, slim);
      this.horseBody = root.method_32086("horse_body");
      this.horseMane = root.method_32086("horse_mane");
      this.horseTail = root.method_32086("horse_tail");
      this.frontLeftLeg = root.method_32086("front_left_leg");
      this.frontRightLeg = root.method_32086("front_right_leg");
      this.backLeftLeg = root.method_32086("back_left_leg");
      this.backRightLeg = root.method_32086("back_right_leg");
      this.horseParts = List.of(this.horseBody, this.horseMane, this.horseTail, this.frontLeftLeg, this.frontRightLeg, this.backLeftLeg, this.backRightLeg);
      this.riderParts = List.of(
         new class_630[]{
            this.field_3398,
            this.field_3394,
            this.field_3391,
            this.field_3483,
            this.field_3401,
            this.field_3486,
            this.field_27433,
            this.field_3484,
            this.field_3392,
            this.field_3479,
            this.field_3397,
            this.field_3482
         }
      );
   }

   public static class_5607 createLayer(boolean slim) {
      class_5609 mesh = class_591.method_32028(class_5605.field_27715, slim);
      class_5610 root = mesh.method_32111();
      root.method_32117(
         "horse_body",
         class_5606.method_32108().method_32101(0, 32).method_32098(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F, new class_5605(0.05F)),
         class_5603.method_32090(0.0F, 11.0F, 14.0F)
      );
      root.method_32117(
         "horse_mane",
         class_5606.method_32108().method_32101(56, 36).method_32097(-1.0F, -4.0F, 0.01F, 2.0F, 9.0F, 2.0F),
         class_5603.method_32090(0.0F, 2.0F, -0.01F)
      );
      root.method_32117(
         "horse_tail",
         class_5606.method_32108().method_32101(42, 36).method_32097(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F),
         class_5603.method_32091(0.0F, 6.0F, 16.0F, TAIL_PITCH, 0.0F, 0.0F)
      );
      root.method_32117(
         "front_left_leg",
         class_5606.method_32108().method_32101(48, 21).method_32096().method_32097(-3.0F, -1.0F, -1.9F, 4.0F, 11.0F, 4.0F),
         class_5603.method_32090(4.0F, 14.0F, -1.0F)
      );
      root.method_32117(
         "front_right_leg",
         class_5606.method_32108().method_32101(48, 21).method_32097(-1.0F, -1.0F, -1.9F, 4.0F, 11.0F, 4.0F),
         class_5603.method_32090(-4.0F, 14.0F, -1.0F)
      );
      root.method_32117(
         "back_left_leg",
         class_5606.method_32108().method_32101(48, 21).method_32096().method_32097(-3.0F, -1.0F, -1.0F, 4.0F, 11.0F, 4.0F),
         class_5603.method_32090(4.0F, 14.0F, 16.0F)
      );
      root.method_32117(
         "back_right_leg",
         class_5606.method_32108().method_32101(48, 21).method_32097(-1.0F, -1.0F, -1.0F, 4.0F, 11.0F, 4.0F),
         class_5603.method_32090(-4.0F, 14.0F, 16.0F)
      );
      return class_5607.method_32110(mesh, 64, 64);
   }

   @Override
   public void method_17087(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (HandRenderPass.active()) {
         HandPassArms.reset(this);
         super.method_17087(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      } else {
         for (int i = 0; i < this.riderParts.size(); i++) {
            ((class_630)this.riderParts.get(i)).method_41923();
         }

         super.method_17087(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         this.field_3392.field_3665 = false;
         this.field_3397.field_3665 = false;
         this.field_3479.field_3665 = false;
         this.field_3482.field_3665 = false;

         for (int i = 0; i < this.riderParts.size(); i++) {
            class_630 part = (class_630)this.riderParts.get(i);
            part.field_3656 += -7.0F;
            part.field_3655 += -2.0F;
         }

         float swing = class_3532.method_15362(limbSwing * 0.6662F) * 0.55F * limbSwingAmount;
         this.frontLeftLeg.field_3654 = -swing;
         this.backRightLeg.field_3654 = -swing;
         this.frontRightLeg.field_3654 = swing;
         this.backLeftLeg.field_3654 = swing;
         this.horseTail.field_3654 = TAIL_PITCH + class_3532.method_15379(swing) * 0.12F;
         this.horseTail.field_3675 = class_3532.method_15374(ageInTicks * 0.06F) * 0.12F;
         this.horseMane.field_3654 = -class_3532.method_15379(swing) * 0.06F;
      }
   }

   @Override
   public void method_2805(boolean visible) {
      super.method_2805(visible);

      for (int i = 0; i < this.horseParts.size(); i++) {
         ((class_630)this.horseParts.get(i)).field_3665 = visible;
      }
   }

   @Override
   public class_2960 defaultExtraTexture() {
      return HORSE_TEXTURE;
   }

   @Override
   public List<class_630> extraTexturedParts() {
      return this.horseParts;
   }

   @Override
   public void collectExtraParts(String normalized, List<class_630> parts) {
      switch (normalized) {
         case "horsebody":
            parts.add(this.horseBody);
            break;
         case "horsemane":
         case "mane":
            parts.add(this.horseMane);
            break;
         case "horsetail":
         case "tail":
            parts.add(this.horseTail);
            break;
         case "frontleftleg":
            parts.add(this.frontLeftLeg);
            break;
         case "frontrightleg":
            parts.add(this.frontRightLeg);
            break;
         case "backleftleg":
         case "hindleftleg":
            parts.add(this.backLeftLeg);
            break;
         case "backrightleg":
         case "hindrightleg":
            parts.add(this.backRightLeg);
            break;
         case "horselegs":
         case "legs":
            parts.add(this.frontLeftLeg);
            parts.add(this.frontRightLeg);
            parts.add(this.backLeftLeg);
            parts.add(this.backRightLeg);
            break;
         case "horse":
         case "lower":
            parts.addAll(this.horseParts);
      }
   }

   @Override
   public void collectExtraParts(List<class_630> parts) {
      parts.addAll(this.horseParts);
   }
}
