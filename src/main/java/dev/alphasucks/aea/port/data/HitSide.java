package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum HitSide implements class_3542 {
   SELF("self"),
   ATTACKER("attacker"),
   TARGET("target");

   public static final Codec<HitSide> CODEC = class_3542.method_28140(HitSide::values);
   private final String name;

   HitSide(String name) {
      this.name = name;
   }

   @Override
   public String method_15434() {
      return this.name;
   }
}
