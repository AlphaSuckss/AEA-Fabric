package dev.alphasucks.aea.port.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.data.IconData;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2960;

public record SkillInfo(
   IconData icon,
   SkillFrame frame,
   class_2960 parent,
   Optional<EntityCondition> condition,
   Optional<EntityCondition> visibilityCondition,
   List<class_2960> excludes,
   SkillCost cost,
   int order,
   SkillReward reward,
   Optional<SkillPlacement> placement
) {
   public static final Codec<SkillInfo> CODEC = AliasingMapCodec.wrap(
         RecordCodecBuilder.<SkillInfo>mapCodec(
            i -> i.group(
                  IconData.CODEC.optionalFieldOf("icon", IconData.grassBlock()).forGetter(SkillInfo::icon),
                  SkillFrame.CODEC.optionalFieldOf("frame", SkillFrame.TASK).forGetter(SkillInfo::frame),
                  IdCodecs.ID.fieldOf("parent").forGetter(SkillInfo::parent),
                  LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(SkillInfo::condition),
                  LoggedOptionalField.strict("visibility_condition", EntityCondition.CODEC).forGetter(SkillInfo::visibilityCondition),
                  IdCodecs.ID.listOf().optionalFieldOf("excludes", List.of()).forGetter(SkillInfo::excludes),
                  SkillCost.CODEC.optionalFieldOf("cost", SkillCost.FREE).forGetter(SkillInfo::cost),
                  Codec.INT.optionalFieldOf("order", 0).forGetter(SkillInfo::order),
                  LoggedOptionalField.strict("reward", SkillReward.CODEC).forGetter((SkillInfo info) -> Skill.optionalReward(info.reward())),
                  LoggedOptionalField.strict("placement", SkillPlacement.CODEC).forGetter(SkillInfo::placement)
               )
               .apply(
                  i,
                  (icon, frame, parent, condition, visibilityCondition, excludes, cost, order, reward, placement) -> new SkillInfo(
                     icon,
                     frame,
                     parent,
                     condition,
                     visibilityCondition,
                     List.copyOf(excludes),
                     cost,
                     order,
                     (SkillReward)reward.orElse(SkillReward.NONE),
                     placement
                  )
               )
         ),
         Map.of("rewards", "reward")
      )
      .codec();
}
