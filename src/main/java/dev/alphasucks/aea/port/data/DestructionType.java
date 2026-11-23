package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;
import net.minecraft.class_1927.class_4179;

public enum DestructionType implements class_3542 {
   BREAK("break", class_4179.field_18687),
   NONE("none", class_4179.field_40878),
   DESTROY("destroy", class_4179.field_40879);

   public static final Codec<DestructionType> CODEC = class_3542.method_28140(DestructionType::values);
   private final String name;
   private final class_4179 vanilla;

   DestructionType(String name, class_4179 vanilla) {
      this.name = name;
      this.vanilla = vanilla;
   }

   public class_4179 vanilla() {
      return this.vanilla;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
