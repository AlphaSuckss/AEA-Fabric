package dev.alphasucks.aea.port.condition.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.TeamData;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_270;

public final class InTeamCondition implements ConditionType<EntityCtx, InTeamCondition.Cfg> {
   @Override
   public MapCodec<InTeamCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TeamData.CODEC.optionalFieldOf("team").forGetter(InTeamCondition.Cfg::team),
               TeamData.LIST_OR_SINGLE.optionalFieldOf("teams", List.of()).forGetter(InTeamCondition.Cfg::teams)
            )
            .apply(i, InTeamCondition.Cfg::new)
      );
   }

   public boolean test(InTeamCondition.Cfg cfg, EntityCtx ctx) {
      class_270 team = ctx.entity().method_5781();
      if (team == null) {
         return false;
      }

      if (cfg.team.isEmpty() && cfg.teams.isEmpty()) {
         return true;
      }

      if (cfg.team.isPresent() && ((TeamData)cfg.team.get()).matches(team)) {
         return true;
      }

      for (int i = 0; i < cfg.teams.size(); i++) {
         if (((TeamData)cfg.teams.get(i)).matches(team)) {
            return true;
         }
      }

      return false;
   }

   public record Cfg(Optional<TeamData> team, List<TeamData> teams) {
   }
}
