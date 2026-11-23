package dev.alphasucks.aea.port.data.expr;

public final class ExprContext {
   private static final int CAPACITY = 32;
   private static final String[] NAMES = new String[32];
   private static int registered;
   private static final ThreadLocal<double[]> FRAME = ThreadLocal.withInitial(() -> new double[32]);

   private ExprContext() {
   }

   public static synchronized int slot(String name) {
      for (int i = 0; i < registered; i++) {
         if (NAMES[i].equals(name)) {
            return i;
         }
      }

      if (registered >= 32) {
         throw new IllegalStateException("Too many expression context variables; raise ExprContext.CAPACITY");
      }

      NAMES[registered] = name;
      return registered++;
   }

   public static double get(int slot) {
      return ((double[])FRAME.get())[slot];
   }

   public static double push(int slot, double value) {
      double[] frame = (double[])FRAME.get();
      double previous = frame[slot];
      frame[slot] = value;
      return previous;
   }

   public static void pop(int slot, double previous) {
      ((double[])FRAME.get())[slot] = previous;
   }
}
