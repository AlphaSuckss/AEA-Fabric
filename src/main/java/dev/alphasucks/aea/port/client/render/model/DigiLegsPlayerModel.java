package dev.alphasucks.aea.port.client.render.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1309;
import net.minecraft.class_5605;
import net.minecraft.class_5607;
import net.minecraft.class_591;
import net.minecraft.class_630;

public class DigiLegsPlayerModel<T extends class_1309> extends class_591<T> {
   private static final String[] SHARED_PARTS = new String[]{
      "head", "hat", "body", "jacket", "right_arm", "left_arm", "right_sleeve", "left_sleeve", "ear", "cloak"
   };

   public DigiLegsPlayerModel(class_630 root, boolean slim) {
      super(root, slim);
   }

   public static class_630 createRoot(boolean slim) {
      class_630 vanilla = class_5607.method_32110(class_591.method_32028(class_5605.field_27715, slim), 64, 64).method_32109();
      Map<String, class_630> children = new LinkedHashMap(SHARED_PARTS.length * 2 + 8);

      for (String name : SHARED_PARTS) {
         children.put(name, vanilla.method_32086(name));
      }

      children.put("right_leg", rightLeg());
      children.put("left_leg", leftLeg());
      children.put("right_pants", rightPants());
      children.put("left_pants", leftPants());
      return new class_630(List.of(), children);
   }

   private static class_630 rightLeg() {
      return PlayerModelBones.bone(
         -1.9F,
         12.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         List.of(),
         PlayerModelBones.bone(
            -0.1F,
            4.3F,
            -2.5F,
            1.20428F,
            0.0F,
            0.0F,
            List.of(),
            PlayerModelBones.bone(
               0.0F,
               -0.21502F,
               0.56015F,
               -0.38397F,
               0.0F,
               0.0F,
               List.of(
                  PlayerModelBones.cube(
                     -1.825F,
                     -0.9F,
                     -1.225F,
                     3.65F,
                     6.85F,
                     3.65F,
                     0.0F,
                     false,
                     new float[][]{
                        {20.0F, 52.0F, 4.0F, -4.0F},
                        {24.0F, 52.0F, 3.8F, -4.1F},
                        {20.0F, 56.0F, -4.0F, 3.0F},
                        {24.0F, 56.0F, -4.0F, 3.0F},
                        {28.0F, 56.0F, -4.0F, 3.0F},
                        {32.0F, 56.0F, -4.0F, 3.0F}
                     }
                  )
               )
            )
         ),
         PlayerModelBones.bone(
            0.0F,
            -0.6F,
            0.0F,
            -0.38397F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  0.0F,
                  -2.0F,
                  4.0F,
                  6.0F,
                  4.0F,
                  0.0F,
                  false,
                  new float[][]{
                     {4.0F, 16.0F, 4.0F, 4.0F},
                     {8.0F, 20.0F, 4.0F, -4.0F},
                     {0.0F, 20.0F, 4.0F, 6.0F},
                     {4.0F, 20.0F, 4.0F, 6.0F},
                     {8.0F, 20.0F, 4.0F, 6.0F},
                     {12.0F, 20.0F, 4.0F, 6.0F}
                  }
               )
            )
         ),
         PlayerModelBones.bone(
            0.0F,
            7.0F,
            0.6F,
            -0.03054F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  -1.0F,
                  -2.0F,
                  4.0F,
                  6.3F,
                  4.0F,
                  0.0F,
                  false,
                  new float[][]{
                     {7.0F, 16.0F, 4.0F, 4.0F},
                     {8.0F, 20.0F, 4.0F, -4.0F},
                     {0.0F, 26.0F, 4.0F, 6.0F},
                     {4.0F, 26.0F, 4.0F, 6.0F},
                     {8.0F, 26.0F, 4.0F, 6.0F},
                     {12.0F, 26.0F, 4.0F, 6.0F}
                  }
               )
            )
         )
      );
   }

   private static class_630 rightPants() {
      return PlayerModelBones.bone(
         -1.9F,
         12.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         List.of(),
         PlayerModelBones.bone(
            -0.1F,
            4.3F,
            -2.5F,
            1.20428F,
            0.0F,
            0.0F,
            List.of(),
            PlayerModelBones.bone(
               0.0F,
               -0.21502F,
               0.56015F,
               -0.38397F,
               0.0F,
               0.0F,
               List.of(
                  PlayerModelBones.cube(
                     -1.825F,
                     -0.9F,
                     -1.225F,
                     3.65F,
                     6.7F,
                     3.65F,
                     0.25F,
                     false,
                     new float[][]{
                        {4.0F, 42.0F, 4.0F, -2.0F},
                        {8.0F, 36.0F, 4.0F, -4.0F},
                        {4.0F, 40.0F, -4.0F, 2.0F},
                        {8.0F, 40.0F, -4.0F, 2.0F},
                        {8.0F, 40.0F, -4.0F, 2.0F},
                        {16.0F, 57.0F, -4.0F, 2.0F}
                     }
                  )
               )
            )
         ),
         PlayerModelBones.bone(
            0.0F,
            -0.6F,
            0.0F,
            -0.38397F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  0.0F,
                  -2.0F,
                  4.0F,
                  5.75F,
                  4.0F,
                  0.25F,
                  false,
                  new float[][]{
                     {4.0F, 32.0F, 4.0F, 4.0F},
                     {8.0F, 36.0F, 4.0F, -4.0F},
                     {0.0F, 36.0F, 4.0F, 6.0F},
                     {4.0F, 36.0F, 4.0F, 6.0F},
                     {8.0F, 36.0F, 4.0F, 6.0F},
                     {12.0F, 36.0F, 4.0F, 6.0F}
                  }
               )
            )
         ),
         PlayerModelBones.bone(
            0.0F,
            7.0F,
            0.6F,
            -0.03054F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  -0.75F,
                  -2.0F,
                  4.0F,
                  6.05F,
                  4.0F,
                  0.25F,
                  false,
                  new float[][]{
                     {4.0F, 32.0F, 4.0F, 4.0F},
                     {8.0F, 36.0F, 4.0F, -4.0F},
                     {0.0F, 42.0F, 4.0F, 6.0F},
                     {4.0F, 42.0F, 4.0F, 6.0F},
                     {8.0F, 42.0F, 4.0F, 6.0F},
                     {12.0F, 42.0F, 4.0F, 6.0F}
                  }
               )
            )
         )
      );
   }

   private static class_630 leftLeg() {
      return PlayerModelBones.bone(
         1.9F,
         12.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         List.of(),
         PlayerModelBones.bone(
            0.0F,
            -0.6F,
            0.0F,
            -0.38397F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  0.0F,
                  -2.0F,
                  4.0F,
                  6.0F,
                  4.0F,
                  0.0F,
                  false,
                  new float[][]{
                     {20.0F, 48.0F, 4.0F, 4.0F},
                     {24.0F, 52.0F, 4.0F, -4.0F},
                     {16.0F, 52.0F, 4.0F, 6.0F},
                     {20.0F, 52.0F, 4.0F, 6.0F},
                     {24.0F, 52.0F, 4.0F, 6.0F},
                     {28.0F, 52.0F, 4.0F, 6.0F}
                  }
               )
            )
         ),
         PlayerModelBones.bone(
            0.0F,
            7.0F,
            0.6F,
            -0.03054F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  -1.0F,
                  -2.0F,
                  4.0F,
                  6.3F,
                  4.0F,
                  0.0F,
                  false,
                  new float[][]{
                     {24.0F, 48.0F, 4.0F, 4.0F},
                     {24.0F, 52.0F, 4.0F, -4.0F},
                     {16.0F, 58.0F, 4.0F, 6.0F},
                     {20.0F, 58.0F, 4.0F, 6.0F},
                     {24.0F, 58.0F, 4.0F, 6.0F},
                     {28.0F, 58.0F, 4.0F, 6.0F}
                  }
               )
            )
         ),
         PlayerModelBones.bone(
            0.1F,
            4.3F,
            -2.5F,
            1.20428F,
            0.0F,
            0.0F,
            List.of(),
            PlayerModelBones.bone(
               0.0F,
               -0.21502F,
               0.56015F,
               -0.38397F,
               0.0F,
               0.0F,
               List.of(
                  PlayerModelBones.cube(
                     -1.825F,
                     -0.9F,
                     -1.225F,
                     3.65F,
                     6.85F,
                     3.65F,
                     0.0F,
                     false,
                     new float[][]{
                        {9.0F, 27.0F, 4.0F, -3.0F},
                        {8.0F, 19.5F, 3.8F, -3.5F},
                        {4.0F, 23.0F, -4.0F, 3.0F},
                        {8.0F, 24.0F, -4.0F, 3.0F},
                        {12.0F, 23.0F, -4.0F, 3.0F},
                        {16.0F, 23.0F, -4.0F, 3.0F}
                     }
                  )
               )
            )
         )
      );
   }

   private static class_630 leftPants() {
      return PlayerModelBones.bone(
         1.9F,
         12.0F,
         0.0F,
         0.0F,
         0.0F,
         0.0F,
         List.of(),
         PlayerModelBones.bone(
            0.0F,
            -0.6F,
            0.0F,
            -0.38397F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  0.0F,
                  -2.0F,
                  4.0F,
                  5.75F,
                  4.0F,
                  0.25F,
                  false,
                  new float[][]{
                     {4.0F, 48.0F, 4.0F, 4.0F},
                     {8.0F, 36.0F, 4.0F, -4.0F},
                     {0.0F, 52.0F, 4.0F, 6.0F},
                     {4.0F, 52.0F, 4.0F, 6.0F},
                     {8.0F, 52.0F, 4.0F, 6.0F},
                     {12.0F, 52.0F, 4.0F, 6.0F}
                  }
               )
            )
         ),
         PlayerModelBones.bone(
            0.0F,
            7.0F,
            0.6F,
            -0.03054F,
            0.0F,
            0.0F,
            List.of(
               PlayerModelBones.cube(
                  -2.0F,
                  -0.75F,
                  -2.0F,
                  4.0F,
                  6.05F,
                  4.0F,
                  0.25F,
                  false,
                  new float[][]{
                     {4.0F, 48.0F, 4.0F, 4.0F},
                     {8.0F, 52.0F, 4.0F, -4.0F},
                     {0.0F, 58.0F, 4.0F, 6.0F},
                     {4.0F, 58.0F, 4.0F, 6.0F},
                     {8.0F, 58.0F, 4.0F, 6.0F},
                     {12.0F, 58.0F, 4.0F, 6.0F}
                  }
               )
            )
         ),
         PlayerModelBones.bone(
            0.1F,
            4.3F,
            -2.5F,
            1.20428F,
            0.0F,
            0.0F,
            List.of(),
            PlayerModelBones.bone(
               0.0F,
               -0.21502F,
               0.56015F,
               -0.38397F,
               0.0F,
               0.0F,
               List.of(
                  PlayerModelBones.cube(
                     -1.825F,
                     -0.9F,
                     -1.225F,
                     3.65F,
                     6.7F,
                     3.65F,
                     0.25F,
                     false,
                     new float[][]{
                        {4.0F, 42.0F, 4.0F, -2.0F},
                        {8.0F, 52.0F, 4.0F, -4.0F},
                        {8.0F, 40.0F, -4.0F, 2.0F},
                        {4.0F, 40.0F, 4.0F, 2.0F},
                        {12.0F, 40.0F, -4.0F, 2.0F},
                        {16.0F, 40.0F, -4.0F, 2.0F}
                     }
                  )
               )
            )
         )
      );
   }
}
