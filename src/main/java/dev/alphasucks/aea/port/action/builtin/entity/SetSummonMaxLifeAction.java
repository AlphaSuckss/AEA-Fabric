package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.entity.summon.Temporary;
import java.util.Optional;
import net.minecraft.class_2960;

public final class SetSummonMaxLifeAction implements ActionType<EntityCtx, SetSummonMaxLifeAction.Cfg> {
   @Override
   public MapCodec<SetSummonMaxLifeAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.INT.fieldOf("amount").forGetter(SetSummonMaxLifeAction.Cfg::amount),
               IdCodecs.ID.optionalFieldOf("summon_id").forGetter(SetSummonMaxLifeAction.Cfg::summonId)
            )
            .apply(i, SetSummonMaxLifeAction.Cfg::new)
      );
   }

   public void run(SetSummonMaxLifeAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.raw() instanceof Temporary summon) {
         if (!cfg.summonId.isPresent() || ((class_2960)cfg.summonId.get()).equals(summon.getSummonId())) {
            summon.setMaxLifeTime(cfg.amount);
         }
      }
   }

   public record Cfg(int amount, Optional<class_2960> summonId) {
   }
}
