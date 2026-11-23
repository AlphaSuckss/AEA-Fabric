package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.PlayerModelType;
import dev.alphasucks.aea.port.entity.PlayerModelTypes;
import net.minecraft.class_1657;

public final class PlayerModelTypeCondition implements ConditionType<EntityCtx, PlayerModelTypeCondition.Cfg> {
   @Override
   public MapCodec<PlayerModelTypeCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(PlayerModelType.CODEC.fieldOf("model_type").forGetter(PlayerModelTypeCondition.Cfg::modelType))
            .apply(i, PlayerModelTypeCondition.Cfg::new)
      );
   }

   public boolean test(PlayerModelTypeCondition.Cfg cfg, EntityCtx ctx) {
      return !(ctx.entity() instanceof class_1657) ? false : PlayerModelTypes.of(ctx.entity()) == cfg.modelType;
   }

   public record Cfg(PlayerModelType modelType) {
   }
}
