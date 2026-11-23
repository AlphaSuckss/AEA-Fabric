package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.CameraPerspective;
import dev.alphasucks.aea.port.entity.CameraPerspectives;
import net.minecraft.class_1657;

public final class PerspectiveCondition implements ConditionType<EntityCtx, PerspectiveCondition.Cfg> {
   @Override
   public MapCodec<PerspectiveCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(CameraPerspective.MASK_CODEC.fieldOf("perspective").forGetter(PerspectiveCondition.Cfg::perspective))
            .apply(i, PerspectiveCondition.Cfg::new)
      );
   }

   public boolean test(PerspectiveCondition.Cfg cfg, EntityCtx ctx) {
      return ctx.entity() instanceof class_1657 player && CameraPerspectives.typeOf(player).matches(cfg.perspective);
   }

   public record Cfg(int perspective) {
   }
}
