package dev.alphasucks.aea.port.action.builtin.item;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ScriptRef;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;

public final class ScriptItemAction implements ActionType<ItemCtx, ScriptRef> {
   @Override
   public MapCodec<ScriptRef> codec() {
      return ScriptRef.CODEC;
   }

   public void run(ScriptRef cfg, ItemCtx ctx) {
      if (ctx.level() != null && !ctx.level().method_8608()) {
         ApoliScripts.run(Kind.ITEM_ACTION, cfg.script(), ScriptCtx.of(ctx, cfg.params()));
      }
   }
}
