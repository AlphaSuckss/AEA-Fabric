package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ScriptRef;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;

public final class ScriptBiEntityCondition implements ConditionType<BiEntityCtx, ScriptRef> {
   @Override
   public MapCodec<ScriptRef> codec() {
      return ScriptRef.CODEC;
   }

   public boolean test(ScriptRef cfg, BiEntityCtx ctx) {
      return ApoliScripts.test(Kind.BIENTITY_CONDITION, cfg.script(), ScriptCtx.of(ctx, cfg.params()), false);
   }
}
