package dev.alphasucks.aea.port.data;

import net.minecraft.class_1297;
import net.minecraft.class_243;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public final class ModelPartAnchor {
   private ModelPartAnchor() {
   }

   public static ModelPartAnchor.Frame frameOf(class_1297 entity, BodyPart part) {
      BodyPart sided = part.sided(entity);
      BodyFrame body = BodyFrame.of(entity);
      return !sided.hasPoint() ? centre(entity, body) : frameOf(sided, HumanoidPose.of(entity), body);
   }

   public static ModelPartAnchor.Frame frameOf(class_1297 entity, BodyPart part, ModelPartTimeline timeline, double now, float partialTick) {
      BodyPart sided = part.sided(entity);
      BodyFrame body = BodyFrame.of(entity, partialTick);
      return !sided.hasPoint() ? centre(entity, body) : frameOf(sided, HumanoidPose.of(entity, timeline, now, partialTick), body);
   }

   private static ModelPartAnchor.Frame centre(class_1297 entity, BodyFrame body) {
      return new ModelPartAnchor.Frame(new class_243(0.0, entity.method_17682() * 0.5, 0.0), 0.0F, 0.0F, 0.0F, body);
   }

   private static ModelPartAnchor.Frame frameOf(BodyPart sided, HumanoidPose pose, BodyFrame body) {
      float[] point = new float[3];
      double[] scratch = new double[3];
      sided.pointInto(pose.poses(), scratch, point);
      body.modelToWorld(point[0], point[1], point[2], scratch);
      class_243 pivot = new class_243(scratch[0], scratch[1], scratch[2]);
      int limb = HumanoidPose.singleLimb(sided);
      return limb >= 0 && !sided.isGroup()
         ? new ModelPartAnchor.Frame(pivot, pose.xRot(limb), pose.yRot(limb), pose.zRot(limb), body)
         : new ModelPartAnchor.Frame(pivot, 0.0F, 0.0F, 0.0F, body);
   }

   public static class_243 offsetOf(class_1297 entity, BodyPart part) {
      return frameOf(entity, part).pivot();
   }

   static class_243 rotate(class_243 v, float xRot, float yRot, float zRot) {
      double x = v.field_1352;
      double y = v.field_1351;
      double z = v.field_1350;
      if (xRot != 0.0F) {
         double cos = Math.cos(xRot);
         double sin = Math.sin(xRot);
         double ny = y * cos - z * sin;
         z = y * sin + z * cos;
         y = ny;
      }

      if (yRot != 0.0F) {
         double cos = Math.cos(yRot);
         double sin = Math.sin(yRot);
         double nx = x * cos + z * sin;
         z = -x * sin + z * cos;
         x = nx;
      }

      if (zRot != 0.0F) {
         double cos = Math.cos(zRot);
         double sin = Math.sin(zRot);
         double nx = x * cos - y * sin;
         y = x * sin + y * cos;
         x = nx;
      }

      return new class_243(x, y, z);
   }

   public static final class Frame {
      private final class_243 pivot;
      private final float partXRot;
      private final float partYRot;
      private final float partZRot;
      @Nullable
      private final BodyFrame body;
      @Nullable
      private final Matrix3f basis;

      Frame(class_243 pivot, float partXRot, float partYRot, float partZRot, BodyFrame body) {
         this(pivot, partXRot, partYRot, partZRot, body, null);
      }

      private Frame(class_243 pivot, float partXRot, float partYRot, float partZRot, @Nullable BodyFrame body, @Nullable Matrix3f basis) {
         this.pivot = pivot;
         this.partXRot = partXRot;
         this.partYRot = partYRot;
         this.partZRot = partZRot;
         this.body = body;
         this.basis = basis;
      }

      public static ModelPartAnchor.Frame world(class_243 pivot, float partXRot, float partYRot, float partZRot, Matrix3f basis) {
         return new ModelPartAnchor.Frame(pivot, partXRot, partYRot, partZRot, null, basis);
      }

      public class_243 pivot() {
         return this.pivot;
      }

      public class_243 direction(class_243 local) {
         if (local.method_1027() < 1.0E-12) {
            return class_243.field_1353;
         } else {
            class_243 model = ModelPartAnchor.rotate(
               new class_243(local.field_1352, -local.field_1351, -local.field_1350), this.partXRot, this.partYRot, this.partZRot
            );
            if (this.basis != null) {
               Vector3f world = this.basis.transform((float)model.field_1352, (float)model.field_1351, (float)model.field_1350, new Vector3f());
               return new class_243(world.x, world.y, world.z);
            } else {
               return this.body.localDirectionToWorld(-model.field_1352, -model.field_1351, model.field_1350);
            }
         }
      }
   }
}
