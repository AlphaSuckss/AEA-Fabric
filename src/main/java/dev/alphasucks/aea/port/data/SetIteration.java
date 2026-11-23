package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum SetIteration implements class_3542 {
   MEMBERS("members"),
   OWNERS("owners");

   public static final Codec<SetIteration> CODEC = class_3542.method_28140(SetIteration::values);
   private final String name;

   SetIteration(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
