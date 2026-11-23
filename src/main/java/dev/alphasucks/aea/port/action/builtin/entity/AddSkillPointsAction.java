package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.skill.SkillData;
import dev.alphasucks.aea.port.skill.SkillDataAttachment;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class AddSkillPointsAction implements ActionType<EntityCtx, AddSkillPointsAction.Cfg> {
   @Override
   public MapCodec<AddSkillPointsAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.fieldOf("skill_tree").forGetter(AddSkillPointsAction.Cfg::skillTree),
               Codec.INT.optionalFieldOf("points", 1).forGetter(AddSkillPointsAction.Cfg::points)
            )
            .apply(i, AddSkillPointsAction.Cfg::new)
      );
   }

   public void run(AddSkillPointsAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         SkillData data = SkillDataAttachment.get(player);
         data.addPoints(cfg.skillTree, cfg.points);
         ApoliNetwork.sendSkillState(player);
      }
   }

   public record Cfg(class_2960 skillTree, int points) {
   }
}
