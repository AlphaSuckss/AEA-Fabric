package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import net.minecraft.class_2960;

public final class InEntitySetCondition implements ConditionType<BiEntityCtx, InEntitySetCondition.Cfg> {
   @Override
   public MapCodec<InEntitySetCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.fieldOf("set").forGetter(InEntitySetCondition.Cfg::set)).apply(i, InEntitySetCondition.Cfg::new)
      );
   }

   public boolean test(InEntitySetCondition.Cfg cfg, BiEntityCtx ctx) {
      if (ctx.actor() != null && ctx.target() != null) {
         PowerContainer container = PowerContainer.of(ctx.actor());
         return container != null && container.hasPower(cfg.set) ? EntitySetPower.contains(ctx.actor(), cfg.set, ctx.target()) : false;
      } else {
         return false;
      }
   }

   public record Cfg(class_2960 set) {
   }
}
