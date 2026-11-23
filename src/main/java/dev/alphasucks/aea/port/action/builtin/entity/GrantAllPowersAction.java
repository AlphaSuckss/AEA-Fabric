package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerSources;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_2960;

public final class GrantAllPowersAction implements ActionType<EntityCtx, GrantAllPowersAction.Cfg> {
   public static final class_2960 DEFAULT_SOURCE = Apoli.id("grant_all_powers");

   @Override
   public MapCodec<GrantAllPowersAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("source").forGetter(GrantAllPowersAction.Cfg::source),
               Codec.STRING.optionalFieldOf("namespace").forGetter(GrantAllPowersAction.Cfg::namespace),
               Codec.BOOL.optionalFieldOf("include_hidden", true).forGetter(GrantAllPowersAction.Cfg::includeHidden),
               SingleOrList.of(IdCodecs.ID).optionalFieldOf("from", List.of()).forGetter(GrantAllPowersAction.Cfg::from)
            )
            .apply(i, GrantAllPowersAction.Cfg::new)
      );
   }

   public void run(GrantAllPowersAction.Cfg cfg, EntityCtx ctx) {
      PowerContainer holder = PowerContainerAttachment.getOrCreate(ctx.entity());
      if (holder != null) {
         if (cfg.from.isEmpty()) {
            class_2960 source = (class_2960)cfg.source.orElse(DEFAULT_SOURCE);

            for (class_2960 id : ApoliPowers.grantableIds()) {
               grant(cfg, holder, id, source);
            }
         } else {
            for (class_2960 from : cfg.from) {
               Set<class_2960> powers = PowerSources.powersOf(from);
               if (powers == null) {
                  Apoli.LOGGER.warn("[Apoli] grant_all_powers: '{}' is not a known power source (origin, skill tree or apoli:multiple power).", from);
               } else {
                  class_2960 source = (class_2960)cfg.source.orElse(from);

                  for (class_2960 id : powers) {
                     grant(cfg, holder, id, source);
                  }
               }
            }
         }
      }
   }

   private static void grant(GrantAllPowersAction.Cfg cfg, PowerContainer holder, class_2960 id, class_2960 source) {
      if (!cfg.namespace.isPresent() || ((String)cfg.namespace.get()).equals(id.method_12836())) {
         if (!cfg.includeHidden) {
            Power p = ApoliPowers.get(id);
            if (p != null && p.hidden()) {
               return;
            }
         }

         holder.addPower(id, source);
      }
   }

   public record Cfg(Optional<class_2960> source, Optional<String> namespace, boolean includeHidden, List<class_2960> from) {
   }
}
