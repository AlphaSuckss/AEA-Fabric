package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class SimplePower extends PowerType<EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> configCodec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }
}
