package dev.alphasucks.aea.port.action.builtin.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.ConnectedBlocks;
import dev.alphasucks.aea.port.data.expr.ExprContext;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;

public final class ConnectedBlocksBlockMetaAction implements ActionType<BlockCtx, ConnectedBlocksBlockMetaAction.Cfg> {
   private static final int SLOT_COUNT = ExprContext.slot("count");
   private static final int SLOT_INDEX = ExprContext.slot("index");

   @Override
   public MapCodec<ConnectedBlocksBlockMetaAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ConnectedBlocks.MAP_CODEC.forGetter(ConnectedBlocksBlockMetaAction.Cfg::group),
               BlockAction.CODEC.fieldOf("block_action").forGetter(ConnectedBlocksBlockMetaAction.Cfg::blockAction),
               LoggedOptionalField.strict("actor_condition", EntityCondition.CODEC).forGetter(ConnectedBlocksBlockMetaAction.Cfg::actorCondition)
            )
            .apply(i, ConnectedBlocksBlockMetaAction.Cfg::new)
      );
   }

   public void run(ConnectedBlocksBlockMetaAction.Cfg cfg, BlockCtx ctx) {
      class_1297 actor = ctx.actor();
      if (!cfg.actorCondition.isPresent() || actor != null) {
         class_1937 level = ctx.level();
         List<class_2338> blocks = cfg.group.find(level, ctx.pos(), ctx.state(), actor);
         if (!blocks.isEmpty()) {
            EntityCtx actorCtx = cfg.actorCondition.isPresent() ? EntityCtx.of(actor, level) : null;
            double previousCount = ExprContext.push(SLOT_COUNT, blocks.size());
            double previousIndex = ExprContext.get(SLOT_INDEX);

            try {
               for (int i = 0; i < blocks.size() && (actorCtx == null || ((EntityCondition)cfg.actorCondition.get()).test(actorCtx)); i++) {
                  ExprContext.push(SLOT_INDEX, i);
                  class_2338 pos = (class_2338)blocks.get(i);
                  cfg.blockAction.run(ctx.at(pos, level.method_8320(pos)));
               }
            } finally {
               ExprContext.pop(SLOT_INDEX, previousIndex);
               ExprContext.pop(SLOT_COUNT, previousCount);
            }
         }
      }
   }

   public record Cfg(ConnectedBlocks group, BlockAction blockAction, Optional<EntityCondition> actorCondition) {
   }
}
