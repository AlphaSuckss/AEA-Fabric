package dev.alphasucks.aea.port.power;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.data.TextComponent;
import dev.alphasucks.aea.port.skill.Skill;
import dev.alphasucks.aea.port.skill.SkillInfo;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public record Power(
   class_2960 typeId,
   Optional<class_2561> name,
   Optional<class_2561> description,
   Optional<EntityCondition> condition,
   boolean hidden,
   List<String> tags,
   Optional<SkillInfo> skill,
   Object config
) {
   public static final Codec<Power> CODEC = class_2960.field_25139.partialDispatch("type", p -> DataResult.success(p.typeId()), id -> {
      class_2960 canonical = PowerTypeRegistry.resolveId(id);
      PowerType<?> type = PowerTypeRegistry.get(canonical);
      return type == null ? DataResult.error(() -> "Unknown power type: " + id) : DataResult.success(buildMapCodec(canonical, type).codec());
   });

   public Power {
      typeId = PowerTypeRegistry.resolveId(typeId);
      tags = List.copyOf(tags);
   }

   public boolean hasTag(String tag) {
      for (int i = 0; i < this.tags.size(); i++) {
         if (((String)this.tags.get(i)).equals(tag)) {
            return true;
         }
      }

      return false;
   }

   public Optional<Skill> toSkill(class_2960 id) {
      return this.skill
         .map(
            info -> new Skill(
               id,
               info.parent(),
               this.displayName(id),
               this.displayDescription(id),
               info.icon(),
               info.frame(),
               List.of(id),
               info.condition(),
               info.visibilityCondition(),
               info.excludes(),
               info.cost(),
               info.order(),
               info.reward(),
               info.placement()
            )
         );
   }

   public PowerType<?> type() {
      return PowerTypeRegistry.get(this.typeId);
   }

   public class_2561 displayName(class_2960 id) {
      return (class_2561)this.name.orElseGet(() -> class_2561.method_43471(translationKey(id, "name")));
   }

   public class_2561 displayDescription(class_2960 id) {
      return (class_2561)this.description.orElseGet(() -> class_2561.method_43471(translationKey(id, "description")));
   }

   public static String translationKey(class_2960 id, String suffix) {
      return TextComponent.autoKey(id, suffix);
   }

   private static <C> MapCodec<Power> buildMapCodec(class_2960 typeId, PowerType<C> type) {
      return RecordCodecBuilder.mapCodec(
         instance -> instance.group(
               LoggedOptionalField.of("name", TextComponent.CODEC).forGetter(Power::name),
               LoggedOptionalField.of("description", TextComponent.CODEC).forGetter(Power::description),
               LoggedOptionalField.strict("condition", EntityCondition.CODEC).forGetter(Power::condition),
               Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Power::hidden),
               SingleOrList.of(Codec.STRING).optionalFieldOf("tags", List.of()).forGetter(Power::tags),
               LoggedOptionalField.of("skill", SkillInfo.CODEC).forGetter(Power::skill),
               type.configCodec().forGetter(p -> (C)p.config())
            )
            .apply(instance, (name, desc, cond, hidden, tags, skill, cfg) -> new Power(typeId, name, desc, cond, hidden, tags, skill, cfg))
      );
   }
}
