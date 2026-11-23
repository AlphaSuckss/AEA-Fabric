package dev.alphasucks.aea.port.condition.builtin.block;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ScriptRef;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;

public final class ScriptBlockCondition implements ConditionType<BlockCtx, ScriptRef> {
   @Override
   public MapCodec<ScriptRef> codec() {
      return ScriptRef.CODEC;
   }

   public boolean test(ScriptRef cfg, BlockCtx ctx) {
      return ApoliScripts.test(Kind.BLOCK_CONDITION, cfg.script(), ScriptCtx.of(ctx, cfg.params()), false);
   }
}
