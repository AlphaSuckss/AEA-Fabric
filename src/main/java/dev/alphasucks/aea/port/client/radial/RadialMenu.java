package dev.alphasucks.aea.port.client.radial;

import dev.alphasucks.aea.port.client.ScreenCloseKeys;
import dev.alphasucks.aea.port.network.payload.RadialMenuSelectC2S;
import dev.alphasucks.aea.port.network.payload.RadialMenuOpenS2C.Entry;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_1799;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_7919;

public class RadialMenu {
   private final List<Entry> entries;
   private final class_2960 menuTexture;
   private final int nonce;
   private final class_4185[] buttons;
   private final float[] posX;
   private final float[] posY;
   private boolean buttonsInitialized = false;

   public RadialMenu(List<Entry> entries, class_2960 menuTexture, int nonce) {
      this.entries = entries;
      this.menuTexture = menuTexture;
      this.nonce = nonce;
      this.buttons = new class_4185[entries.size()];
      this.posX = new float[entries.size()];
      this.posY = new float[entries.size()];
   }

   public class_4185[] getButtons() {
      return this.buttons;
   }

   public void draw(class_310 client, int elapsedTime) {
      this.positionEntries(client, elapsedTime);
      if (!this.buttonsInitialized) {
         for (int i = 0; i < this.entries.size(); i++) {
            Entry entry = (Entry)this.entries.get(i);
            int buttonWidth = entry.buttonWidth();
            int buttonHeight = entry.buttonHeight();
            class_2561 tooltipText = (class_2561)entry.tooltip().orElse(null);
            if (tooltipText == null && !entry.item().method_7960()) {
               tooltipText = class_2561.method_43470(entry.item().method_7964().getString());
            }

            int index = i;
            class_4185 button = class_4185.method_46430(class_2561.method_43473(), widget -> this.select(index))
               .method_46433(-100, 0)
               .method_46437(buttonWidth, buttonHeight)
               .method_46436(tooltipText != null ? class_7919.method_47407(tooltipText) : null)
               .method_46431();
            button.field_22763 = true;
            button.field_22764 = true;
            this.buttons[i] = button;
         }

         this.buttonsInitialized = true;
      }

      for (int i = 0; i < this.entries.size(); i++) {
         class_4185 button = this.buttons[i];
         if (button != null) {
            button.method_46421(Math.round(this.posX[i]));
            button.method_46419(Math.round(this.posY[i] - 1.0F));
         }
      }
   }

   public void renderBackground(class_332 context, class_310 client) {
      if (this.menuTexture != null) {
         int centerX = client.method_22683().method_4486() / 2;
         int centerY = client.method_22683().method_4502() / 2;
         int textureSize = 256;
         int halfSize = textureSize / 2;
         context.method_25290(this.menuTexture, centerX - halfSize, centerY - halfSize, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
      }
   }

   public void renderButtons(class_332 context, int mouseX, int mouseY, float delta) {
      for (int i = 0; i < this.entries.size(); i++) {
         class_4185 button = this.buttons[i];
         if (button != null) {
            Entry entry = (Entry)this.entries.get(i);
            class_2960 buttonTexture = (class_2960)entry.buttonTexture().orElse(null);
            class_2960 highlightButtonTexture = (class_2960)entry.highlightButtonTexture().orElse(null);
            boolean isHovered = button.method_25367();
            if (isHovered && highlightButtonTexture != null) {
               buttonTexture = highlightButtonTexture;
            }

            if (buttonTexture != null) {
               button.method_25350(0.0F);
               button.method_25394(context, mouseX, mouseY, delta);
               button.method_25350(1.0F);
               context.method_25290(
                  buttonTexture,
                  button.method_46426(),
                  button.method_46427(),
                  0.0F,
                  0.0F,
                  button.method_25368(),
                  button.method_25364(),
                  button.method_25368(),
                  button.method_25364()
               );
            } else {
               button.method_25394(context, mouseX, mouseY, delta);
            }
         }
      }
   }

   public void renderIcons(class_332 context) {
      for (int i = 0; i < this.entries.size(); i++) {
         class_4185 button = this.buttons[i];
         if (button != null) {
            Entry entry = (Entry)this.entries.get(i);
            class_2960 icon = (class_2960)entry.icon().orElse(null);
            class_2960 highlightIcon = (class_2960)entry.highlightIcon().orElse(null);
            boolean isHovered = button.method_25367();
            if (isHovered && highlightIcon != null) {
               icon = highlightIcon;
            }

            int buttonX = button.method_46426();
            int buttonY = button.method_46427();
            int buttonWidth = button.method_25368();
            int buttonHeight = button.method_25364();
            if (icon != null) {
               int iconWidth = entry.iconWidth();
               int iconHeight = entry.iconHeight();
               int iconX = buttonX + (buttonWidth - iconWidth) / 2;
               int iconY = buttonY + (buttonHeight - iconHeight) / 2;
               context.method_25290(icon, iconX, iconY, 0.0F, 0.0F, iconWidth, iconHeight, iconWidth, iconHeight);
            } else if (!entry.item().method_7960()) {
               class_1799 stack = entry.item();
               int itemWidth = entry.itemWidth();
               int itemHeight = entry.itemHeight();
               int itemX = buttonX + (buttonWidth - itemWidth) / 2;
               int itemY = buttonY + (buttonHeight - itemHeight) / 2;
               context.method_51448().method_22903();
               context.method_51448().method_46416(itemX, itemY, 0.0F);
               context.method_51448().method_22905(itemWidth / 16.0F, itemHeight / 16.0F, 1.0F);
               context.method_51429(stack, 0, 0, 0, 100);
               context.method_51448().method_22909();
            }
         }
      }
   }

   private void positionEntries(class_310 client, int elapsedTime) {
      int count = this.entries.size();
      if (count != 0) {
         float angleInterval = 360.0F / count;

         for (int i = 0; i < count; i++) {
            float centerX = client.method_22683().method_4486() / 2.0F;
            float centerY = client.method_22683().method_4502() / 2.0F;
            Entry entry = (Entry)this.entries.get(i);
            float angle = (Float)entry.angle().orElse(angleInterval * i);
            int maxDistance = entry.distance() != -1 ? entry.distance() : client.method_22683().method_4502() / 4;
            float velocity = entry.velocity() != -1 ? entry.velocity() : maxDistance / 3.0F;
            float distance = velocity * elapsedTime < maxDistance ? velocity * elapsedTime : maxDistance;
            float progress = maxDistance == 0 ? 1.0F : distance / maxDistance;
            float px = (float)(centerX + distance * Math.cos(Math.toRadians(angle))) + entry.offsetX() * progress;
            float py = (float)(centerY + distance * Math.sin(Math.toRadians(angle))) + entry.offsetY() * progress;
            this.posX[i] = px - entry.buttonWidth() / 2.0F;
            this.posY[i] = py - entry.buttonHeight() / 2.0F;
         }
      }
   }

   public void resetButtons() {
      this.buttonsInitialized = false;

      for (int i = 0; i < this.buttons.length; i++) {
         this.buttons[i] = null;
      }
   }

   private void select(int index) {
      ScreenCloseKeys.carryOver();
      class_2540 buf = new class_2540(Unpooled.buffer());
      new RadialMenuSelectC2S(this.nonce, index).write(buf);
      ClientPlayNetworking.send(RadialMenuSelectC2S.CHANNEL, buf);
      class_310.method_1551().method_1507(null);
   }
}
