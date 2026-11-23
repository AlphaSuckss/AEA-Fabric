package dev.alphasucks.aea.port.data;

import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4050;

public final class BodyFrame {
   private static final double MODEL_ORIGIN = 1.501;
   private static final double PLAYER_MODEL_SCALE = 0.9375;
   private static final double PIXEL = 0.0625;
   private static final double DEG_TO_RAD = Math.PI / 180.0;
   private final float bodyYaw;
   private final float bodyPitch;
   private final float bodyRoll;
   private final double modelScale;
   private final double entityScale;
   private final double offsetY;
   private final double offsetZ;
   private final double yawCos;
   private final double yawSin;
   private final double pitchCos;
   private final double pitchSin;
   private final double rollCos;
   private final double rollSin;

   private BodyFrame(float bodyYaw, float bodyPitch, float bodyRoll, double modelScale, double entityScale, double offsetY, double offsetZ) {
      this.bodyYaw = bodyYaw;
      this.bodyPitch = bodyPitch;
      this.bodyRoll = bodyRoll;
      this.modelScale = modelScale;
      this.entityScale = entityScale;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      double yaw = (180.0 - bodyYaw) * (Math.PI / 180.0);
      this.yawCos = Math.cos(yaw);
      this.yawSin = Math.sin(yaw);
      this.pitchCos = Math.cos(bodyPitch);
      this.pitchSin = Math.sin(bodyPitch);
      this.rollCos = Math.cos(bodyRoll);
      this.rollSin = Math.sin(bodyRoll);
   }

   public static BodyFrame of(class_1297 entity) {
      return of(entity, 1.0F, 0.0F);
   }

   public static BodyFrame of(class_1297 entity, float partialTick) {
      return of(entity, partialTick, partialTick);
   }

   private static BodyFrame of(class_1297 entity, float partialTick, float tickOffset) {
      float bodyYaw = entity instanceof class_1309 living
         ? class_3532.method_17821(partialTick, living.field_6220, living.field_6283)
         : class_3532.method_17821(partialTick, entity.field_5982, entity.method_36454());
      float bodyPitch = 0.0F;
      float bodyRoll = 0.0F;
      double offsetY = 0.0;
      double offsetZ = 0.0;
      double modelScale = entity instanceof class_1657 ? 0.9375 : 1.0;
      if (entity instanceof class_1657 player) {
         float swimAmount = player.method_6024(partialTick);
         float xRot = player.method_5695(partialTick);
         class_4050 forced = EntityPoses.forced(player);
         boolean forcedGlide = !player.method_6128() && forced == class_4050.field_18077;
         if (!player.method_6128() && !forcedGlide) {
            if (swimAmount > 0.0F) {
               float target = player.method_5799() ? -90.0F - xRot : -90.0F;
               bodyPitch = class_3532.method_16439(swimAmount, 0.0F, target) * (float) (Math.PI / 180.0);
               if (player.method_20232()) {
                  offsetY = -1.0;
                  offsetZ = 0.3;
               }
            } else if (forced != null && EntityPoses.movesBody(forced)) {
               float progress = EntityPoses.progress(player, tickOffset);
               bodyPitch = EntityPoses.bodyPitch(forced) * progress * (float) (Math.PI / 180.0);
               offsetY = -EntityPoses.bodyDrop(forced) * progress * 0.0625 * modelScale;
            }
         } else {
            float ticks = (forcedGlide ? EntityPoses.forcedTicks(player) : player.method_6003()) + tickOffset;
            bodyPitch = class_3532.method_15363(ticks * ticks / 100.0F, 0.0F, 1.0F) * (-90.0F - xRot) * (float) (Math.PI / 180.0);
            bodyRoll = glideRoll(player, partialTick);
         }
      }

      return new BodyFrame(bodyYaw, bodyPitch, bodyRoll, modelScale, entityScale(entity), offsetY, offsetZ);
   }

   private static double entityScale(class_1297 entity) {
      return 1.0;
   }

   public float bodyYaw() {
      return this.bodyYaw;
   }

   public void modelToWorld(double x, double y, double z, double[] out) {
      double lx = -x * 0.0625 * this.modelScale;
      double ly = (1.501 - y * 0.0625) * this.modelScale;
      double lz = z * 0.0625 * this.modelScale + this.offsetZ;
      ly += this.offsetY;
      this.toWorld(lx, ly, lz, out);
      out[0] *= this.entityScale;
      out[1] *= this.entityScale;
      out[2] *= this.entityScale;
   }

   public void worldToModel(double wx, double wy, double wz, double[] out) {
      double scale = this.entityScale == 0.0 ? 1.0 : this.entityScale;
      this.fromWorld(wx / scale, wy / scale, wz / scale, out);
      double lx = out[0];
      double ly = out[1] - this.offsetY;
      double lz = out[2] - this.offsetZ;
      out[0] = -lx / (0.0625 * this.modelScale);
      out[1] = (1.501 - ly / this.modelScale) / 0.0625;
      out[2] = lz / (0.0625 * this.modelScale);
   }

   public void worldDirectionToModel(double dx, double dy, double dz, double[] out) {
      double scale = this.entityScale == 0.0 ? 1.0 : this.entityScale;
      this.fromWorld(dx / scale, dy / scale, dz / scale, out);
      double factor = 0.0625 * this.modelScale;
      out[0] = -out[0] / factor;
      out[1] = -out[1] / factor;
      out[2] /= factor;
   }

   public class_243 localDirectionToWorld(double x, double y, double z) {
      double[] out = new double[3];
      this.toWorld(x, y, z, out);
      return new class_243(out[0], out[1], out[2]);
   }

   private void toWorld(double x, double y, double z, double[] out) {
      if (this.bodyRoll != 0.0F) {
         double nx = x * this.rollCos + z * this.rollSin;
         z = -x * this.rollSin + z * this.rollCos;
         x = nx;
      }

      if (this.bodyPitch != 0.0F) {
         double ny = y * this.pitchCos - z * this.pitchSin;
         z = y * this.pitchSin + z * this.pitchCos;
         y = ny;
      }

      double nx = x * this.yawCos + z * this.yawSin;
      z = -x * this.yawSin + z * this.yawCos;
      out[0] = nx;
      out[1] = y;
      out[2] = z;
   }

   private void fromWorld(double x, double y, double z, double[] out) {
      double nx = x * this.yawCos - z * this.yawSin;
      z = x * this.yawSin + z * this.yawCos;
      x = nx;
      if (this.bodyPitch != 0.0F) {
         double ny = y * this.pitchCos + z * this.pitchSin;
         z = -y * this.pitchSin + z * this.pitchCos;
         y = ny;
      }

      if (this.bodyRoll != 0.0F) {
         double rx = x * this.rollCos - z * this.rollSin;
         z = x * this.rollSin + z * this.rollCos;
         x = rx;
      }

      out[0] = x;
      out[1] = y;
      out[2] = z;
   }

   private static float glideRoll(class_1657 player, float partialTick) {
      class_243 view = player.method_5828(partialTick);
      class_243 motion = player.method_18798();
      double motionSqr = motion.method_37268();
      double viewSqr = view.method_37268();
      if (!(motionSqr <= 0.0) && !(viewSqr <= 0.0)) {
         double dot = (motion.field_1352 * view.field_1352 + motion.field_1350 * view.field_1350) / Math.sqrt(motionSqr * viewSqr);
         double cross = motion.field_1352 * view.field_1350 - motion.field_1350 * view.field_1352;
         return (float)(Math.signum(cross) * Math.acos(class_3532.method_15350(dot, -1.0, 1.0)));
      } else {
         return 0.0F;
      }
   }
}
