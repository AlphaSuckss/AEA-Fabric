package dev.alphasucks.aea.port.client.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2350;
import net.minecraft.class_4588;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_630.class_628;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PerFaceCube extends class_628 {
   private static final Set<class_2350> NO_POLYGONS = EnumSet.noneOf(class_2350.class);
   private static final float BOX_UV_EPSILON = 1.0E-7F;
   static final class_2350[] ORDER = new class_2350[]{
      class_2350.field_11033, class_2350.field_11036, class_2350.field_11039, class_2350.field_11043, class_2350.field_11034, class_2350.field_11035
   };
   private static final int[][] CORNERS = new int[][]{{5, 4, 0, 1}, {2, 3, 7, 6}, {0, 4, 7, 3}, {1, 0, 3, 2}, {5, 1, 2, 6}, {4, 5, 6, 7}};
   private final float[] positions;
   private final float[] uvs;
   private final float[] normals;
   private final int faceCount;

   public PerFaceCube(
      Map<class_2350, float[]> faces,
      float originX,
      float originY,
      float originZ,
      float sizeX,
      float sizeY,
      float sizeZ,
      float grow,
      boolean mirror,
      float texWidth,
      float texHeight
   ) {
      this(ordered(faces), originX, originY, originZ, sizeX, sizeY, sizeZ, grow, mirror, texWidth, texHeight);
   }

   public PerFaceCube(
      float u,
      float v,
      float originX,
      float originY,
      float originZ,
      float sizeX,
      float sizeY,
      float sizeZ,
      float grow,
      boolean mirror,
      float texWidth,
      float texHeight
   ) {
      this(boxUv(u, v, sizeX, sizeY, sizeZ), originX, originY, originZ, sizeX, sizeY, sizeZ, grow, mirror, texWidth, texHeight);
   }

   public static PerFaceCube of(
      float[][] faces,
      float originX,
      float originY,
      float originZ,
      float sizeX,
      float sizeY,
      float sizeZ,
      float grow,
      boolean mirror,
      float texWidth,
      float texHeight
   ) {
      return new PerFaceCube(faces, originX, originY, originZ, sizeX, sizeY, sizeZ, grow, mirror, texWidth, texHeight);
   }

   private PerFaceCube(
      float[][] faces,
      float originX,
      float originY,
      float originZ,
      float sizeX,
      float sizeY,
      float sizeZ,
      float grow,
      boolean mirror,
      float texWidth,
      float texHeight
   ) {
      super(0, 0, originX, originY, originZ, sizeX, sizeY, sizeZ, grow, grow, grow, mirror, texWidth, texHeight, NO_POLYGONS);
      float maxX = originX + sizeX + grow;
      float maxY = originY + sizeY + grow;
      float maxZ = originZ + sizeZ + grow;
      float minX = originX - grow;
      float minY = originY - grow;
      float minZ = originZ - grow;
      if (mirror) {
         float swap = maxX;
         maxX = minX;
         minX = swap;
      }

      float[] corners = new float[]{
         minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ
      };
      int count = 0;

      for (int i = 0; i < faces.length; i++) {
         if (faces[i] != null) {
            count++;
         }
      }

      this.faceCount = count;
      this.positions = new float[count * 12];
      this.uvs = new float[count * 8];
      this.normals = new float[count * 3];
      int slot = 0;

      for (int i = 0; i < ORDER.length; i++) {
         if (faces[i] != null) {
            this.emit(slot++, corners, CORNERS[i], faces[i], ORDER[i], mirror, texWidth, texHeight);
         }
      }
   }

   private static float[][] ordered(Map<class_2350, float[]> faces) {
      float[][] out = new float[ORDER.length][];

      for (int i = 0; i < ORDER.length; i++) {
         out[i] = (float[])faces.get(ORDER[i]);
      }

      return out;
   }

   private static float[][] boxUv(float u, float v, float sizeX, float sizeY, float sizeZ) {
      float sx = (float)Math.floor(sizeX + 1.0E-7F);
      float sy = (float)Math.floor(sizeY + 1.0E-7F);
      float sz = (float)Math.floor(sizeZ + 1.0E-7F);
      return new float[][]{
         {u + sz, v, sx, sz},
         {u + sz + sx, v + sz, sx, -sz},
         {u, v + sz, sz, sy},
         {u + sz, v + sz, sx, sy},
         {u + sz + sx, v + sz, sz, sy},
         {u + sz + sz + sx, v + sz, sx, sy}
      };
   }

   private void emit(int slot, float[] corners, int[] indices, float[] uv, class_2350 face, boolean mirror, float texWidth, float texHeight) {
      float u1 = uv[0] / texWidth;
      float v1 = uv[1] / texHeight;
      float u2 = (uv[0] + uv[2]) / texWidth;
      float v2 = (uv[1] + uv[3]) / texHeight;
      float[] u = new float[]{u2, u1, u1, u2};
      float[] v = new float[]{v1, v1, v2, v2};
      int position = slot * 12;
      int texture = slot * 8;

      for (int i = 0; i < 4; i++) {
         int source = mirror ? 3 - i : i;
         int corner = indices[source] * 3;
         this.positions[position + i * 3] = corners[corner] / 16.0F;
         this.positions[position + i * 3 + 1] = corners[corner + 1] / 16.0F;
         this.positions[position + i * 3 + 2] = corners[corner + 2] / 16.0F;
         this.uvs[texture + i * 2] = u[source];
         this.uvs[texture + i * 2 + 1] = v[source];
      }

      int normal = slot * 3;
      this.normals[normal] = mirror ? -face.method_10148() : face.method_10148();
      this.normals[normal + 1] = face.method_10164();
      this.normals[normal + 2] = face.method_10165();
   }

   @Override
   public void method_32089(class_4665 pose, class_4588 consumer, int light, int overlay, float red, float green, float blue, float alpha) {
      Matrix4f matrix = pose.method_23761();
      Matrix3f normals = pose.method_23762();
      Vector3f scratch = new Vector3f();
      Vector4f corner = new Vector4f();

      for (int face = 0; face < this.faceCount; face++) {
         int normal = face * 3;
         Vector3f transformed = normals.transform(scratch.set(this.normals[normal], this.normals[normal + 1], this.normals[normal + 2]));
         float nx = transformed.x();
         float ny = transformed.y();
         float nz = transformed.z();
         int position = face * 12;
         int texture = face * 8;

         for (int i = 0; i < 4; i++) {
            Vector4f vertex = matrix.transform(
               corner.set(this.positions[position + i * 3], this.positions[position + i * 3 + 1], this.positions[position + i * 3 + 2], 1.0F)
            );
            consumer.method_23919(
               vertex.x(),
               vertex.y(),
               vertex.z(),
               red,
               green,
               blue,
               alpha,
               this.uvs[texture + i * 2],
               this.uvs[texture + i * 2 + 1],
               overlay,
               light,
               nx,
               ny,
               nz
            );
         }
      }
   }
}
