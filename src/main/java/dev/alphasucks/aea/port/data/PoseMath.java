package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.data.ModelPartTransformation.Type;

public final class PoseMath {
   public static final int STRIDE = 9;
   public static final int X = 0;
   public static final int Y = 1;
   public static final int Z = 2;
   public static final int X_ROT = 3;
   public static final int Y_ROT = 4;
   public static final int Z_ROT = 5;
   public static final int X_SCALE = 6;
   public static final int Y_SCALE = 7;
   public static final int Z_SCALE = 8;
   public static final int AXIS_X = 0;
   public static final int AXIS_Y = 1;
   public static final int AXIS_Z = 2;
   private static final double GIMBAL = 0.9999999;
   private static final double MIN_SCALE = 1.0E-4;

   private PoseMath() {
   }

   public static void applyGroup(Type type, float[] pose, int at, float value, float weight, boolean override, float pivotX, float pivotY, float pivotZ) {
      switch (type) {
         case PITCH:
            rotateGroup(pose, at, 0, value, weight, override, pivotX, pivotY, pivotZ);
            break;
         case YAW:
            rotateGroup(pose, at, 1, value, weight, override, pivotX, pivotY, pivotZ);
            break;
         case ROLL:
            rotateGroup(pose, at, 2, value, weight, override, pivotX, pivotY, pivotZ);
            break;
         case X_SCALE:
            scaleAbout(pose, at, pivotX, pivotY, pivotZ, 0, 1.0F + value * weight);
            break;
         case Y_SCALE:
            scaleAbout(pose, at, pivotX, pivotY, pivotZ, 1, 1.0F + value * weight);
            break;
         case Z_SCALE:
            scaleAbout(pose, at, pivotX, pivotY, pivotZ, 2, 1.0F + value * weight);
            break;
         case PIVOT_X:
            pose[at + 0] = pose[at + 0] + value * weight;
            break;
         case PIVOT_Y:
            pose[at + 1] = pose[at + 1] + value * weight;
            break;
         case PIVOT_Z:
            pose[at + 2] = pose[at + 2] + value * weight;
      }
   }

   public static boolean isSpatial(Type type) {
      return type != Type.VISIBLE && type != Type.HIDDEN;
   }

   public static void reach(float[] pose, int at, float endX, float endY, float endZ, float targetX, float targetY, float targetZ, float blend) {
      if (!(blend <= 0.0F) && !(Math.abs(endY) < 1.0E-4)) {
         double dx = targetX - pose[at + 0];
         double dy = targetY - pose[at + 1];
         double dz = targetZ - pose[at + 2];
         double sideX = endX * pose[at + 6];
         double sideZ = endZ * pose[at + 8];
         double along = Math.sqrt(Math.max(dx * dx + dy * dy + dz * dz - sideX * sideX - sideZ * sideZ, 0.0));
         double radius = Math.sqrt(along * along + sideZ * sideZ);
         if (!(radius < 1.0E-4)) {
            if (endY < 0.0F) {
               along = -along;
            }

            double phase = Math.atan2(sideZ, along);
            double swing = Math.acos(Math.max(-1.0, Math.min(1.0, dy / radius)));
            double heading = Math.atan2(dx, dz);
            double pitch = 0.0;
            double yaw = 0.0;
            boolean found = false;

            for (int candidate = 0; candidate < 2; candidate++) {
               double tryPitch = candidate == 0 ? swing - phase : -swing - phase;
               double localZ = along * Math.sin(tryPitch) + sideZ * Math.cos(tryPitch);
               double tryYaw = wrap(heading - Math.atan2(sideX, localZ));
               if (!found || Math.abs(tryYaw) < Math.abs(yaw)) {
                  pitch = wrap(tryPitch);
                  yaw = tryYaw;
                  found = true;
               }
            }

            float yScale = (float)Math.max(Math.abs(along) / Math.abs(endY), 1.0E-4);
            pose[at + 3] = pose[at + 3] + (float)wrap(pitch - pose[at + 3]) * blend;
            pose[at + 4] = pose[at + 4] + (float)wrap(yaw - pose[at + 4]) * blend;
            pose[at + 5] = pose[at + 5] - pose[at + 5] * blend;
            pose[at + 7] = pose[at + 7] + (yScale - pose[at + 7]) * blend;
         }
      }
   }

   private static double wrap(double angle) {
      double wrapped = angle % (Math.PI * 2);
      if (wrapped > Math.PI) {
         wrapped -= Math.PI * 2;
      } else if (wrapped < -Math.PI) {
         wrapped += Math.PI * 2;
      }

      return wrapped;
   }

   private static void rotateGroup(float[] pose, int at, int axis, float value, float weight, boolean override, float pivotX, float pivotY, float pivotZ) {
      if (override) {
         int channel = at + 3 + axis;
         pose[channel] -= pose[channel] * weight;
      }

      rotateAbout(pose, at, pivotX, pivotY, pivotZ, axis, value * weight);
   }

   public static void rotateAbout(float[] pose, int at, float pivotX, float pivotY, float pivotZ, int axis, float angle) {
      if (angle != 0.0F) {
         double cos = Math.cos(angle);
         double sin = Math.sin(angle);
         double dx = pose[at + 0] - pivotX;
         double dy = pose[at + 1] - pivotY;
         double dz = pose[at + 2] - pivotZ;
         switch (axis) {
            case 0:
               double ny = dy * cos - dz * sin;
               dz = dy * sin + dz * cos;
               dy = ny;
               break;
            case 1: {
               double nx = dx * cos + dz * sin;
               dz = -dx * sin + dz * cos;
               dx = nx;
               break;
            }
            default: {
               double nx = dx * cos - dy * sin;
               dy = dx * sin + dy * cos;
               dx = nx;
            }
         }

         pose[at + 0] = (float)(pivotX + dx);
         pose[at + 1] = (float)(pivotY + dy);
         pose[at + 2] = (float)(pivotZ + dz);
         turn(pose, at, axis, angle, cos, sin);
      }
   }

   private static void turn(float[] pose, int at, int axis, float angle, double cos, double sin) {
      float xRot = pose[at + 3];
      float yRot = pose[at + 4];
      float zRot = pose[at + 5];
      if (axis == 2) {
         pose[at + 5] = zRot + angle;
      } else if (axis == 1 && zRot == 0.0F) {
         pose[at + 4] = yRot + angle;
      } else if (axis == 0 && yRot == 0.0F && zRot == 0.0F) {
         pose[at + 3] = xRot + angle;
      } else {
         double ca = Math.cos(xRot);
         double sa = Math.sin(xRot);
         double cb = Math.cos(yRot);
         double sb = Math.sin(yRot);
         double cg = Math.cos(zRot);
         double sg = Math.sin(zRot);
         double m00 = cg * cb;
         double m01 = cg * sb * sa - sg * ca;
         double m02 = cg * sb * ca + sg * sa;
         double m10 = sg * cb;
         double m11 = sg * sb * sa + cg * ca;
         double m12 = sg * sb * ca - cg * sa;
         double m20 = -sb;
         double m21 = cb * sa;
         double m22 = cb * ca;
         double r00;
         double r01;
         double r10;
         double r11;
         double r20;
         double r21;
         double r22;
         if (axis == 0) {
            r00 = m00;
            r01 = m01;
            r10 = cos * m10 - sin * m20;
            r11 = cos * m11 - sin * m21;
            r20 = sin * m10 + cos * m20;
            r21 = sin * m11 + cos * m21;
            r22 = sin * m12 + cos * m22;
         } else {
            r00 = cos * m00 + sin * m20;
            r01 = cos * m01 + sin * m21;
            r10 = m10;
            r11 = m11;
            r20 = -sin * m00 + cos * m20;
            r21 = -sin * m01 + cos * m21;
            r22 = -sin * m02 + cos * m22;
         }

         euler(r00, r01, r10, r11, r20, r21, r22, pose, at);
      }
   }

   public static void rotation(float xRot, float yRot, float zRot, double[] out, int at) {
      double ca = Math.cos(xRot);
      double sa = Math.sin(xRot);
      double cb = Math.cos(yRot);
      double sb = Math.sin(yRot);
      double cg = Math.cos(zRot);
      double sg = Math.sin(zRot);
      out[at] = cg * cb;
      out[at + 1] = cg * sb * sa - sg * ca;
      out[at + 2] = cg * sb * ca + sg * sa;
      out[at + 3] = sg * cb;
      out[at + 4] = sg * sb * sa + cg * ca;
      out[at + 5] = sg * sb * ca - cg * sa;
      out[at + 6] = -sb;
      out[at + 7] = cb * sa;
      out[at + 8] = cb * ca;
   }

   public static void euler(double[] m, int from, float[] pose, int at) {
      euler(m[from], m[from + 1], m[from + 3], m[from + 4], m[from + 6], m[from + 7], m[from + 8], pose, at);
   }

   private static void euler(double r00, double r01, double r10, double r11, double r20, double r21, double r22, float[] pose, int at) {
      double sinY = -r20;
      if (sinY >= 0.9999999) {
         pose[at + 3] = (float)Math.atan2(r01, r11);
         pose[at + 4] = (float) (Math.PI / 2);
         pose[at + 5] = 0.0F;
      } else if (sinY <= -0.9999999) {
         pose[at + 3] = (float)Math.atan2(-r01, r11);
         pose[at + 4] = (float) (-Math.PI / 2);
         pose[at + 5] = 0.0F;
      } else {
         pose[at + 3] = (float)Math.atan2(r21, r22);
         pose[at + 4] = (float)Math.asin(sinY);
         pose[at + 5] = (float)Math.atan2(r10, r00);
      }
   }

   public static void scaleAbout(float[] pose, int at, float pivotX, float pivotY, float pivotZ, int axis, float factor) {
      switch (axis) {
         case 0:
            pose[at + 0] = pivotX + (pose[at + 0] - pivotX) * factor;
            pose[at + 6] = pose[at + 6] * factor;
            break;
         case 1:
            pose[at + 1] = pivotY + (pose[at + 1] - pivotY) * factor;
            pose[at + 7] = pose[at + 7] * factor;
            break;
         default:
            pose[at + 2] = pivotZ + (pose[at + 2] - pivotZ) * factor;
            pose[at + 8] = pose[at + 8] * factor;
      }
   }

   public static void localToParent(float[] pose, int at, double lx, double ly, double lz, double[] out) {
      double x = lx * pose[at + 6];
      double y = ly * pose[at + 7];
      double z = lz * pose[at + 8];
      rotateForward(pose, at, x, y, z, out);
      out[0] += pose[at + 0];
      out[1] += pose[at + 1];
      out[2] += pose[at + 2];
   }

   public static void parentToLocal(float[] pose, int at, double px, double py, double pz, double[] out) {
      rotateBackward(pose, at, px - pose[at + 0], py - pose[at + 1], pz - pose[at + 2], out);
      out[0] /= safeScale(pose[at + 6]);
      out[1] /= safeScale(pose[at + 7]);
      out[2] /= safeScale(pose[at + 8]);
   }

   public static void directionToLocal(float[] pose, int at, double dx, double dy, double dz, double[] out) {
      rotateBackward(pose, at, dx, dy, dz, out);
      out[0] /= safeScale(pose[at + 6]);
      out[1] /= safeScale(pose[at + 7]);
      out[2] /= safeScale(pose[at + 8]);
   }

   private static double safeScale(float scale) {
      return Math.abs(scale) < 1.0E-4 ? (scale < 0.0F ? -1.0E-4 : 1.0E-4) : scale;
   }

   private static void rotateForward(float[] pose, int at, double x, double y, double z, double[] out) {
      float xRot = pose[at + 3];
      float yRot = pose[at + 4];
      float zRot = pose[at + 5];
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

      out[0] = x;
      out[1] = y;
      out[2] = z;
   }

   private static void rotateBackward(float[] pose, int at, double x, double y, double z, double[] out) {
      float xRot = pose[at + 3];
      float yRot = pose[at + 4];
      float zRot = pose[at + 5];
      if (zRot != 0.0F) {
         double cos = Math.cos(zRot);
         double sin = Math.sin(zRot);
         double nx = x * cos + y * sin;
         y = -x * sin + y * cos;
         x = nx;
      }

      if (yRot != 0.0F) {
         double cos = Math.cos(yRot);
         double sin = Math.sin(yRot);
         double nx = x * cos - z * sin;
         z = x * sin + z * cos;
         x = nx;
      }

      if (xRot != 0.0F) {
         double cos = Math.cos(xRot);
         double sin = Math.sin(xRot);
         double ny = y * cos + z * sin;
         z = -y * sin + z * cos;
         y = ny;
      }

      out[0] = x;
      out[1] = y;
      out[2] = z;
   }
}
