package dev.alphasucks.aea.port.network.payload;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.data.TextBar;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record TextDisplayS2C(TextBar bar, class_2561 text, int fadeIn, int stay, int fadeOut) {
   public static final class_2960 CHANNEL = Apoli.internal("text_display");

   public void write(class_2540 buf) {
      buf.writeByte(this.bar.ordinal());
      buf.method_10805(this.text);
      buf.method_10804(this.fadeIn);
      buf.method_10804(this.stay);
      buf.method_10804(this.fadeOut);
   }

   public static TextDisplayS2C read(class_2540 buf) {
      TextBar bar = TextBar.VALUES[Math.floorMod(buf.readByte(), TextBar.VALUES.length)];
      class_2561 text = buf.method_10808();
      int fadeIn = buf.method_10816();
      int stay = buf.method_10816();
      int fadeOut = buf.method_10816();
      return new TextDisplayS2C(bar, text, fadeIn, stay, fadeOut);
   }
}
