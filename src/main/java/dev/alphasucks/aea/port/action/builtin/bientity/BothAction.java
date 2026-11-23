package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;

public final class BothAction implements ActionType<BiEntityCtx, BothAction.Cfg> {
   @Override
   public MapCodec<BothAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(i -> i.group(EntityAction.CODEC.fieldOf("action").forGetter(BothAction.Cfg::action)).apply(i, BothAction.Cfg::new));
   }

   public void run(BothAction.Cfg cfg, BiEntityCtx ctx) {
      cfg.action.run(ctx.asActor());
      cfg.action.run(ctx.asTarget());
   }

   public record Cfg(EntityAction action) {
   }
}
