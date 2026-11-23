package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1676;
import net.minecraft.class_6025;
import org.jetbrains.annotations.Nullable;

public final class OwnerCondition implements ConditionType<EntityCtx, OwnerCondition.Cfg> {
   @Override
   public MapCodec<OwnerCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(OwnerCondition.Cfg::bientityCondition))
            .apply(i, OwnerCondition.Cfg::new)
      );
   }

   public boolean test(OwnerCondition.Cfg cfg, EntityCtx ctx) {
      class_1297 owner = ownerOf(ctx.raw());
      if (owner == null) {
         return false;
      } else {
         return cfg.bientityCondition.isEmpty() ? true : ((BiEntityCondition)cfg.bientityCondition.get()).test(new BiEntityCtx(ctx.raw(), owner, ctx.level()));
      }
   }

   @Nullable
   private static class_1297 ownerOf(class_1297 entity) {
      if (entity instanceof class_6025 ownable) {
         return ownable.method_35057();
      } else {
         return entity instanceof class_1676 projectile ? projectile.method_24921() : null;
      }
   }

   public record Cfg(Optional<BiEntityCondition> bientityCondition) {
   }
}
