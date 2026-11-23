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

public class StinkFlyPlayerModel<T extends class_1309> extends class_591<T> implements ExtraModelParts {
   private static final float ABDOMEN_PITCH = -1.0036F;
   private static final float THORAX_PITCH = 0.4363F;
   private static final float SECOND_LEG_PITCH = -0.3491F;
   private static final float ARM_PITCH = -0.5236F;
   public final class_630 rightSecondLeg;
   public final class_630 rightSecondPants;
   public final class_630 leftSecondLeg;
   public final class_630 leftSecondPants;
   private final List<class_630> extraParts;
   private final List<class_630> bodyParts;

   public StinkFlyPlayerModel(class_630 root, boolean slim) {
      super(root, slim);
      this.rightSecondLeg = root.method_32086("right_second_leg");
      this.rightSecondPants = root.method_32086("right_second_pants");
      this.leftSecondLeg = root.method_32086("left_second_leg");
      this.leftSecondPants = root.method_32086("left_second_pants");
      this.extraParts = List.of(this.rightSecondLeg, this.rightSecondPants, this.leftSecondLeg, this.leftSecondPants);
      List<class_630> parts = new ArrayList();
      super.method_22948().forEach(parts::add);
      parts.addAll(this.extraParts);
      this.bodyParts = List.copyOf(parts);
   }

   public static class_5607 createLayer(boolean slim) {
      class_5609 mesh = class_591.method_32028(class_5605.field_27715, slim);
      class_5610 root = mesh.method_32111();
      class_5605 none = class_5605.field_27715;
      class_5605 overlay = new class_5605(0.25F);
      root.method_32117(
         "head",
         class_5606.method_32108().method_32101(0, 0).method_32098(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, none),
         class_5603.method_32090(0.0F, 5.0F, -2.0F)
      );
      root.method_32117(
         "hat",
         class_5606.method_32108().method_32101(32, 0).method_32098(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new class_5605(0.5F)),
         class_5603.method_32090(0.0F, 5.0F, -2.0F)
      );
      class_5610 body = root.method_32117("body", class_5606.method_32108(), class_5603.method_32090(0.0F, 12.0F, 5.5F));
      body.method_32117(
         "lower_r1",
         class_5606.method_32108().method_32101(16, 16).method_32098(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, none),
         class_5603.method_32091(0.0F, 1.0F, 4.5F, -1.0036F, 0.0F, 0.0F)
      );
      body.method_32117(
         "upper_r1",
         class_5606.method_32108().method_32101(16, 16).method_32098(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, none),
         class_5603.method_32091(0.0F, -1.0F, -4.5F, 0.4363F, 0.0F, 0.0F)
      );
      class_5610 jacket = root.method_32117("jacket", class_5606.method_32108(), class_5603.method_32090(0.0F, 12.0F, 5.5F));
      jacket.method_32117(
         "lower_r2",
         class_5606.method_32108().method_32101(16, 32).method_32098(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, overlay),
         class_5603.method_32091(0.0F, 1.0F, 4.5F, -1.0036F, 0.0F, 0.0F)
      );
      jacket.method_32117(
         "upper_r2",
         class_5606.method_32108().method_32101(16, 32).method_32098(-4.0F, -6.0F, -2.0F, 8.0F, 12.0F, 4.0F, overlay),
         class_5603.method_32091(0.0F, -1.0F, -4.5F, 0.4363F, 0.0F, 0.0F)
      );
      root.method_32117(
         "right_leg",
         class_5606.method_32108().method_32101(0, 16).method_32098(-4.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, none),
         class_5603.method_32090(-4.0F, 12.0F, 3.0F)
      );
      root.method_32117(
         "right_pants",
         class_5606.method_32108().method_32101(0, 32).method_32098(-4.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, overlay),
         class_5603.method_32090(-4.0F, 12.0F, 3.0F)
      );
      root.method_32117(
         "right_second_leg",
         class_5606.method_32108().method_32101(0, 16).method_32098(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, none),
         class_5603.method_32091(-4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      root.method_32117(
         "right_second_pants",
         class_5606.method_32108().method_32101(0, 32).method_32098(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, overlay),
         class_5603.method_32091(-4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      root.method_32117(
         "left_leg",
         class_5606.method_32108().method_32101(16, 48).method_32098(0.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, none),
         class_5603.method_32090(4.0F, 12.0F, 3.0F)
      );
      root.method_32117(
         "left_pants",
         class_5606.method_32108().method_32101(0, 48).method_32098(0.0F, 0.0F, -1.0F, 4.0F, 12.0F, 4.0F, overlay),
         class_5603.method_32090(4.0F, 12.0F, 3.0F)
      );
      root.method_32117(
         "left_second_leg",
         class_5606.method_32108().method_32101(16, 48).method_32098(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, none),
         class_5603.method_32091(4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      root.method_32117(
         "left_second_pants",
         class_5606.method_32108().method_32101(0, 48).method_32098(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, overlay),
         class_5603.method_32091(4.0F, 12.0F, 0.0F, -0.3491F, 0.0F, 0.0F)
      );
      float width = slim ? 3.0F : 4.0F;
      float rightInset = slim ? -3.0F : -4.0F;
      root.method_32117(
         "left_arm",
         class_5606.method_32108().method_32101(32, 48).method_32098(slim ? 0.0F : -1.0F, -2.0F, -2.0F, width, 12.0F, 4.0F, none),
         class_5603.method_32091(4.0F, 6.0F, -1.5F, -0.5236F, 0.0F, 0.0F)
      );
      root.method_32117(
         "left_sleeve",
         class_5606.method_32108().method_32101(48, 48).method_32098(slim ? 0.0F : -1.0F, -2.0F, -2.0F, width, 12.0F, 4.0F, overlay),
         class_5603.method_32091(4.0F, 6.0F, -1.5F, -0.5236F, 0.0F, 0.0F)
      );
      root.method_32117(
         "right_arm",
         class_5606.method_32108().method_32101(40, 16).method_32098(rightInset, -2.0F, -2.0F, width, 12.0F, 4.0F, none),
         class_5603.method_32091(-4.0F, 6.0F, -2.0F, -0.5236F, 0.0F, 0.0F)
      );
      root.method_32117(
         "right_sleeve",
         class_5606.method_32108().method_32101(40, 32).method_32098(rightInset, -2.0F, -2.0F, width, 12.0F, 4.0F, overlay),
         class_5603.method_32091(-4.0F, 6.0F, -2.0F, -0.5236F, 0.0F, 0.0F)
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

         this.field_3398.method_41923();
         super.method_17087(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
         copyRotation(this.field_3392, this.rightSecondLeg);
         copyRotation(this.field_3397, this.leftSecondLeg);
         copyRotation(this.field_3479, this.rightSecondPants);
         copyRotation(this.field_3482, this.leftSecondPants);
         this.rightSecondPants.field_3665 = this.field_3479.field_3665;
         this.leftSecondPants.field_3665 = this.field_3482.field_3665;
         this.field_3391.field_3656 = this.field_3391.field_3656 + (this.field_3400 ? 9.0F : 12.0F);
         this.field_3483.field_3656 = this.field_3483.field_3656 + (this.field_3400 ? 9.0F : 12.0F);
         this.field_3398.field_3656 += 5.0F;
         this.field_3394.field_3656 += 5.0F;
         this.field_3392.field_3655 += 3.0F;
         this.field_3392.field_3656 = this.field_3392.field_3656 - (this.field_3400 ? 2.0F : 0.0F);
         this.field_3479.field_3655 += 3.0F;
         this.field_3479.field_3656 = this.field_3479.field_3656 - (this.field_3400 ? 2.0F : 0.0F);
         this.rightSecondLeg.field_3655 = this.rightSecondLeg.field_3655 + (this.field_3400 ? 4.0F : 0.0F);
         this.rightSecondPants.field_3655 = this.rightSecondPants.field_3655 + (this.field_3400 ? 4.0F : 0.0F);
         this.field_3397.field_3655 += 3.0F;
         this.field_3397.field_3656 = this.field_3397.field_3656 - (this.field_3400 ? 2.0F : 0.0F);
         this.field_3482.field_3655 += 3.0F;
         this.field_3482.field_3656 = this.field_3482.field_3656 - (this.field_3400 ? 2.0F : 0.0F);
         this.leftSecondLeg.field_3655 = this.leftSecondLeg.field_3655 + (this.field_3400 ? 4.0F : 0.0F);
         this.leftSecondPants.field_3655 = this.leftSecondPants.field_3655 + (this.field_3400 ? 4.0F : 0.0F);
         this.field_3401.field_3656 += 4.0F;
         this.field_3401.field_3657++;
         this.field_3486.field_3656 += 4.0F;
         this.field_3486.field_3657++;
         this.field_27433.field_3656 += 4.0F;
         this.field_27433.field_3657--;
         this.field_3484.field_3656 += 4.0F;
         this.field_3484.field_3657--;

         for (int i = 0; i < this.bodyParts.size(); i++) {
            class_630 part = (class_630)this.bodyParts.get(i);
            class_5603 pose = part.method_41921();
            part.field_3654 = part.field_3654 + pose.field_27705;
            part.field_3675 = part.field_3675 + pose.field_27706;
            part.field_3674 = part.field_3674 + pose.field_27707;
         }
      }
   }

   private static void copyRotation(class_630 from, class_630 to) {
      to.field_3654 = from.field_3654;
      to.field_3675 = from.field_3675;
      to.field_3674 = from.field_3674;
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
         case "rightsecondleg":
         case "secondrightleg":
            parts.add(this.rightSecondLeg);
            parts.add(this.rightSecondPants);
            break;
         case "leftsecondleg":
         case "secondleftleg":
            parts.add(this.leftSecondLeg);
            parts.add(this.leftSecondPants);
            break;
         case "rightsecondpants":
         case "secondrightpants":
            parts.add(this.rightSecondPants);
            break;
         case "leftsecondpants":
         case "secondleftpants":
            parts.add(this.leftSecondPants);
            break;
         case "legs":
         case "lower":
            parts.add(this.rightSecondLeg);
            parts.add(this.rightSecondPants);
            parts.add(this.leftSecondLeg);
            parts.add(this.leftSecondPants);
      }
   }

   @Override
   public void collectExtraParts(List<class_630> parts) {
      parts.addAll(this.extraParts);
   }
}
