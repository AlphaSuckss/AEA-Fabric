package dev.alphasucks.aea.port.action.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.EntitySetPower;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_2960;

public final class AddToEntitySetAction implements ActionType<BiEntityCtx, AddToEntitySetAction.Cfg> {
   @Override
   public MapCodec<AddToEntitySetAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("set").forGetter(AddToEntitySetAction.Cfg::set),
               Codec.INT.optionalFieldOf("time_limit").forGetter(AddToEntitySetAction.Cfg::timeLimit)
            )
            .apply(i, AddToEntitySetAction.Cfg::new)
      );
   }

   public void run(AddToEntitySetAction.Cfg cfg, BiEntityCtx ctx) {
      if (ctx.actor() != null && ctx.target() != null) {
         PowerContainer container = PowerContainer.of(ctx.actor());
         if (container != null && container.hasPower(cfg.set)) {
            EntitySetPower.Cfg setCfg = EntitySetPower.resolveCfg(cfg.set);
            if (setCfg != null) {
               OptionalInt limit = (OptionalInt)cfg.timeLimit.map(OptionalInt::of).orElse(OptionalInt.empty());
               EntitySetPower.add(ctx.actor(), cfg.set, setCfg, ctx.target(), limit);
            }
         }
      }
   }

   public record Cfg(class_2960 set, Optional<Integer> timeLimit) {
   }
}
