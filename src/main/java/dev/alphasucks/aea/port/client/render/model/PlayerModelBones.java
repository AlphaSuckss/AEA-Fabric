package dev.alphasucks.aea.port.client.render.model;

import dev.alphasucks.aea.port.client.model.PerFaceCube;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_5603;
import net.minecraft.class_630;
import net.minecraft.class_630.class_628;

public final class PlayerModelBones {
   public static final float SKIN_WIDTH = 64.0F;
   public static final float SKIN_HEIGHT = 64.0F;

   private PlayerModelBones() {
   }

   public static class_630 bone(float x, float y, float z, float xRot, float yRot, float zRot, List<class_628> cubes, class_630... children) {
      Map<String, class_630> named = Map.of();
      if (children.length > 0) {
         named = new LinkedHashMap(children.length * 2);

         for (int i = 0; i < children.length; i++) {
            named.put("bone" + i, children[i]);
         }
      }

      class_630 part = new class_630(cubes, named);
      part.method_41918(class_5603.method_32091(x, y, z, xRot, yRot, zRot));
      part.method_41923();
      return part;
   }

   public static class_628 cube(float x, float y, float z, float sizeX, float sizeY, float sizeZ, float grow, boolean mirror, float[][] faces) {
      return PerFaceCube.of(faces, x, y, z, sizeX, sizeY, sizeZ, grow, mirror, 64.0F, 64.0F);
   }
}
