package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.disguise.DisguiseManager;
import dev.alphasucks.aea.port.shared.EmptyCfg;

public final class RemoveDisguiseAction implements ActionType<EntityCtx, EmptyCfg> {
   @Override
   public MapCodec<EmptyCfg> codec() {
      return MapCodec.unit(EmptyCfg.INSTANCE);
   }

   public void run(EmptyCfg cfg, EntityCtx ctx) {
      DisguiseManager.remove(ctx.entity());
   }
}
