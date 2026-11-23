package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2540;
import net.minecraft.class_2960;

public record RadialMenuSelectC2S(int nonce, int index) {
   public static final class_2960 CHANNEL = Apoli.internal("radial_menu_select");

   public void write(class_2540 buf) {
      buf.method_10804(this.nonce);
      buf.method_10804(this.index);
   }

   public static RadialMenuSelectC2S read(class_2540 buf) {
      return new RadialMenuSelectC2S(buf.method_10816(), buf.method_10816());
   }
}
