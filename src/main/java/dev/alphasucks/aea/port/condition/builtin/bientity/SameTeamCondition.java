package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import net.minecraft.class_1297;
import net.minecraft.class_270;

public final class SameTeamCondition implements ConditionType<BiEntityCtx, SameTeamCondition.Cfg> {
   @Override
   public MapCodec<SameTeamCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(Codec.BOOL.optionalFieldOf("allow_teamless", false).forGetter(SameTeamCondition.Cfg::allowTeamless)).apply(i, SameTeamCondition.Cfg::new)
      );
   }

   public boolean test(SameTeamCondition.Cfg cfg, BiEntityCtx ctx) {
      class_1297 actor = ctx.actor();
      class_1297 target = ctx.target();
      if (actor != null && target != null) {
         class_270 actorTeam = actor.method_5781();
         class_270 targetTeam = target.method_5781();
         return actorTeam != null && targetTeam != null ? actorTeam.method_1206(targetTeam) : cfg.allowTeamless && actorTeam == targetTeam;
      } else {
         return false;
      }
   }

   public record Cfg(boolean allowTeamless) {
   }
}
