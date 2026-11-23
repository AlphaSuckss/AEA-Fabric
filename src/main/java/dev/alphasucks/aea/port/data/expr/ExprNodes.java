package dev.alphasucks.aea.port.data.expr;

import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import org.jetbrains.annotations.Nullable;

public final class ExprNodes {
   private ExprNodes() {
   }

   private static boolean allConstant(ExprNode[] args) {
      for (ExprNode arg : args) {
         if (!arg.isConstant()) {
            return false;
         }
      }

      return true;
   }

   public record AndN(ExprNode[] args) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         for (ExprNode arg : this.args) {
            if (arg.eval(entity, container, level, value) == 0.0) {
               return 0.0;
            }
         }

         return 1.0;
      }

      @Override
      public boolean isConstant() {
         return ExprNodes.allConstant(this.args);
      }
   }

   public record AvgN(ExprNode[] args) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double sum = 0.0;

         for (ExprNode arg : this.args) {
            sum += arg.eval(entity, container, level, value);
         }

         return sum / this.args.length;
      }

      @Override
      public boolean isConstant() {
         return ExprNodes.allConstant(this.args);
      }
   }

   public record Bin(ExprNodes.Op op, ExprNode l, ExprNode r) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double a = this.l.eval(entity, container, level, value);
         double b = this.r.eval(entity, container, level, value);

         return switch (this.op) {
            case ADD -> a + b;
            case SUB -> a - b;
            case MUL -> a * b;
            case DIV -> a / b;
            case IDIV -> (long)(a / b);
            case MOD -> a % b;
            case POW -> Math.pow(a, b);
            case EQ -> a == b ? 1.0 : 0.0;
            case NE -> a != b ? 1.0 : 0.0;
            case LT -> a < b ? 1.0 : 0.0;
            case LE -> a <= b ? 1.0 : 0.0;
            case GT -> a > b ? 1.0 : 0.0;
            case GE -> a >= b ? 1.0 : 0.0;
         };
      }

      @Override
      public boolean isConstant() {
         return this.l.isConstant() && this.r.isConstant();
      }
   }

   public record Const(double v) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.v;
      }

      @Override
      public boolean isConstant() {
         return true;
      }
   }

   @FunctionalInterface
   public interface DoubleTernaryOperator {
      double applyAsDouble(double var1, double var3, double var5);
   }

   public record Fact(ExprNode n) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double v = this.n.eval(entity, container, level, value);
         long k = Math.round(v);
         if (k >= 0L && !(Math.abs(v - k) > 1.0E-9)) {
            double result = 1.0;

            for (long i = 2L; i <= Math.min(k, 171L); i++) {
               result *= i;
            }

            return k > 170L ? Double.POSITIVE_INFINITY : result;
         } else {
            return Double.NaN;
         }
      }

      @Override
      public boolean isConstant() {
         return this.n.isConstant();
      }
   }

   public record Fn1(DoubleUnaryOperator fn, ExprNode a) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.fn.applyAsDouble(this.a.eval(entity, container, level, value));
      }

      @Override
      public boolean isConstant() {
         return this.a.isConstant();
      }
   }

   public record Fn2(DoubleBinaryOperator fn, ExprNode a, ExprNode b) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.fn.applyAsDouble(this.a.eval(entity, container, level, value), this.b.eval(entity, container, level, value));
      }

      @Override
      public boolean isConstant() {
         return this.a.isConstant() && this.b.isConstant();
      }
   }

   public record Fn3(ExprNodes.DoubleTernaryOperator fn, ExprNode a, ExprNode b, ExprNode c) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.fn
            .applyAsDouble(
               this.a.eval(entity, container, level, value), this.b.eval(entity, container, level, value), this.c.eval(entity, container, level, value)
            );
      }

      @Override
      public boolean isConstant() {
         return this.a.isConstant() && this.b.isConstant() && this.c.isConstant();
      }
   }

   public record If(ExprNode cond, ExprNode then, ExprNode otherwise) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.cond.eval(entity, container, level, value) != 0.0
            ? this.then.eval(entity, container, level, value)
            : this.otherwise.eval(entity, container, level, value);
      }

      @Override
      public boolean isConstant() {
         return this.cond.isConstant() && this.then.isConstant() && this.otherwise.isConstant();
      }
   }

   public record MaxN(ExprNode[] args) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double best = this.args[0].eval(entity, container, level, value);

         for (int i = 1; i < this.args.length; i++) {
            best = Math.max(best, this.args[i].eval(entity, container, level, value));
         }

         return best;
      }

      @Override
      public boolean isConstant() {
         return ExprNodes.allConstant(this.args);
      }
   }

   public record MinN(ExprNode[] args) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double best = this.args[0].eval(entity, container, level, value);

         for (int i = 1; i < this.args.length; i++) {
            best = Math.min(best, this.args[i].eval(entity, container, level, value));
         }

         return best;
      }

      @Override
      public boolean isConstant() {
         return ExprNodes.allConstant(this.args);
      }
   }

   public record Neg(ExprNode n) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return -this.n.eval(entity, container, level, value);
      }

      @Override
      public boolean isConstant() {
         return this.n.isConstant();
      }
   }

   public enum Op {
      ADD,
      SUB,
      MUL,
      DIV,
      IDIV,
      MOD,
      POW,
      EQ,
      NE,
      LT,
      LE,
      GT,
      GE;
   }

   public record OrN(ExprNode[] args) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         for (ExprNode arg : this.args) {
            if (arg.eval(entity, container, level, value) != 0.0) {
               return 1.0;
            }
         }

         return 0.0;
      }

      @Override
      public boolean isConstant() {
         return ExprNodes.allConstant(this.args);
      }
   }

   public record RList(ExprNode[] args) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.args[ThreadLocalRandom.current().nextInt(this.args.length)].eval(entity, container, level, value);
      }
   }

   public record RNor(ExprNode mean, ExprNode stddev) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double mu = this.mean.eval(entity, container, level, value);
         double sigma = this.stddev.eval(entity, container, level, value);
         return mu + ThreadLocalRandom.current().nextGaussian() * sigma;
      }
   }

   public record RUni(ExprNode lo, ExprNode hi) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double a = this.lo.eval(entity, container, level, value);
         double b = this.hi.eval(entity, container, level, value);
         if (a > b) {
            double t = a;
            a = b;
            b = t;
         }

         return a == b ? a : a + ThreadLocalRandom.current().nextDouble() * (b - a);
      }
   }

   public record RUnid(ExprNode lo, ExprNode hi) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         long a = Math.round(this.lo.eval(entity, container, level, value));
         long b = Math.round(this.hi.eval(entity, container, level, value));
         if (a > b) {
            long t = a;
            a = b;
            b = t;
         }

         return a == b ? a : ThreadLocalRandom.current().nextLong(a, b + 1L);
      }
   }

   public record Tetra(ExprNode l, ExprNode r) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         double a = this.l.eval(entity, container, level, value);
         long b = Math.round(this.r.eval(entity, container, level, value));
         if (b < 0L) {
            return Double.NaN;
         }

         if (b == 0L) {
            return 1.0;
         }

         double result = a;
         long steps = Math.min(b, 64L) - 1L;

         for (long i = 0L; i < steps && Double.isFinite(result); i++) {
            result = Math.pow(a, result);
         }

         return result;
      }

      @Override
      public boolean isConstant() {
         return this.l.isConstant() && this.r.isConstant();
      }
   }

   public record Var(ExprVar accessor) implements ExprNode {
      @Override
      public double eval(@Nullable class_1297 entity, @Nullable PowerContainer container, @Nullable class_1937 level, double value) {
         return this.accessor.get(entity, container, level, value);
      }
   }
}
