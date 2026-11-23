package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerSources;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2960;

public final class RevokeAllPowersAction implements ActionType<EntityCtx, RevokeAllPowersAction.Cfg> {
   @Override
   public MapCodec<RevokeAllPowersAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(SingleOrList.of(IdCodecs.ID).fieldOf("source").forGetter(RevokeAllPowersAction.Cfg::sources)).apply(i, RevokeAllPowersAction.Cfg::new)
      );
   }

   public void run(RevokeAllPowersAction.Cfg cfg, EntityCtx ctx) {
      PowerContainer holder = PowerContainer.of(ctx.entity());
      if (holder != null) {
         for (class_2960 source : cfg.sources) {
            holder.removeAllFromSource(source);
            Set<class_2960> provided = PowerSources.powersOf(source);
            if (provided != null) {
               for (class_2960 power : provided) {
                  holder.removePowerCompletely(power);
               }
            }
         }
      }
   }

   public record Cfg(List<class_2960> sources) {
   }
}
