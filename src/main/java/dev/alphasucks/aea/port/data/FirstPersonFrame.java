package dev.alphasucks.aea.port.data;

import net.minecraft.class_3532;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class FirstPersonFrame {
   public static final int MATRIX_SIZE = 18;
   private static final float PIXEL = 0.0625F;
   private static final float SHOULDER_X = -5.0F;
   private static final float SHOULDER_Y = 2.0F;
   private static final int TRANSLATION = 9;
   private static final int PRODUCT = 9;
   private static final double[] RIGHT_TO_BODY = toBody(1.0F);
   private static final double[] LEFT_TO_BODY = toBody(-1.0F);

   private FirstPersonFrame() {
   }

   public static Matrix4f arm(Matrix4f frame, float side, float equip, float swing) {
      float root = class_3532.method_15355(swing);
      frame.translate(
         side * (-0.3F * class_3532.method_15374(root * (float) Math.PI) + 0.64000005F),
         0.4F * class_3532.method_15374(root * (float) (Math.PI * 2)) + -0.6F + equip * -0.6F,
         -0.4F * class_3532.method_15374(swing * (float) Math.PI) + -0.71999997F
      );
      frame.rotateY(side * 45.0F * (float) (Math.PI / 180.0));
      frame.rotateY(side * class_3532.method_15374(root * (float) Math.PI) * 70.0F * (float) (Math.PI / 180.0));
      frame.rotateZ(side * class_3532.method_15374(swing * swing * (float) Math.PI) * -20.0F * (float) (Math.PI / 180.0));
      frame.translate(side * -1.0F, 3.6F, 3.5F);
      frame.rotateZ(side * 120.0F * (float) (Math.PI / 180.0));
      frame.rotateX((float) (Math.PI * 10.0 / 9.0));
      frame.rotateY(side * -135.0F * (float) (Math.PI / 180.0));
      frame.translate(side * 5.6F, 0.0F, 0.0F);
      return frame;
   }

   public static void reach(
      boolean right,
      float[] pose,
      int at,
      float endX,
      float endY,
      float endZ,
      float targetX,
      float targetY,
      float targetZ,
      float blend,
      float[] body,
      double[] matrix
   ) {
      if (!(blend <= 0.0F)) {
         double[] toBody = right ? RIGHT_TO_BODY : LEFT_TO_BODY;
         double x = pose[at + 0];
         double y = pose[at + 1];
         double z = pose[at + 2];
         body[0] = (float)(toBody[0] * x + toBody[1] * y + toBody[2] * z + toBody[9]);
         body[1] = (float)(toBody[3] * x + toBody[4] * y + toBody[5] * z + toBody[10]);
         body[2] = (float)(toBody[6] * x + toBody[7] * y + toBody[8] * z + toBody[11]);
         body[6] = pose[at + 6];
         body[7] = pose[at + 7];
         body[8] = pose[at + 8];
         PoseMath.rotation(pose[at + 3], pose[at + 4], pose[at + 5], matrix, 0);
         multiply(toBody, false, matrix);
         PoseMath.euler(matrix, 9, body, 0);
         PoseMath.reach(body, 0, endX, endY, endZ, targetX, targetY, targetZ, blend);
         PoseMath.rotation(body[3], body[4], body[5], matrix, 0);
         multiply(toBody, true, matrix);
         PoseMath.euler(matrix, 9, pose, at);
         pose[at + 7] = body[7];
      }
   }

   private static void multiply(double[] toBody, boolean transposed, double[] matrix) {
      for (int row = 0; row < 3; row++) {
         for (int col = 0; col < 3; col++) {
            double sum = 0.0;

            for (int k = 0; k < 3; k++) {
               double left = transposed ? toBody[k * 3 + row] : toBody[row * 3 + k];
               sum += left * matrix[k * 3 + col];
            }

            matrix[9 + row * 3 + col] = sum;
         }
      }
   }

   private static double[] toBody(float side) {
      Vector3f shoulder = arm(new Matrix4f(), 1.0F, 0.0F, 0.0F).transformPosition(-0.3125F, 0.125F, 0.0F, new Vector3f());
      Matrix4f m = new Matrix4f()
         .scale(16.0F)
         .mul(arm(new Matrix4f(), side, 0.0F, 0.0F).invert())
         .translate(0.0F, shoulder.y + 0.125F, shoulder.z)
         .scale(-0.0625F, -0.0625F, 0.0625F)
         .invert();
      return new double[]{m.m00(), m.m10(), m.m20(), m.m01(), m.m11(), m.m21(), m.m02(), m.m12(), m.m22(), m.m30(), m.m31(), m.m32()};
   }
}
