package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.alphasucks.aea.port.data.expr.ExprNode;
import dev.alphasucks.aea.port.data.expr.ExprParseException;
import dev.alphasucks.aea.port.data.expr.ExprParser;
import dev.alphasucks.aea.port.data.expr.ExprVars;
import dev.alphasucks.aea.port.data.expr.ExprNodes.Const;
import dev.alphasucks.aea.port.data.expr.ExprParser.Result;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import org.jetbrains.annotations.Nullable;

public final class Expression {
   public static final Codec<Expression> CODEC = Codec.STRING.comapFlatMap(Expression::tryOf, Expression::source);
   public static final Codec<Expression> INT_OR_EXPR = Codec.either(Codec.INT, CODEC)
      .xmap(
         either -> (Expression)either.map(Expression::constant, expr -> expr),
         expr -> expr.constantValue().isPresent() && expr.constantValue().getAsDouble() == (int)expr.constantValue().getAsDouble()
            ? Either.left((int)expr.constantValue().getAsDouble())
            : Either.right(expr)
      );
   public static final Codec<Expression> FLOAT_OR_EXPR = Codec.either(Codec.FLOAT, CODEC)
      .xmap(
         either -> (Expression)either.map(f -> constant(f.doubleValue()), expr -> expr),
         expr -> expr.constantValue().isPresent() ? Either.left((float)expr.constantValue().getAsDouble()) : Either.right(expr)
      );
   public static final Codec<Expression> DOUBLE_OR_EXPR = Codec.either(Codec.DOUBLE, CODEC)
      .xmap(
         either -> (Expression)either.map(Expression::constant, expr -> expr),
         expr -> expr.constantValue().isPresent() ? Either.left(expr.constantValue().getAsDouble()) : Either.right(expr)
      );
   private final String source;
   private final ExprNode root;
   private final boolean isConst;
   private final double constVal;
   private final boolean needsContainer;
   private final boolean needsPeer;
   private static final int CACHE_LIMIT = 256;
   private static final Map<String, Expression> CACHE = new ConcurrentHashMap();

   private Expression(String source, ExprNode root, boolean needsContainer, boolean needsPeer) {
      this.source = source;
      this.root = root;
      this.needsContainer = needsContainer;
      this.needsPeer = needsPeer;
      if (root instanceof Const c) {
         this.isConst = true;
         this.constVal = c.v();
      } else {
         this.isConst = false;
         this.constVal = 0.0;
      }
   }

   public static Expression of(String src) {
      if (src != null && !src.isBlank()) {
         try {
            Result result = ExprParser.parse(src, ExprVars::resolve);
            return new Expression(src, result.root(), result.needsContainer(), result.needsPeer());
         } catch (ExprParseException e) {
            throw new IllegalArgumentException("Invalid expression \"" + src + "\": " + e.getMessage());
         }
      } else {
         throw new IllegalArgumentException("Expression source must not be empty");
      }
   }

   private static DataResult<Expression> tryOf(String src) {
      try {
         return DataResult.success(of(src));
      } catch (IllegalArgumentException e) {
         return DataResult.error(e::getMessage);
      }
   }

   public static Expression cached(String src) {
      Expression hit = (Expression)CACHE.get(src);
      if (hit != null) {
         return hit;
      }

      Expression built;
      try {
         built = of(src);
      } catch (IllegalArgumentException e) {
         built = constant(0.0);
      }

      if (CACHE.size() < 256) {
         CACHE.putIfAbsent(src, built);
      }

      return built;
   }

   public static Expression constant(double value) {
      String repr = value == (long)value ? Long.toString((long)value) : Double.toString(value);
      return new Expression(repr, new Const(value), false, false);
   }

   public String source() {
      return this.source;
   }

   public OptionalDouble constantValue() {
      return this.isConst ? OptionalDouble.of(this.constVal) : OptionalDouble.empty();
   }

   public boolean needsContainer() {
      return this.needsContainer;
   }

   public boolean needsPeer() {
      return this.needsPeer;
   }

   public double eval(@Nullable class_1297 entity) {
      return this.isConst ? sanitize(this.constVal) : this.evalFull(entity, this.autoContainer(entity), null, 0.0);
   }

   public double eval(@Nullable class_1297 entity, double value) {
      return this.isConst ? sanitize(this.constVal) : this.evalFull(entity, this.autoContainer(entity), null, value);
   }

   public double evalWith(@Nullable class_1297 entity, @Nullable PowerContainer container, double value) {
      return this.isConst ? sanitize(this.constVal) : this.evalFull(entity, container, null, value);
   }

   public double eval(@Nullable class_1937 level) {
      return this.isConst ? sanitize(this.constVal) : this.evalFull(null, null, level, 0.0);
   }

   public int evalInt(@Nullable class_1297 entity) {
      return (int)Math.round(this.eval(entity));
   }

   public int evalInt(@Nullable class_1297 entity, double value) {
      return (int)Math.round(this.eval(entity, value));
   }

   public int evalIntWith(@Nullable class_1297 entity, @Nullable PowerContainer container, double value) {
      return (int)Math.round(this.evalWith(entity, container, value));
   }

   private double evalFull(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
      return sanitize(this.root.eval(entity, container, level, value));
   }

   private static double sanitize(double result) {
      return !Double.isNaN(result) && !Double.isInfinite(result) ? result : 0.0;
   }

   @Nullable
   private PowerContainer autoContainer(@Nullable class_1297 entity) {
      return this.needsContainer && entity != null ? PowerContainer.of(entity) : null;
   }

   public boolean equals(Object o) {
      return o instanceof Expression e && e.source.equals(this.source);
   }

   public int hashCode() {
      return this.source.hashCode();
   }

   public String toString() {
      return "Expression[" + this.source + "]";
   }
}
