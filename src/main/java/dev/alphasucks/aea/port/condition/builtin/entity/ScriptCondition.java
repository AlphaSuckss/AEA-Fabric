package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ScriptRef;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;

public final class ScriptCondition implements ConditionType<EntityCtx, ScriptRef> {
   @Override
   public MapCodec<ScriptRef> codec() {
      return ScriptRef.CODEC;
   }

   public boolean test(ScriptRef cfg, EntityCtx ctx) {
      return ApoliScripts.test(Kind.ENTITY_CONDITION, cfg.script(), ScriptCtx.of(ctx, cfg.params()), false);
   }
}
