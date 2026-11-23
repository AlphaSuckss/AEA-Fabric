package dev.overgrown.sync.factory.action.entity.radial_menu.utils;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.radial_menu.packet.NetworkingConstants;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_1799;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_4587;
import net.minecraft.class_7919;

public class RadialMenu {
   private List<RadialMenuEntry> entries;
   private class_2960 menuTexture;
   private boolean buttonsInitialized = false;

   public RadialMenu(List<RadialMenuEntry> entries, class_2960 menuTexture) {
      if (entries == null || entries.size() != 0) {
         this.entries = entries;
         this.menuTexture = menuTexture;
      }
   }

   @Environment(EnvType.CLIENT)
   public void draw(class_310 client, long elapsedTime) {
      this.positionEntries(client, elapsedTime);
      if (!this.buttonsInitialized) {
         this.entries
            .forEach(
               radialMenuEntry -> {
                  int buttonWidth = radialMenuEntry.getButtonWidth();
                  int buttonHeight = radialMenuEntry.getButtonHeight();
                  class_2561 tooltipText = radialMenuEntry.getTooltipText();
                  if (tooltipText == null && !radialMenuEntry.getStack().method_7960()) {
                     tooltipText = class_2561.method_43470(radialMenuEntry.getStack().method_7964().getString());
                  }

                  int index = this.entries.indexOf(radialMenuEntry);
                  class_4185 button = class_4185.method_46430(class_2561.method_43473(), widget -> {
                        if (radialMenuEntry.getEntityAction() != null) {
                           // only the index is sent; the server runs the action of the entry it offered
                           class_2540 buf = new class_2540(Unpooled.buffer());
                           buf.method_10804(index);
                           ClientPlayNetworking.send(NetworkingConstants.RADIAL_MENU_CLIENT_TO_SERVER, buf);
                        }
                     })
                     .method_46433(-100, 0)
                     .method_46437(buttonWidth, buttonHeight)
                     .method_46436(tooltipText != null ? class_7919.method_47407(tooltipText) : null)
                     .method_46431();
                  button.field_22763 = true;
                  button.field_22764 = true;
                  radialMenuEntry.setButton(button);
               }
            );
         this.buttonsInitialized = true;
      }

      this.entries.forEach(radialMenuEntry -> {
         class_4185 button = radialMenuEntry.getButton();
         if (button != null) {
            button.method_46421(Math.round(radialMenuEntry.getPosition().x()));
            button.method_46419(Math.round(radialMenuEntry.getPosition().y() - 1.0F));
         }
      });
   }

   @Environment(EnvType.CLIENT)
   public void renderBackground(class_332 context, class_310 client) {
      if (this.menuTexture != null) {
         try {
            int centerX = client.method_22683().method_4486() / 2;
            int centerY = client.method_22683().method_4502() / 2;
            int textureSize = 256;
            int halfSize = textureSize / 2;
            context.method_25290(this.menuTexture, centerX - halfSize, centerY - halfSize, 0.0F, 0.0F, textureSize, textureSize, textureSize, textureSize);
         } catch (Exception e) {
            Sync.LOGGER.warn("Could not load radial menu texture: {}", this.menuTexture);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   public void renderButtons(class_332 context, class_310 client, int mouseX, int mouseY, float delta) {
      this.entries.forEach(radialMenuEntry -> {
         class_4185 button = radialMenuEntry.getButton();
         if (button != null) {
            class_2960 buttonTexture = radialMenuEntry.getButtonTexture();
            class_2960 highlightButtonTexture = radialMenuEntry.getHighlightButtonTexture();
            boolean isHovered = button.method_25367();
            if (isHovered && highlightButtonTexture != null) {
               buttonTexture = highlightButtonTexture;
            }

            if (buttonTexture != null) {
               button.method_25350(0.0F);
               button.method_25394(context, mouseX, mouseY, delta);
               button.method_25350(1.0F);

               try {
                  int buttonX = button.method_46426();
                  int buttonY = button.method_46427();
                  int buttonWidth = button.method_25368();
                  int buttonHeight = button.method_25364();
                  context.method_25290(buttonTexture, buttonX, buttonY, 0.0F, 0.0F, buttonWidth, buttonHeight, buttonWidth, buttonHeight);
               } catch (Exception e) {
                  Sync.LOGGER.warn("Could not load button texture: {}", buttonTexture);
               }
            } else {
               button.method_25394(context, mouseX, mouseY, delta);
            }
         }
      });
   }

   @Environment(EnvType.CLIENT)
   public void renderIcons(class_332 context, class_310 client) {
      this.entries.forEach(radialMenuEntry -> {
         class_4185 button = radialMenuEntry.getButton();
         if (button != null) {
            class_2960 icon = radialMenuEntry.getIcon();
            class_2960 highlightIcon = radialMenuEntry.getHighlightIcon();
            boolean isHovered = button.method_25367();
            if (isHovered && highlightIcon != null) {
               icon = highlightIcon;
            }

            if (icon != null) {
               try {
                  int buttonX = button.method_46426();
                  int buttonY = button.method_46427();
                  int buttonWidth = button.method_25368();
                  int buttonHeight = button.method_25364();
                  int iconWidth = radialMenuEntry.getIconWidth();
                  int iconHeight = radialMenuEntry.getIconHeight();
                  int iconX = buttonX + (buttonWidth - iconWidth) / 2;
                  int iconY = buttonY + (buttonHeight - iconHeight) / 2;
                  context.method_25290(icon, iconX, iconY, 0.0F, 0.0F, iconWidth, iconHeight, iconWidth, iconHeight);
               } catch (Exception e) {
                  Sync.LOGGER.warn("Could not load icon texture: {}", icon);
               }
            } else {
               class_1799 stack = radialMenuEntry.getStack();
               if (!stack.method_7960()) {
                  int buttonX = button.method_46426();
                  int buttonY = button.method_46427();
                  int buttonWidth = button.method_25368();
                  int buttonHeight = button.method_25364();
                  int itemWidth = radialMenuEntry.getItemWidth();
                  int itemHeight = radialMenuEntry.getItemHeight();
                  int itemX = buttonX + (buttonWidth - itemWidth) / 2;
                  int itemY = buttonY + (buttonHeight - itemHeight) / 2;
                  class_4587 matrices = context.method_51448();
                  matrices.method_22903();
                  float scaleX = itemWidth / 16.0F;
                  float scaleY = itemHeight / 16.0F;
                  matrices.method_46416(itemX, itemY, 0.0F);
                  matrices.method_22905(scaleX, scaleY, 1.0F);
                  context.method_51429(stack, 0, 0, 0, 100);
                  matrices.method_22909();
               }
            }
         }
      });
   }

   private void positionEntries(class_310 client, long elapsedTime) {
      float angleInterval = 360.0F / this.entries.size();

      for (int i = 0; i < this.entries.size(); i++) {
         float angle = angleInterval * i;
         Vector2f center = new Vector2f(client.method_22683().method_4486() / 2.0F, client.method_22683().method_4502() / 2.0F);
         int maxDistance = ((RadialMenuEntry)this.entries.get(i)).getDistance() != -1
            ? ((RadialMenuEntry)this.entries.get(i)).getDistance()
            : client.method_22683().method_4502() / 4;
         float velocity = ((RadialMenuEntry)this.entries.get(i)).getVelocity() != -1
            ? ((RadialMenuEntry)this.entries.get(i)).getVelocity()
            : maxDistance / 3.0F;
         float distance = velocity * (float)elapsedTime < maxDistance ? velocity * (float)elapsedTime : maxDistance;
         Vector2f position = getPosFromAngle(angle, distance, center);
         int buttonWidth = ((RadialMenuEntry)this.entries.get(i)).getButtonWidth();
         int buttonHeight = ((RadialMenuEntry)this.entries.get(i)).getButtonHeight();
         ((RadialMenuEntry)this.entries.get(i)).setPosition(new Vector2f(position.x() - buttonWidth / 2.0F, position.y() - buttonHeight / 2.0F));
      }
   }

   public static Vector2f getPosFromAngle(float angle, float distance, Vector2f center) {
      return new Vector2f(
         (float)(center.x() + distance * Math.cos(angle * (Math.PI / 180.0))), (float)(center.y() + distance * Math.sin(angle * (Math.PI / 180.0)))
      );
   }

   public List<RadialMenuEntry> getEntries() {
      return this.entries;
   }

   public class_2960 getMenuTexture() {
      return this.menuTexture;
   }

   public void resetButtons() {
      this.buttonsInitialized = false;
      this.entries.forEach(entry -> entry.setButton(null));
   }
}
