package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1297;

public final class HasCommandTagCondition implements ConditionType<EntityCtx, HasCommandTagCondition.Cfg> {
   @Override
   public MapCodec<HasCommandTagCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.STRING.optionalFieldOf("command_tag").forGetter(c -> Optional.empty()),
               Codec.STRING.listOf().optionalFieldOf("command_tags", List.of()).forGetter(HasCommandTagCondition.Cfg::commandTags)
            )
            .apply(i, (single, list) -> {
               if (single.isEmpty()) {
                  return new HasCommandTagCondition.Cfg(list);
               }

               List<String> merged = new ArrayList(list);
               merged.add((String)single.get());
               return new HasCommandTagCondition.Cfg(List.copyOf(merged));
            })
      );
   }

   public boolean test(HasCommandTagCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity == null) {
         return false;
      }

      Set<String> tags = entity.method_5752();
      if (cfg.commandTags.isEmpty()) {
         return !tags.isEmpty();
      }

      for (int i = 0; i < cfg.commandTags.size(); i++) {
         if (tags.contains(cfg.commandTags.get(i))) {
            return true;
         }
      }

      return false;
   }

   public record Cfg(List<String> commandTags) {
   }
}
