package dev.alphasucks.aea.port.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.IconData;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_1044;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;

@Environment(EnvType.CLIENT)
public final class IconRenderer {
   private static final int FALLBACK_TEXTURE_SIZE = 16;
   private static final Map<class_2960, int[]> TEXTURE_SIZES = new HashMap();

   private IconRenderer() {
   }

   public static void render(class_332 graphics, IconData icon, int x, int y) {
      render(graphics, icon, x, y, 16, 16, false);
   }

   public static void renderFake(class_332 graphics, IconData icon, int x, int y) {
      render(graphics, icon, x, y, 16, 16, true);
   }

   public static void render(class_332 graphics, IconData icon, int x, int y, int width, int height, boolean fake) {
      class_2960 texture = (class_2960)icon.texture().orElse(null);
      if (texture != null) {
         renderTexture(graphics, texture, icon.width(), icon.height(), x, y, width, height);
      } else if (!icon.stack().method_7960()) {
         if (fake) {
            graphics.method_51445(icon.stack(), x, y);
         } else {
            graphics.method_51427(icon.stack(), x, y);
         }
      }
   }

   private static void renderTexture(class_332 graphics, class_2960 texture, int declaredWidth, int declaredHeight, int x, int y, int width, int height) {
      int[] size = sizeOf(texture);
      int textureWidth = size[0];
      int textureHeight = size[1];
      int regionWidth = declaredWidth > 0 ? Math.min(declaredWidth, textureWidth) : textureWidth;
      int regionHeight = declaredHeight > 0 ? Math.min(declaredHeight, textureHeight) : textureHeight;
      class_1044 bound = class_310.method_1551().method_1531().method_4619(texture);
      bound.method_4527(regionWidth > width || regionHeight > height, false);
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      graphics.method_25293(texture, x, y, width, height, 0.0F, 0.0F, regionWidth, regionHeight, textureWidth, textureHeight);
   }

   private static int[] sizeOf(class_2960 texture) {
      int[] cached = (int[])TEXTURE_SIZES.get(texture);
      if (cached != null) {
         return cached;
      }

      int[] size = new int[]{16, 16};

      try {
         InputStream in = class_310.method_1551().method_1478().open(texture);

         try (class_1011 image = class_1011.method_4309(in)) {
            size = new int[]{Math.max(1, image.method_4307()), Math.max(1, image.method_4323())};
         } catch (Throwable var10) {
            if (in != null) {
               try {
                  in.close();
               } catch (Throwable var7) {
                  var10.addSuppressed(var7);
               }
            }

            throw var10;
         }

         if (in != null) {
            in.close();
         }
      } catch (Exception e) {
         Apoli.LOGGER.warn("[Apoli] Could not read icon texture {}, assuming {}x{}: {}", new Object[]{texture, 16, 16, e.toString()});
      }

      TEXTURE_SIZES.put(texture, size);
      return size;
   }

   public static void clearCache() {
      TEXTURE_SIZES.clear();
   }
}
