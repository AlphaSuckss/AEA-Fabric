package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.SavedLocations;
import java.util.Optional;
import net.minecraft.class_1297;

public final class HasLocationCondition implements ConditionType<EntityCtx, HasLocationCondition.Cfg> {
   @Override
   public MapCodec<HasLocationCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.STRING.optionalFieldOf("id").forGetter(HasLocationCondition.Cfg::id)).apply(i, HasLocationCondition.Cfg::new)
      );
   }

   public boolean test(HasLocationCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.raw();
      if (entity != null && !entity.method_37908().method_8608()) {
         SavedLocations locations = SavedLocations.of(entity);
         if (locations == null) {
            return false;
         } else {
            return cfg.id.isPresent() ? locations.has(entity.method_5667(), (String)cfg.id.get()) : !locations.ids(entity.method_5667()).isEmpty();
         }
      } else {
         return false;
      }
   }

   public record Cfg(Optional<String> id) {
   }
}
