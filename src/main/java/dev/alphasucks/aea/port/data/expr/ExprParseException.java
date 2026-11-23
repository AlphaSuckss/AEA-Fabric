package dev.alphasucks.aea.port.data.expr;

public final class ExprParseException extends Exception {
   public ExprParseException(String message, int position) {
      super(message + " (at index " + position + ")");
   }
}
