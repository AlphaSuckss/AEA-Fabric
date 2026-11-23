package dev.alphasucks.aea.port.condition.context;

public record StaticCtx() {
   public static final StaticCtx INSTANCE = new StaticCtx();
}
