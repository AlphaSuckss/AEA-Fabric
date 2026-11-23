package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.List;
import net.minecraft.class_2960;

public final class UnsuppressPowerAction implements ActionType<EntityCtx, UnsuppressPowerAction.Cfg> {
   @Override
   public MapCodec<UnsuppressPowerAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               SingleOrList.of(IdCodecs.ID).optionalFieldOf("power", List.of()).forGetter(UnsuppressPowerAction.Cfg::powers),
               SingleOrList.of(Codec.STRING).optionalFieldOf("tags", List.of()).forGetter(UnsuppressPowerAction.Cfg::tags),
               SingleOrList.of(IdCodecs.ID).optionalFieldOf("source", SuppressPowerAction.DEFAULT_SOURCES).forGetter(UnsuppressPowerAction.Cfg::sources)
            )
            .apply(i, UnsuppressPowerAction.Cfg::new)
      );
   }

   public void run(UnsuppressPowerAction.Cfg cfg, EntityCtx ctx) {
      PowerContainer holder = PowerContainer.of(ctx.entity());
      if (holder != null) {
         for (class_2960 power : cfg.powers) {
            apply(holder, power, cfg.sources);
         }

         for (String tag : cfg.tags) {
            for (class_2960 power : ApoliPowers.withTag(tag)) {
               if (holder.hasPower(power)) {
                  apply(holder, power, cfg.sources);
               }
            }
         }
      }
   }

   private static void apply(PowerContainer holder, class_2960 power, List<class_2960> sources) {
      for (class_2960 source : sources) {
         holder.unsuppressPower(power, source);
      }
   }

   public record Cfg(List<class_2960> powers, List<String> tags, List<class_2960> sources) {
   }
}
