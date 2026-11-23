package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum ProcessMode implements class_3542 {
   ITEMS("items"),
   STACKS("stacks");

   public static final Codec<ProcessMode> CODEC = class_3542.method_28140(ProcessMode::values);
   private final String name;

   ProcessMode(String n) {
      this.name = n;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
