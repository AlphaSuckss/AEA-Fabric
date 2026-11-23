package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.skill.SkillTrees;
import java.util.Optional;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class ResetSkillsAction implements ActionType<EntityCtx, ResetSkillsAction.Cfg> {
   @Override
   public MapCodec<ResetSkillsAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("tree").forGetter(ResetSkillsAction.Cfg::tree),
               Codec.BOOL.optionalFieldOf("refund", true).forGetter(ResetSkillsAction.Cfg::refund)
            )
            .apply(i, ResetSkillsAction.Cfg::new)
      );
   }

   public void run(ResetSkillsAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         SkillTrees.resetSkills(player, (class_2960)cfg.tree().orElse(null), cfg.refund());
         ApoliNetwork.sendSkillState(player);
      }
   }

   public record Cfg(Optional<class_2960> tree, boolean refund) {
   }
}
