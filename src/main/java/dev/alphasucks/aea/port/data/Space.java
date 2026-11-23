package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3542;

public enum Space implements class_3542 {
   WORLD("world"),
   LOCAL("local"),
   LOCAL_HORIZONTAL("local_horizontal"),
   LOCAL_HORIZONTAL_NORMALIZED("local_horizontal_normalized"),
   VELOCITY("velocity"),
   VELOCITY_NORMALIZED("velocity_normalized"),
   VELOCITY_HORIZONTAL("velocity_horizontal"),
   VELOCITY_HORIZONTAL_NORMALIZED("velocity_horizontal_normalized");

   public static final Codec<Space> CODEC = class_3542.method_28140(Space::values);
   private final String name;

   Space(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public class_243 toGlobal(class_1297 entity, class_243 input) {
      class_243 forward;
      boolean normalize;
      switch (this) {
         case WORLD:
            return input;
         case LOCAL:
            forward = entity.method_5720();
            normalize = false;
            break;
         case LOCAL_HORIZONTAL:
            forward = horizontal(entity.method_5720());
            normalize = false;
            break;
         case LOCAL_HORIZONTAL_NORMALIZED:
            return rotateByYaw(input, entity.method_36454());
         case VELOCITY:
            forward = entity.method_18798();
            normalize = false;
            break;
         case VELOCITY_NORMALIZED:
            forward = entity.method_18798();
            normalize = true;
            break;
         case VELOCITY_HORIZONTAL:
            forward = horizontal(entity.method_18798());
            normalize = false;
            break;
         case VELOCITY_HORIZONTAL_NORMALIZED:
            forward = horizontal(entity.method_18798());
            normalize = true;
            break;
         default:
            return input;
      }

      return transformVectorToBase(forward, input, entity.method_36454(), normalize);
   }

   public class_243 position(class_1297 entity, Vector offset) {
      class_243 origin = entity.method_19538();
      return offset.isZero() ? origin : origin.method_1019(this.toGlobal(entity, new class_243(offset.x(), offset.y(), offset.z())));
   }

   public boolean isLocal() {
      return this == LOCAL || this == LOCAL_HORIZONTAL || this == LOCAL_HORIZONTAL_NORMALIZED;
   }

   public static class_243 rotateByYaw(class_243 input, float yaw) {
      double radians = yaw * (Math.PI / 180.0);
      double sin = Math.sin(radians);
      double cos = Math.cos(radians);
      return new class_243(input.field_1352 * cos - input.field_1350 * sin, input.field_1351, input.field_1352 * sin + input.field_1350 * cos);
   }

   private static class_243 horizontal(class_243 v) {
      return new class_243(v.field_1352, 0.0, v.field_1350);
   }

   public static class_243 transformVectorToBase(class_243 baseForward, class_243 input, float baseYaw, boolean normalize) {
      double baseScale = baseForward.method_1033();
      if (baseScale <= 0.007) {
         return class_243.field_1353;
      }

      class_243 z = baseForward.method_1021(1.0 / baseScale);
      double xX;
      double xZ;
      if (Math.abs(z.field_1351) != 1.0) {
         xX = z.field_1350;
         xZ = -z.field_1352;
         double f = 1.0 / Math.sqrt(xX * xX + xZ * xZ);
         xX *= f;
         xZ *= f;
      } else {
         double trigYaw = -baseYaw * (Math.PI / 180.0);
         xX = Math.cos(trigYaw);
         xZ = -Math.sin(trigYaw);
      }

      double yX = z.field_1351 * xZ;
      double yY = z.field_1350 * xX - z.field_1352 * xZ;
      double yZ = -z.field_1351 * xX;
      double scale = normalize ? 1.0 : baseScale;
      return new class_243(
         (xX * input.field_1352 + yX * input.field_1351 + z.field_1352 * input.field_1350) * scale,
         (yY * input.field_1351 + z.field_1351 * input.field_1350) * scale,
         (xZ * input.field_1352 + yZ * input.field_1351 + z.field_1350 * input.field_1350) * scale
      );
   }
}
