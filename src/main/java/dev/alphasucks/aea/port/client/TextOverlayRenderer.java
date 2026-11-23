package dev.alphasucks.aea.port.client;

import dev.alphasucks.aea.port.data.TextBar;
import dev.alphasucks.aea.port.network.payload.TextDisplayS2C;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
public final class TextOverlayRenderer {
   private static final TextOverlayRenderer.Entry[] SLOTS = new TextOverlayRenderer.Entry[TextBar.VALUES.length];
   private static final float ABOVE_CHAT_Z = 200.0F;
   private static long guiTicks;

   private TextOverlayRenderer() {
   }

   public static void apply(TextDisplayS2C payload) {
      int slot = payload.bar().ordinal();
      if (payload.text().getString().isEmpty() && payload.text().method_10855().isEmpty()) {
         SLOTS[slot] = null;
      } else {
         TextOverlayRenderer.Entry entry = SLOTS[slot];
         boolean sameText = entry != null && payload.text().equals(entry.text);
         if (!sameText) {
            entry = new TextOverlayRenderer.Entry();
            entry.text = payload.text();
            entry.shownAt = guiTicks;
            SLOTS[slot] = entry;
         }

         entry.fadeIn = Math.max(0, payload.fadeIn());
         entry.fadeOut = Math.max(0, payload.fadeOut());
         entry.steadyUntil = payload.stay() < 0 ? Long.MAX_VALUE : guiTicks + entry.fadeIn + payload.stay();
      }
   }

   public static void clear() {
      Arrays.fill(SLOTS, null);
   }

   public static void tick() {
      guiTicks++;
   }

   public static void render(class_332 graphics, float partialTick) {
      class_310 mc = class_310.method_1551();
      if (!mc.field_1690.field_1842) {
         boolean any = false;

         for (TextOverlayRenderer.Entry slot : SLOTS) {
            if (slot != null) {
               any = true;
               break;
            }
         }

         if (any) {
            class_327 font = mc.field_1772;
            int width = graphics.method_51421();
            int height = graphics.method_51443();

            for (int i = 0; i < SLOTS.length; i++) {
               TextOverlayRenderer.Entry entry = SLOTS[i];
               if (entry != null) {
                  float age = (float)(guiTicks - entry.shownAt) + partialTick;
                  float alpha = alphaFor(entry, age, partialTick);
                  if (alpha <= 0.0F) {
                     if (entry.steadyUntil != Long.MAX_VALUE && guiTicks > entry.steadyUntil + entry.fadeOut) {
                        SLOTS[i] = null;
                     }
                  } else {
                     int alphaBits = (int)(alpha * 255.0F) << 24;
                     if ((alphaBits & -67108864) != 0) {
                        int color = 16777215 | alphaBits;
                        graphics.method_51448().method_22903();
                        graphics.method_51448().method_46416(0.0F, 0.0F, 200.0F);
                        draw(graphics, font, TextBar.VALUES[i], entry.text, color, width, height);
                        graphics.method_51448().method_22909();
                     }
                  }
               }
            }
         }
      }
   }

   private static float alphaFor(TextOverlayRenderer.Entry entry, float age, float partialTick) {
      if (entry.fadeIn > 0 && age < entry.fadeIn) {
         return age / entry.fadeIn;
      } else if (entry.steadyUntil == Long.MAX_VALUE) {
         return 1.0F;
      } else {
         float sinceSteadyEnd = (float)(guiTicks - entry.steadyUntil) + partialTick;
         if (sinceSteadyEnd <= 0.0F) {
            return 1.0F;
         } else {
            return entry.fadeOut <= 0 ? 0.0F : class_3532.method_15363(1.0F - sinceSteadyEnd / entry.fadeOut, 0.0F, 1.0F);
         }
      }
   }

   private static void draw(class_332 graphics, class_327 font, TextBar bar, class_2561 text, int color, int width, int height) {
      int textWidth = font.method_27525(text);
      switch (bar) {
         case TITLE:
            graphics.method_51448().method_22903();
            graphics.method_51448().method_46416(width / 2.0F, height / 2.0F, 0.0F);
            graphics.method_51448().method_22905(4.0F, 4.0F, 4.0F);
            graphics.method_51439(font, text, -textWidth / 2, -10, color, true);
            graphics.method_51448().method_22909();
            break;
         case SUBTITLE:
            graphics.method_51448().method_22903();
            graphics.method_51448().method_46416(width / 2.0F, height / 2.0F, 0.0F);
            graphics.method_51448().method_22905(2.0F, 2.0F, 2.0F);
            graphics.method_51439(font, text, -textWidth / 2, 5, color, true);
            graphics.method_51448().method_22909();
            break;
         case ACTIONBAR:
            graphics.method_51439(font, text, (width - textWidth) / 2, height - 68, color, true);
            break;
         case TOP_LEFT:
            graphics.method_51439(font, text, 4, 4, color, true);
            break;
         case TOP_CENTER:
            graphics.method_51439(font, text, (width - textWidth) / 2, 24, color, true);
            break;
         case TOP_RIGHT:
            graphics.method_51439(font, text, width - textWidth - 4, 4, color, true);
            break;
         case LEFT:
            graphics.method_51439(font, text, 4, (height - 9) / 2, color, true);
            break;
         case RIGHT:
            graphics.method_51439(font, text, width - textWidth - 4, (height - 9) / 2, color, true);
            break;
         case BOTTOM_LEFT:
            graphics.method_51439(font, text, 4, height - 50, color, true);
            break;
         case BOTTOM_RIGHT:
            graphics.method_51439(font, text, width - textWidth - 4, height - 50, color, true);
      }
   }

   private static final class Entry {
      class_2561 text;
      int fadeIn;
      int fadeOut;
      long shownAt;
      long steadyUntil;
   }
}
