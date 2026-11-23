package dev.alphasucks.aea.port.client.render.model;

import dev.alphasucks.aea.port.client.render.HandRenderPass;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_5603;
import net.minecraft.class_5605;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_591;
import net.minecraft.class_630;

public class FourArmsPlayerModel<T extends class_1309> extends class_591<T> implements ExtraModelParts {
   private static final float ARM_PITCH = (float)Math.toRadians(17.0);
   private static final float ARM_ROLL = (float)Math.toRadians(27.0);
   private static final float SECOND_ARM_ROLL = (float)Math.toRadians(17.0);
   private static final float SECOND_ARM_DROP = 3.0F;
   public final class_630 rightSecondArm;
   public final class_630 rightSecondSleeve;
   public final class_630 leftSecondArm;
   public final class_630 leftSecondSleeve;
   private final List<class_630> extraParts;
   private final List<class_630> bodyParts;

   public FourArmsPlayerModel(class_630 root, boolean slim) {
      super(root, slim);
      this.rightSecondArm = root.method_32086("right_second_arm");
      this.rightSecondSleeve = root.method_32086("right_second_sleeve");
      this.leftSecondArm = root.method_32086("left_second_arm");
      this.leftSecondSleeve = root.method_32086("left_second_sleeve");
      this.extraParts = List.of(this.rightSecondArm, this.leftSecondArm, this.rightSecondSleeve, this.leftSecondSleeve);
      List<class_630> parts = new ArrayList();
      super.method_22948().forEach(parts::add);
      parts.addAll(this.extraParts);
      this.bodyParts = List.copyOf(parts);
   }

   public static class_5607 createLayer(boolean slim) {
      class_5609 mesh = class_591.method_32028(class_5605.field_27715, slim);
      class_5610 root = mesh.method_32111();
      class_5605 none = class_5605.field_27715;
      class_5605 sleeve = none.method_32094(0.25F);
      float width = slim ? 3.0F : 4.0F;
      float leftInset = -1.0F;
      float rightInset = slim ? -2.0F : -3.0F;
      float pivotY = slim ? 5.5F : 5.0F;
      root.method_32117(
         "left_second_arm",
         class_5606.method_32108().method_32101(32, 48).method_32098(leftInset, -2.0F, -2.0F, width, 12.0F, 4.0F, none),
         class_5603.method_32090(5.0F, pivotY, 0.0F)
      );
      root.method_32117(
         "right_second_arm",
         class_5606.method_32108().method_32101(40, 16).method_32098(rightInset, -2.0F, -2.0F, width, 12.0F, 4.0F, none),
         class_5603.method_32090(-5.0F, pivotY, 0.0F)
      );
      root.method_32117(
         "left_second_sleeve",
         class_5606.method_32108().method_32101(48, 48).method_32098(leftInset, -2.0F, -2.0F, width, 12.0F, 4.0F, sleeve),
         class_5603.method_32090(5.0F, pivotY, 0.0F)
      );
      root.method_32117(
         "right_second_sleeve",
         class_5606.method_32108().method_32101(40, 32).method_32098(rightInset, -2.0F, -2.0F, width, 12.0F, 4.0F, sleeve),
         class_5603.method_32090(-5.0F, pivotY, 0.0F)
      );
      return class_5607.method_32110(mesh, 64, 64);
   }

   @Override
   protected Iterable<class_630> method_22948() {
      return this.bodyParts;
   }

   @Override
   public void method_17087(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      if (HandRenderPass.active()) {
         HandPassArms.reset(this);
         super.method_17087(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
      } else {
         for (int i = 0; i < this.bodyParts.size(); i++) {
            ((class_630)this.bodyParts.get(i)).method_41923();
         }

         super.method_17087(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         this.field_3401.field_3654 = this.field_3401.field_3654 - ARM_PITCH;
         this.field_3486.field_3654 = this.field_3486.field_3654 - ARM_PITCH;
         this.field_3401.field_3674 = this.field_3401.field_3674 + ARM_ROLL;
         this.field_3486.field_3674 = this.field_3486.field_3674 + ARM_ROLL;
         this.field_27433.field_3654 = this.field_27433.field_3654 - ARM_PITCH;
         this.field_3484.field_3654 = this.field_3484.field_3654 - ARM_PITCH;
         this.field_27433.field_3674 = this.field_27433.field_3674 - ARM_ROLL;
         this.field_3484.field_3674 = this.field_3484.field_3674 - ARM_ROLL;
         this.rightSecondArm.method_17138(this.field_3401);
         this.leftSecondArm.method_17138(this.field_27433);
         this.rightSecondSleeve.method_17138(this.field_3486);
         this.leftSecondSleeve.method_17138(this.field_3484);
         this.rightSecondArm.field_3674 = this.rightSecondArm.field_3674 - SECOND_ARM_ROLL;
         this.rightSecondSleeve.field_3674 = this.rightSecondSleeve.field_3674 - SECOND_ARM_ROLL;
         this.leftSecondArm.field_3674 = this.leftSecondArm.field_3674 + SECOND_ARM_ROLL;
         this.leftSecondSleeve.field_3674 = this.leftSecondSleeve.field_3674 + SECOND_ARM_ROLL;
         this.rightSecondArm.field_3656 += 3.0F;
         this.leftSecondArm.field_3656 += 3.0F;
         this.rightSecondSleeve.field_3656 += 3.0F;
         this.leftSecondSleeve.field_3656 += 3.0F;
      }
   }

   @Override
   public void method_2805(boolean visible) {
      super.method_2805(visible);

      for (int i = 0; i < this.extraParts.size(); i++) {
         ((class_630)this.extraParts.get(i)).field_3665 = visible;
      }
   }

   @Override
   public void collectExtraParts(String normalized, List<class_630> parts) {
      switch (normalized) {
         case "rightsecondarm":
         case "secondrightarm":
            parts.add(this.rightSecondArm);
            parts.add(this.rightSecondSleeve);
            break;
         case "leftsecondarm":
         case "secondleftarm":
            parts.add(this.leftSecondArm);
            parts.add(this.leftSecondSleeve);
            break;
         case "rightsecondsleeve":
         case "secondrightsleeve":
            parts.add(this.rightSecondSleeve);
            break;
         case "leftsecondsleeve":
         case "secondleftsleeve":
            parts.add(this.leftSecondSleeve);
            break;
         case "arms":
         case "upper":
            parts.add(this.rightSecondArm);
            parts.add(this.rightSecondSleeve);
            parts.add(this.leftSecondArm);
            parts.add(this.leftSecondSleeve);
      }
   }

   @Override
   public void collectExtraParts(List<class_630> parts) {
      parts.addAll(this.extraParts);
   }
}
