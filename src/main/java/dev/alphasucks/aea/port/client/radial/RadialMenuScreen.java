package dev.alphasucks.aea.port.client.radial;

import dev.alphasucks.aea.port.client.ScreenCloseKeys;
import dev.alphasucks.aea.port.network.payload.RadialMenuOpenS2C;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;

public class RadialMenuScreen extends class_437 {
   private int elapsedTime;
   private final RadialMenu radialMenu;

   public RadialMenuScreen(RadialMenuOpenS2C payload) {
      super(class_2561.method_43471("screen.apoli.radial_menu"));
      this.radialMenu = new RadialMenu(payload.entries(), (class_2960)payload.sprite().orElse(null), payload.nonce());
      this.elapsedTime = 0;
   }

   @Override
   public void method_25393() {
      this.elapsedTime++;
      this.radialMenu.draw(this.field_22787, this.elapsedTime);
   }

   @Override
   protected void method_25426() {
      this.radialMenu.resetButtons();
      this.radialMenu.draw(this.field_22787, this.elapsedTime);

      for (class_4185 button : this.radialMenu.getButtons()) {
         if (button != null) {
            this.method_37063(button);
         }
      }
   }

   @Override
   public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
      if (this.field_22787 != null) {
         this.radialMenu.renderBackground(context, this.field_22787);
         this.radialMenu.renderButtons(context, mouseX, mouseY, delta);
         this.radialMenu.renderIcons(context);
      }
   }

   @Override
   public boolean method_25421() {
      return false;
   }

   @Override
   public boolean method_25404(int keyCode, int scanCode, int modifiers) {
      if (this.field_22787 == null
         || !ScreenCloseKeys.closes(this, keyCode, scanCode) && !this.field_22787.field_1690.field_1822.method_1417(keyCode, scanCode)) {
         return super.method_25404(keyCode, scanCode, modifiers);
      }

      this.method_25419();
      return true;
   }

   @Override
   public void method_25419() {
      super.method_25419();
      this.radialMenu.resetButtons();
   }
}
