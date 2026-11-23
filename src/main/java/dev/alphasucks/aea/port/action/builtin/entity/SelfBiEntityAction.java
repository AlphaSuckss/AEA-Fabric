package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Map;
import net.minecraft.class_1297;

public final class SelfBiEntityAction implements ActionType<EntityCtx, SelfBiEntityAction.Cfg> {
   @Override
   public MapCodec<SelfBiEntityAction.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(BiEntityAction.CODEC.fieldOf("bientity_action").forGetter(SelfBiEntityAction.Cfg::bientityAction))
               .apply(i, SelfBiEntityAction.Cfg::new)
         ),
         Map.of("action", "bientity_action")
      );
   }

   public void run(SelfBiEntityAction.Cfg cfg, EntityCtx ctx) {
      class_1297 self = ctx.raw();
      if (self != null) {
         cfg.bientityAction.run(new BiEntityCtx(self, self, ctx.level()));
      }
   }

   public record Cfg(BiEntityAction bientityAction) {
   }
}
