package dev.alphasucks.aea.port.sound;

import dev.alphasucks.aea.port.Apoli;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class SoundVariants {
   private static final String PREFIX = "sound_variant/";

   private SoundVariants() {
   }

   public static class_2960 encode(class_2960 sound, int index) {
      return Apoli.id("sound_variant/" + index + "/" + sound.method_12836() + "/" + sound.method_12832());
   }

   public static boolean isVariant(class_2960 location) {
      return "apoli".equals(location.method_12836()) && location.method_12832().startsWith("sound_variant/");
   }

   public static int index(class_2960 variant) {
      String path = variant.method_12832();
      int end = path.indexOf(47, "sound_variant/".length());
      if (end <= "sound_variant/".length()) {
         return -1;
      }

      try {
         return Integer.parseInt(path, "sound_variant/".length(), end, 10);
      } catch (NumberFormatException e) {
         return -1;
      }
   }

   @Nullable
   public static class_2960 base(class_2960 variant) {
      String path = variant.method_12832();
      int indexEnd = path.indexOf(47, "sound_variant/".length());
      if (indexEnd < 0) {
         return null;
      }

      int namespaceEnd = path.indexOf(47, indexEnd + 1);
      return namespaceEnd < 0 ? null : class_2960.method_43902(path.substring(indexEnd + 1, namespaceEnd), path.substring(namespaceEnd + 1));
   }

   public static class_2960 unwrap(class_2960 location) {
      if (!isVariant(location)) {
         return location;
      }

      class_2960 base = base(location);
      return base != null ? base : location;
   }
}
