package dev.alphasucks.aea.port.action;

import com.mojang.serialization.MapCodec;

public interface ActionType<CTX, C> {
   MapCodec<C> codec();

   void run(C var1, CTX var2);
}
