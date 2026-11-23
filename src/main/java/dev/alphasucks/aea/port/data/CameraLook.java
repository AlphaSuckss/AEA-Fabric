package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum CameraLook implements class_3542 {
   ANCHOR("anchor"),
   HOLDER("holder"),
   FIXED("fixed"),
   AT_HOLDER("at_holder"),
   AT_ANCHOR("at_anchor");

   public static final Codec<CameraLook> CODEC = class_3542.method_28140(CameraLook::values);
   private final String name;

   CameraLook(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }

   public boolean looksAt() {
      return this == AT_HOLDER || this == AT_ANCHOR;
   }
}
