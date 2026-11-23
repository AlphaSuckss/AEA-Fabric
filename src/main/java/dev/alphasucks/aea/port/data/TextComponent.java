package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_5699;

public final class TextComponent {
   public static final Codec<class_2561> CODEC = class_5699.field_40722;

   private TextComponent() {
   }

   public static String autoKey(class_2960 id, String suffix) {
      return "power." + id.method_12836() + "." + id.method_12832() + "." + suffix;
   }

   public static class_2561 autoTranslatable(class_2960 id, String suffix) {
      return class_2561.method_43471(autoKey(id, suffix));
   }
}
