package dev.alphasucks.aea.port.condition.builtin.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import java.util.Map;
import net.minecraft.class_1309;

public final class HolderItemCondition implements ConditionType<ItemCtx, HolderItemCondition.Cfg> {
   @Override
   public MapCodec<HolderItemCondition.Cfg> codec() {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(EntityCondition.CODEC.fieldOf("entity_condition").forGetter(HolderItemCondition.Cfg::entityCondition))
               .apply(i, HolderItemCondition.Cfg::new)
         ),
         Map.of("condition", "entity_condition")
      );
   }

   public boolean test(HolderItemCondition.Cfg cfg, ItemCtx ctx) {
      class_1309 holder = ctx.holder();
      return holder != null && cfg.entityCondition.test(EntityCtx.of(holder, ctx.level()));
   }

   public record Cfg(EntityCondition entityCondition) {
   }
}
