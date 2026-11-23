package dev.alphasucks.aea.port.power;

import net.minecraft.class_2960;

public final class PowerTypeGate {
   private final class_2960 canonicalType;
   private volatile int cached = -2;

   public PowerTypeGate(class_2960 canonicalType) {
      this.canonicalType = canonicalType;
   }

   public boolean inUse() {
      int generation = ApoliPowers.generation();
      int cached = this.cached;
      if (cached >> 1 == generation) {
         return (cached & 1) != 0;
      }

      boolean inUse = ApoliPowers.anyOfType(this.canonicalType);
      this.cached = generation << 1 | (inUse ? 1 : 0);
      return inUse;
   }
}
