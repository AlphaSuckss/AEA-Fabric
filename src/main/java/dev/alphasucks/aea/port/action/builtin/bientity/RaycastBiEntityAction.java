package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.builtin.entity.RaycastAction;
import dev.alphasucks.aea.port.action.builtin.entity.RaycastAction.Cfg;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;

public final class RaycastBiEntityAction implements ActionType<BiEntityCtx, Cfg> {
   @Override
   public MapCodec<Cfg> codec() {
      return RaycastAction.CONFIG_CODEC;
   }

   public void run(Cfg cfg, BiEntityCtx ctx) {
      RaycastAction.runTowards(cfg, ctx.asActor(), ctx.target());
   }
}
