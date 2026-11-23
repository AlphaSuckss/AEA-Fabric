package dev.alphasucks.aea.port.action.builtin.meta;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class NothingMetaAction<CTX> implements ActionType<CTX, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public void run(EmptyCfg cfg, CTX ctx) {
   }
}
