package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ScriptRef;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;

public final class ScriptAction implements ActionType<EntityCtx, ScriptRef> {
   @Override
   public MapCodec<ScriptRef> codec() {
      return ScriptRef.CODEC;
   }

   public void run(ScriptRef cfg, EntityCtx ctx) {
      if (ctx.level() != null && !ctx.level().method_8608()) {
         ApoliScripts.run(Kind.ENTITY_ACTION, cfg.script(), ScriptCtx.of(ctx, cfg.params()));
      }
   }
}
