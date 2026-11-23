package dev.alphasucks.aea.port.client.particle;

import dev.alphasucks.aea.port.Apoli;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1049;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_1011.class_1012;

@Environment(EnvType.CLIENT)
public final class RepairedParticleTexture extends class_1049 {
   private static final int OPAQUE = 255;
   private static final int TOLERANCE = 12;
   private static final float SUSPECT_SHARE = 0.15F;
   private static final int MAX_ANALYSED = 262144;
   private final boolean bleed;

   public RepairedParticleTexture(class_2960 source, boolean bleed) {
      super(source);
      this.bleed = bleed;
   }

   @Override
   protected class_4006 method_18153(class_3300 resourceManager) {
      class_4006 loaded = super.method_18153(resourceManager);

      class_1011 image;
      try {
         image = loaded.method_18157();
      } catch (IOException ignored) {
         return loaded;
      }

      if (repair(image, this.bleed) && !this.bleed) {
         Apoli.LOGGER
            .warn(
               "[Apoli] Particle texture {} loses its colour across the semi-transparent pixels around its edge, so those pixels draw as a dark fringe that grows as the particle fills more of the screen. Either fill the colour in under the transparency in the PNG, or set \"alpha_bleed\": true on the apoli:custom particle to have Apoli fill it in at load.",
               this.field_5224
            );
      }

      return loaded;
   }

   static boolean repair(class_1011 image, boolean apply) {
      int width = image.method_4307();
      int height = image.method_4323();
      int size = width * height;
      if (size > 0 && image.method_4318() == class_1012.field_4997) {
         if (!scan(image, width, height)) {
            return false;
         }

         if (!apply && size > 262144) {
            return false;
         }

         int[] original = new int[size];
         int[] dilated = new int[size];
         int[] alpha = new int[size];
         int[] queue = new int[size];
         boolean[] filled = new boolean[size];
         int tail = 0;
         int semiTransparent = 0;

         for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
               int index = y * width + x;
               int packed = image.method_4315(x, y);
               int a = packed >>> 24 & 0xFF;
               alpha[index] = a;
               original[index] = packed & 16777215;
               dilated[index] = original[index];
               if (a == 255) {
                  filled[index] = true;
                  queue[tail++] = index;
               } else if (a != 0) {
                  semiTransparent++;
               }
            }
         }

         if (semiTransparent != 0 && tail != 0) {
            int head = 0;

            while (head < tail) {
               int index = queue[head++];
               int x = index % width;
               int y = index / width;

               for (int dy = -1; dy <= 1; dy++) {
                  int ny = y + dy;
                  if (ny >= 0 && ny < height) {
                     for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        if (nx >= 0 && nx < width && (dx != 0 || dy != 0)) {
                           int neighbour = ny * width + nx;
                           if (!filled[neighbour]) {
                              filled[neighbour] = true;
                              dilated[neighbour] = average(dilated, filled, width, height, nx, ny);
                              queue[tail++] = neighbour;
                           }
                        }
                     }
                  }
               }
            }

            int suspect = 0;

            for (int y = 0; y < height; y++) {
               for (int x = 0; x < width; x++) {
                  int index = y * width + x;
                  int a = alpha[index];
                  if (a != 255) {
                     int source = original[index];
                     int target = dilated[index];
                     if (a != 0 && darkerBy(source, target) > 12) {
                        suspect++;
                     }

                     if (apply) {
                        image.method_4305(x, y, a << 24 | target);
                     }
                  }
               }
            }

            return suspect > semiTransparent * 0.15F;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean scan(class_1011 image, int width, int height) {
      boolean opaque = false;
      boolean semiTransparent = false;

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int a = image.method_4315(x, y) >>> 24 & 0xFF;
            if (a == 255) {
               opaque = true;
            } else if (a != 0) {
               semiTransparent = true;
            }

            if (opaque && semiTransparent) {
               return true;
            }
         }
      }

      return false;
   }

   private static int darkerBy(int source, int target) {
      int red = (target & 0xFF) - (source & 0xFF);
      int green = (target >>> 8 & 0xFF) - (source >>> 8 & 0xFF);
      int blue = (target >>> 16 & 0xFF) - (source >>> 16 & 0xFF);
      return Math.max(red, Math.max(green, blue));
   }

   private static int average(int[] colour, boolean[] filled, int width, int height, int x, int y) {
      int red = 0;
      int green = 0;
      int blue = 0;
      int count = 0;

      for (int dy = -1; dy <= 1; dy++) {
         int ny = y + dy;
         if (ny >= 0 && ny < height) {
            for (int dx = -1; dx <= 1; dx++) {
               int nx = x + dx;
               if (nx >= 0 && nx < width && (dx != 0 || dy != 0)) {
                  int index = ny * width + nx;
                  if (filled[index]) {
                     int packed = colour[index];
                     red += packed & 0xFF;
                     green += packed >>> 8 & 0xFF;
                     blue += packed >>> 16 & 0xFF;
                     count++;
                  }
               }
            }
         }
      }

      return count == 0 ? 0 : blue / count << 16 | green / count << 8 | red / count;
   }
}
