package dev.alphasucks.aea.port.tick;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum TickScope implements class_3542 {
   ENTITY("entity"),
   CHUNK("chunk"),
   DIMENSION("dimension"),
   SERVER("server");

   public static final Codec<TickScope> CODEC = class_3542.method_28140(TickScope::values);
   private final String name;

   TickScope(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
