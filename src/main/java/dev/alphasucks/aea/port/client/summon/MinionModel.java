package dev.alphasucks.aea.port.client.summon;

import dev.alphasucks.aea.port.client.render.model.ExtraModelParts;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import java.util.List;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5603;
import net.minecraft.class_5606;
import net.minecraft.class_5607;
import net.minecraft.class_5609;
import net.minecraft.class_5610;
import net.minecraft.class_583;
import net.minecraft.class_630;
import org.jetbrains.annotations.Nullable;

public class MinionModel extends class_583<MinionEntity> implements ExtraModelParts {
   public static final String MAIN = "main";
   public static final String FLAT_2 = "flat2";
   public static final String FLAT_3 = "flat3";
   public static final String[] BONES = new String[]{"main", "flat2", "flat3"};
   private final class_630 main;
   private final class_630 flat2;
   private final class_630 flat3;

   public MinionModel(class_630 root) {
      this.main = root.method_32086("main");
      this.flat2 = this.main.method_32086("flat2");
      this.flat3 = this.main.method_32086("flat3");
   }

   public static class_5607 createBodyLayer() {
      class_5609 mesh = new class_5609();
      class_5610 parts = mesh.method_32111();
      class_5610 bone = parts.method_32117(
         "main",
         class_5606.method_32108()
            .method_32101(24, 0)
            .method_32097(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .method_32101(24, 16)
            .method_32097(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F)
            .method_32101(24, 28)
            .method_32097(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F)
            .method_32101(0, 12)
            .method_32097(0.0F, -6.0F, -6.0F, 0.0F, 12.0F, 12.0F),
         class_5603.method_32090(0.0F, 20.0F, 0.0F)
      );
      bone.method_32117(
         "flat3",
         class_5606.method_32108().method_32101(0, -12).method_32097(0.0F, -6.0F, -6.0F, 0.0F, 12.0F, 12.0F),
         class_5603.method_32091(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.5708F)
      );
      bone.method_32117(
         "flat2",
         class_5606.method_32108().method_32101(0, 0).method_32097(0.0F, -10.0F, -6.0F, 0.0F, 12.0F, 12.0F),
         class_5603.method_32091(0.0F, 4.0F, 0.0F, 0.0F, -1.5708F, 0.0F)
      );
      return class_5607.method_32110(mesh, 64, 64);
   }

   @Nullable
   public class_630 bone(String name) {
      return switch (name) {
         case "main" -> this.main;
         case "flat2" -> this.flat2;
         case "flat3" -> this.flat3;
         default -> null;
      };
   }

   @Override
   public void collectExtraParts(String normalized, List<class_630> parts) {
      class_630 part = this.bone(normalized);
      if (part != null) {
         parts.add(part);
      }
   }

   @Override
   public void collectExtraParts(List<class_630> parts) {
      parts.add(this.main);
   }

   // original carried a javac bridge from the intermediary name
   @Override
   public void method_2819(MinionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
   }

   public void setupAnim(MinionEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
      this.main.field_3675 = netHeadYaw * (float) (Math.PI / 180.0);
      this.main.field_3654 = headPitch * (float) (Math.PI / 180.0);
   }

   public void method_2828(class_4587 pose, class_4588 consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      this.main.method_22699(pose, consumer, light, overlay, red, green, blue, alpha);
   }
}
