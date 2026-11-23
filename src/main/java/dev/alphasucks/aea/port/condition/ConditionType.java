package dev.alphasucks.aea.port.condition;

import com.mojang.serialization.MapCodec;

public interface ConditionType<CTX, C> {
   MapCodec<C> codec();

   boolean test(C var1, CTX var2);
}
