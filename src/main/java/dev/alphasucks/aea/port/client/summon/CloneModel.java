package dev.alphasucks.aea.port.client.summon;

import dev.alphasucks.aea.port.entity.summon.CloneEntity;
import net.minecraft.class_1306;
import net.minecraft.class_1764;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_572.class_573;

public class CloneModel extends class_591<CloneEntity> {
   public CloneModel(class_630 root, boolean slim) {
      super(root, slim);
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_17087(CloneEntity clone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.setupAnim(clone, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
   }

   public void setupAnim(CloneEntity clone, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.field_3395 = class_573.field_3409;
      this.field_3399 = class_573.field_3409;
      class_1799 main = clone.method_6047();
      class_1799 off = clone.method_6079();
      class_1306 arm = clone.method_6068();
      if (main.method_31574(class_1802.field_8102) && clone.method_6510()) {
         this.setPose(arm, class_573.field_3403);
      } else if (main.method_7909() instanceof class_1764) {
         this.setPose(arm, clone.isCharging() ? class_573.field_3405 : class_573.field_3408);
      } else {
         if (!main.method_7960()) {
            this.setPose(arm, class_573.field_3410);
         }

         if (!off.method_7960()) {
            this.setPose(arm.method_5928(), class_573.field_3410);
         }
      }

      super.method_17087(clone, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
   }

   private void setPose(class_1306 arm, class_573 pose) {
      if (arm == class_1306.field_6183) {
         this.field_3395 = pose;
      } else {
         this.field_3399 = pose;
      }
   }
}
