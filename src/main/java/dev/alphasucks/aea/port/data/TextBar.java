package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum TextBar implements class_3542 {
   ACTIONBAR("actionbar"),
   TITLE("title"),
   SUBTITLE("subtitle"),
   TOP_LEFT("top_left"),
   TOP_CENTER("top_center"),
   TOP_RIGHT("top_right"),
   LEFT("left"),
   RIGHT("right"),
   BOTTOM_LEFT("bottom_left"),
   BOTTOM_RIGHT("bottom_right");

   public static final Codec<TextBar> CODEC = class_3542.method_28140(TextBar::values);
   public static final TextBar[] VALUES = values();
   private final String name;

   TextBar(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
