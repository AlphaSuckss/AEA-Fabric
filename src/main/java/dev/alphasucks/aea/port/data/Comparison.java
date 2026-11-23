package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import net.minecraft.class_3542;

public enum Comparison implements class_3542 {
   LESS("<", (a, b) -> a < b),
   LESS_EQUAL("<=", (a, b) -> a <= b),
   GREATER(">", (a, b) -> a > b),
   GREATER_EQUAL(">=", (a, b) -> a >= b),
   EQUAL("==", (a, b) -> a == b),
   NOT_EQUAL("!=", (a, b) -> a != b);

   public static final Codec<Comparison> CODEC = class_3542.method_28140(Comparison::values);
   private final String symbol;
   private final Comparison.DoubleComparator op;

   Comparison(String symbol, Comparison.DoubleComparator op) {
      this.symbol = symbol;
      this.op = op;
   }

   public boolean compare(double a, double b) {
      return this.op.test(a, b);
   }

   @Override
   public String method_15434() {
      return this.symbol;
   }

   @FunctionalInterface
   private interface DoubleComparator {
      boolean test(double var1, double var3);
   }
}
