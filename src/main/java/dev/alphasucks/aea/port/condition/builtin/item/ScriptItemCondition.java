package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.MapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.script.ApoliScripts;
import dev.alphasucks.aea.port.script.ScriptCtx;
import dev.alphasucks.aea.port.script.ScriptRef;
import dev.alphasucks.aea.port.script.ApoliScripts.Kind;

public final class ScriptItemCondition implements ConditionType<ItemCtx, ScriptRef> {
   @Override
   public MapCodec<ScriptRef> codec() {
      return ScriptRef.CODEC;
   }

   public boolean test(ScriptRef cfg, ItemCtx ctx) {
      return ApoliScripts.test(Kind.ITEM_CONDITION, cfg.script(), ScriptCtx.of(ctx, cfg.params()), false);
   }
}
