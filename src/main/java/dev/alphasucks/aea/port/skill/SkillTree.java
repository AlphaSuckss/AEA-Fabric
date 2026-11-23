package dev.alphasucks.aea.port.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.data.IconData;
import dev.alphasucks.aea.port.data.TextComponent;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record SkillTree(
   class_2960 id,
   class_2561 name,
   class_2561 description,
   IconData icon,
   SkillFrame frame,
   Optional<class_2960> background,
   List<class_2960> defaultPowers,
   int order,
   boolean autoGrant,
   boolean refundable,
   Optional<EntityCondition> condition
) {
   public static MapCodec<SkillTree> codec(class_2960 id) {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               TextComponent.CODEC.optionalFieldOf("name", class_2561.method_43473()).forGetter(SkillTree::name),
               TextComponent.CODEC.optionalFieldOf("description", class_2561.method_43473()).forGetter(SkillTree::description),
               IconData.CODEC.optionalFieldOf("icon", IconData.grassBlock()).forGetter(SkillTree::icon),
               SkillFrame.CODEC.optionalFieldOf("frame", SkillFrame.TASK).forGetter(SkillTree::frame),
               IdCodecs.ID.optionalFieldOf("background").forGetter(SkillTree::background),
               IdCodecs.ID.listOf().optionalFieldOf("default_powers", List.of()).forGetter(SkillTree::defaultPowers),
               Codec.INT.optionalFieldOf("order", 0).forGetter(SkillTree::order),
               Codec.BOOL.optionalFieldOf("auto_grant", true).forGetter(SkillTree::autoGrant),
               Codec.BOOL.optionalFieldOf("refundable", true).forGetter(SkillTree::refundable),
               LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(SkillTree::condition)
            )
            .apply(
               i,
               (name, description, icon, frame, background, defaultPowers, order, autoGrant, refundable, condition) -> new SkillTree(
                  id, name, description, icon, frame, background, List.copyOf(defaultPowers), order, autoGrant, refundable, condition
               )
            )
      );
   }
}
