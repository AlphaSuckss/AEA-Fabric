package dev.alphasucks.aea.port.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.data.IconData;
import dev.alphasucks.aea.port.data.TextComponent;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record Skill(
   class_2960 id,
   class_2960 parent,
   class_2561 name,
   class_2561 description,
   IconData icon,
   SkillFrame frame,
   List<class_2960> powers,
   Optional<EntityCondition> condition,
   Optional<EntityCondition> visibilityCondition,
   List<class_2960> excludes,
   SkillCost cost,
   int order,
   SkillReward reward,
   Optional<SkillPlacement> placement
) {
   public class_2960 rootId() {
      return SkillRegistry.rootOf(this.id);
   }

   public boolean isGate() {
      return this.powers.isEmpty() && this.reward.isEmpty();
   }

   static Optional<SkillReward> optionalReward(SkillReward reward) {
      return reward.isEmpty() && !reward.hidden() ? Optional.empty() : Optional.of(reward);
   }

   public static String translationKey(class_2960 id, String suffix) {
      return "skill." + id.method_12836() + "." + id.method_12832().replace('/', '.') + "." + suffix;
   }

   public static MapCodec<Skill> fileCodec(class_2960 id) {
      return AliasingMapCodec.wrap(
         RecordCodecBuilder.mapCodec(
            i -> i.group(
                  IdCodecs.ID.fieldOf("parent").forGetter(Skill::parent),
                  TextComponent.CODEC.optionalFieldOf("name", class_2561.method_43471(translationKey(id, "name"))).forGetter(Skill::name),
                  TextComponent.CODEC.optionalFieldOf("description", class_2561.method_43471(translationKey(id, "description"))).forGetter(Skill::description),
                  IconData.CODEC.optionalFieldOf("icon", IconData.grassBlock()).forGetter(Skill::icon),
                  SkillFrame.CODEC.optionalFieldOf("frame", SkillFrame.TASK).forGetter(Skill::frame),
                  SingleOrList.of(IdCodecs.ID).optionalFieldOf("powers", List.of()).forGetter(Skill::powers),
                  LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(Skill::condition),
                  LoggedOptionalField.strict("visibility_condition", EntityCondition.CODEC).forGetter(Skill::visibilityCondition),
                  IdCodecs.ID.listOf().optionalFieldOf("excludes", List.of()).forGetter(Skill::excludes),
                  SkillCost.CODEC.optionalFieldOf("cost", SkillCost.FREE).forGetter(Skill::cost),
                  Codec.INT.optionalFieldOf("order", 0).forGetter(Skill::order),
                  LoggedOptionalField.strict("reward", SkillReward.CODEC).forGetter(skill -> optionalReward(skill.reward())),
                  LoggedOptionalField.strict("placement", SkillPlacement.CODEC).forGetter(Skill::placement)
               )
               .apply(
                  i,
                  (parent, name, description, icon, frame, powers, condition, visibilityCondition, excludes, cost, order, reward, placement) -> new Skill(
                     id,
                     parent,
                     name,
                     description,
                     icon,
                     frame,
                     List.copyOf(powers),
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
      );
   }
}
