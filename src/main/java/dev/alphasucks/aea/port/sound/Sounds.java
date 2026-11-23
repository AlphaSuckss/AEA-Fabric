package dev.alphasucks.aea.port.sound;

import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_7923;

public final class Sounds {
   private Sounds() {
   }

   public static class_3414 event(class_2960 id) {
      class_3414 registered = class_7923.field_41172.method_10223(id);
      return registered != null ? registered : class_3414.method_47908(id);
   }

   public static class_3414 variant(class_2960 id, int index) {
      class_3414 base = event(id);
      class_2960 variant = SoundVariants.encode(id, index);
      float range = base.method_43044(1.0F);
      return range == base.method_43044(2.0F) ? class_3414.method_47909(variant, range) : class_3414.method_47908(variant);
   }
}
