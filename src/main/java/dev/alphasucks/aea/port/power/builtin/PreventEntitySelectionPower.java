package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import net.minecraft.class_1297;

public final class PreventEntitySelectionPower extends PowerType<PreventEntitySelectionPower.Config> {
   @Override
   public MapCodec<PreventEntitySelectionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("entity_condition", EntityCondition.CODEC).forGetter(PreventEntitySelectionPower.Config::entityCondition),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(PreventEntitySelectionPower.Config::bientityCondition)
            )
            .apply(i, PreventEntitySelectionPower.Config::new)
      );
   }

   public static boolean prevents(class_1297 viewer, class_1297 entity) {
      return PowerLookup.anyActive(
         viewer,
         ApoliIds.PREVENT_ENTITY_SELECTION,
         PreventEntitySelectionPower.Config.class,
         PreventEntitySelectionPower::blocks,
         BiEntityCtx.of(viewer, entity, entity.method_37908())
      );
   }

   private static boolean blocks(PreventEntitySelectionPower.Config config, BiEntityCtx pair) {
      return config.entityCondition().isPresent() && !((EntityCondition)config.entityCondition().get()).test(EntityCtx.of(pair.target(), pair.level()))
         ? false
         : config.bientityCondition().isEmpty() || ((BiEntityCondition)config.bientityCondition().get()).test(pair);
   }

   public record Config(Optional<EntityCondition> entityCondition, Optional<BiEntityCondition> bientityCondition) {
   }
}
