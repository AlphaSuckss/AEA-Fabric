package dev.alphasucks.aea.port.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;
import java.util.Map;
import net.minecraft.class_1322.class_1323;

public enum AttributeModifierOperation {
   ADD_BASE_EARLY(AttributeModifierOperation.Phase.BASE, 0, (base, total, mod) -> new AttributeModifierOperation.Result(base + mod, total)),
   MULTIPLY_BASE_ADDITIVE(AttributeModifierOperation.Phase.BASE, 1, (base, total, mod) -> new AttributeModifierOperation.Result(base + base * mod, total)),
   MULTIPLY_BASE_MULTIPLICATIVE(
      AttributeModifierOperation.Phase.BASE, 2, (base, total, mod) -> new AttributeModifierOperation.Result(base * (1.0 + mod), total)
   ),
   STANDARD_MULTIPLY_BASE(AttributeModifierOperation.Phase.BASE, 3, (base, total, mod) -> new AttributeModifierOperation.Result(base * mod, total)),
   STANDARD_DIVIDE_BASE(
      AttributeModifierOperation.Phase.BASE, 4, (base, total, mod) -> new AttributeModifierOperation.Result(mod == 0.0 ? base : base / mod, total)
   ),
   ADD_BASE_LATE(AttributeModifierOperation.Phase.BASE, 5, (base, total, mod) -> new AttributeModifierOperation.Result(base + mod, total)),
   MIN_BASE(AttributeModifierOperation.Phase.BASE, 6, (base, total, mod) -> new AttributeModifierOperation.Result(Math.max(base, mod), total)),
   MAX_BASE(AttributeModifierOperation.Phase.BASE, 7, (base, total, mod) -> new AttributeModifierOperation.Result(Math.min(base, mod), total)),
   SET_BASE(AttributeModifierOperation.Phase.BASE, 8, (base, total, mod) -> new AttributeModifierOperation.Result(mod, total)),
   ADD_TOTAL_EARLY(AttributeModifierOperation.Phase.TOTAL, 0, (base, total, mod) -> new AttributeModifierOperation.Result(base, total + mod)),
   MULTIPLY_TOTAL_ADDITIVE(AttributeModifierOperation.Phase.TOTAL, 1, (base, total, mod) -> new AttributeModifierOperation.Result(base, total + total * mod)),
   MULTIPLY_TOTAL_MULTIPLICATIVE(
      AttributeModifierOperation.Phase.TOTAL, 2, (base, total, mod) -> new AttributeModifierOperation.Result(base, total * (1.0 + mod))
   ),
   STANDARD_MULTIPLY_TOTAL(AttributeModifierOperation.Phase.TOTAL, 3, (base, total, mod) -> new AttributeModifierOperation.Result(base, total * mod)),
   STANDARD_DIVIDE_TOTAL(
      AttributeModifierOperation.Phase.TOTAL, 4, (base, total, mod) -> new AttributeModifierOperation.Result(base, mod == 0.0 ? total : total / mod)
   ),
   MIN_TOTAL(AttributeModifierOperation.Phase.TOTAL, 5, (base, total, mod) -> new AttributeModifierOperation.Result(base, Math.max(total, mod))),
   MAX_TOTAL(AttributeModifierOperation.Phase.TOTAL, 6, (base, total, mod) -> new AttributeModifierOperation.Result(base, Math.min(total, mod))),
   SET_TOTAL(AttributeModifierOperation.Phase.TOTAL, 7, (base, total, mod) -> new AttributeModifierOperation.Result(base, mod)),
   ADD_TOTAL_LATE(AttributeModifierOperation.Phase.TOTAL, 8, (base, total, mod) -> new AttributeModifierOperation.Result(base, total + mod));

   private static final Map<String, AttributeModifierOperation> ALIASES = Map.of(
      "addition",
      ADD_BASE_EARLY,
      "multiply_base",
      MULTIPLY_BASE_ADDITIVE,
      "multiply_total",
      MULTIPLY_TOTAL_MULTIPLICATIVE,
      "add_value",
      ADD_BASE_EARLY,
      "add_multiplied_base",
      MULTIPLY_BASE_ADDITIVE,
      "add_multiplied_total",
      MULTIPLY_TOTAL_MULTIPLICATIVE,
      "ADD_VALUE",
      ADD_BASE_EARLY,
      "ADD_MULTIPLIED_BASE",
      MULTIPLY_BASE_ADDITIVE,
      "ADD_MULTIPLIED_TOTAL",
      MULTIPLY_TOTAL_MULTIPLICATIVE
   );
   public static final Codec<AttributeModifierOperation> CODEC = Codec.STRING.flatXmap(s -> {
      String name = s.startsWith("apoli:") ? s.substring("apoli:".length()) : s;
      AttributeModifierOperation v = (AttributeModifierOperation)ALIASES.get(name);
      if (v != null) {
         return DataResult.success(v);
      }

      for (AttributeModifierOperation op : values()) {
         if (op.serializedName().equals(name)) {
            return DataResult.success(op);
         }
      }

      return DataResult.error(() -> "Unknown attribute modifier operation: " + s);
   }, op -> DataResult.success(op.serializedName()));
   private final AttributeModifierOperation.Phase phase;
   private final int order;
   private final AttributeModifierOperation.Apply applyFn;

   AttributeModifierOperation(AttributeModifierOperation.Phase phase, int order, AttributeModifierOperation.Apply apply) {
      this.phase = phase;
      this.order = order;
      this.applyFn = apply;
   }

   public AttributeModifierOperation.Phase phase() {
      return this.phase;
   }

   public int order() {
      return this.order;
   }

   public String serializedName() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public AttributeModifierOperation.Result apply(double base, double total, double modifier) {
      return this.applyFn.apply(base, total, modifier);
   }

   public double applySingle(double value, double modifier) {
      AttributeModifierOperation.Result r = this.applyFn.apply(value, value, modifier);
      return this.phase == AttributeModifierOperation.Phase.BASE ? r.base() : r.total();
   }

   public boolean bounds() {
      return switch (this) {
         case MIN_BASE, MAX_BASE, SET_BASE, MIN_TOTAL, MAX_TOTAL, SET_TOTAL -> true;
         default -> false;
      };
   }

   public class_1323 vanillaOperation() {
      return switch (this) {
         case ADD_BASE_EARLY, ADD_BASE_LATE, SET_BASE, ADD_TOTAL_EARLY, ADD_TOTAL_LATE -> class_1323.field_6328;
         case MULTIPLY_BASE_ADDITIVE, MULTIPLY_BASE_MULTIPLICATIVE, STANDARD_MULTIPLY_BASE, STANDARD_DIVIDE_BASE, MIN_BASE, MAX_BASE -> class_1323.field_6330;
         case MULTIPLY_TOTAL_ADDITIVE, MULTIPLY_TOTAL_MULTIPLICATIVE, STANDARD_MULTIPLY_TOTAL, STANDARD_DIVIDE_TOTAL, MIN_TOTAL, MAX_TOTAL, SET_TOTAL -> class_1323.field_6331;
      };
   }

   @FunctionalInterface
   public interface Apply {
      AttributeModifierOperation.Result apply(double var1, double var3, double var5);
   }

   public enum Phase {
      BASE,
      TOTAL;
   }

   public record Result(double base, double total) {
   }
}
