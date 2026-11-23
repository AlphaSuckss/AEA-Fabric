package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.client.render.LevelRenderPass;
import dev.alphasucks.aea.port.client.render.ModelPartLookup;
import dev.alphasucks.aea.port.data.BodyPart;
import dev.alphasucks.aea.port.data.HumanoidPose;
import dev.alphasucks.aea.port.data.ModelPartAnchor.Frame;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_156;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_897;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class RenderedPoses {
   private static final long WATCH_MILLIS = 5000L;
   private static final int MAX_AGE_FRAMES = 3;
   private static final int MAX_DEPTH = 4;
   private static final float PIXEL = 0.0625F;
   private static final Int2LongOpenHashMap WATCHED = new Int2LongOpenHashMap();
   private static final Int2ObjectOpenHashMap<RenderedPoses.Pose> POSES = new Int2ObjectOpenHashMap();
   private static final Matrix4f[] ROOTS = new Matrix4f[4];
   private static final class_1297[] ROOT_ENTITIES = new class_1297[4];
   private static int depth;
   private static int frame;

   private RenderedPoses() {
   }

   public static void nextFrame() {
      frame++;
      depth = 0;
      if ((frame & 0xFF) == 0) {
         expire();
      }
   }

   public static void watch(class_1297 entity) {
      WATCHED.put(entity.method_5628(), class_156.method_658() + 5000L);
   }

   @Nullable
   public static RenderedPoses.Pose recent(class_1297 entity) {
      if (POSES.isEmpty()) {
         return null;
      }

      RenderedPoses.Pose pose = (RenderedPoses.Pose)POSES.get(entity.method_5628());
      return pose != null && frame - pose.frame <= 3 ? pose : null;
   }

   public static void begin(class_1297 entity, class_4587 poseStack) {
      if (depth < 4) {
         boolean wanted = !WATCHED.isEmpty() && LevelRenderPass.active() && WATCHED.containsKey(entity.method_5628());
         ROOT_ENTITIES[depth] = wanted ? entity : null;
         if (wanted) {
            ROOTS[depth].set(poseStack.method_23760().method_23761());
         }
      }

      depth++;
   }

   public static void capture(class_897 renderer, class_1297 entity, class_4587 poseStack, class_583<?> model, float partialTick) {
      int index = depth - 1;
      if (index >= 0 && index < 4 && ROOT_ENTITIES[index] == entity) {
         if (model instanceof class_572<?> humanoid) {
            RenderedPoses.Pose pose = (RenderedPoses.Pose)POSES.get(entity.method_5628());
            if (pose == null) {
               pose = new RenderedPoses.Pose();
               POSES.put(entity.method_5628(), pose);
            }

            pose.local.set(ROOTS[index]).invertAffine().mul(poseStack.method_23760().method_23761());
            ModelPartLookup.limbPosesInto(humanoid, pose.limbs);
            class_243 offset = renderer.method_23169(entity, partialTick);
            pose.x = class_3532.method_16436(partialTick, entity.field_6038, entity.method_23317()) + offset.field_1352;
            pose.y = class_3532.method_16436(partialTick, entity.field_5971, entity.method_23318()) + offset.field_1351;
            pose.z = class_3532.method_16436(partialTick, entity.field_5989, entity.method_23321()) + offset.field_1350;
            pose.frame = frame;
         }
      }
   }

   public static void end() {
      if (depth > 0) {
         depth--;
      }

      if (depth < 4) {
         ROOT_ENTITIES[depth] = null;
      }
   }

   public static void clear() {
      WATCHED.clear();
      POSES.clear();
      depth = 0;
   }

   private static void expire() {
      if (!WATCHED.isEmpty() || !POSES.isEmpty()) {
         long now = class_156.method_658();
         ObjectIterator<Entry> it = WATCHED.int2LongEntrySet().fastIterator();

         while (it.hasNext()) {
            if (((Entry)it.next()).getLongValue() < now) {
               it.remove();
            }
         }

         ObjectIterator<it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<RenderedPoses.Pose>> posesIt = POSES.int2ObjectEntrySet().fastIterator();

         while (posesIt.hasNext()) {
            if (!WATCHED.containsKey(((it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry)posesIt.next()).getIntKey())) {
               posesIt.remove();
            }
         }
      }
   }

   static {
      for (int i = 0; i < 4; i++) {
         ROOTS[i] = new Matrix4f();
      }
   }

   public static final class Pose {
      private final Matrix4f local = new Matrix4f();
      private final float[] limbs = new float[54];
      private double x;
      private double y;
      private double z;
      private int frame;

      @Nullable
      public Frame frame(BodyPart sided, class_243 base) {
         if (!sided.hasPoint()) {
            return null;
         }

         Matrix3f basis = new Matrix3f(this.local);
         Vector3f scale = basis.getScale(new Vector3f());
         if (!(scale.x < 1.0E-6F) && !(scale.y < 1.0E-6F) && !(scale.z < 1.0E-6F)) {
            basis.scale(1.0F / scale.x, 1.0F / scale.y, 1.0F / scale.z);
            float[] point = new float[3];
            sided.pointInto(this.limbs, new double[3], point);
            Vector3f at = this.local.transformPosition(point[0] * 0.0625F, point[1] * 0.0625F, point[2] * 0.0625F, new Vector3f());
            class_243 pivot = new class_243(this.x + at.x - base.field_1352, this.y + at.y - base.field_1351, this.z + at.z - base.field_1350);
            int limb = HumanoidPose.singleLimb(sided);
            if (limb >= 0 && !sided.isGroup()) {
               int offset = limb * 9;
               return Frame.world(pivot, this.limbs[offset + 3], this.limbs[offset + 4], this.limbs[offset + 5], basis);
            } else {
               return Frame.world(pivot, 0.0F, 0.0F, 0.0F, basis);
            }
         } else {
            return null;
         }
      }
   }
}
