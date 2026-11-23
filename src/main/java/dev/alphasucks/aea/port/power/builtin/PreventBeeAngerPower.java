package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public class PreventBeeAngerPower extends PowerType<PreventBeeAngerPower.Config> {
   @Override
   public MapCodec<PreventBeeAngerPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("block_condition", BlockCondition.CODEC).forGetter(PreventBeeAngerPower.Config::blockCondition),
               LoggedOptionalField.of("bientity_condition", BiEntityCondition.CODEC).forGetter(PreventBeeAngerPower.Config::biEntityCondition),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(PreventBeeAngerPower.Config::biEntityAction)
            )
            .apply(i, PreventBeeAngerPower.Config::new)
      );
   }

   public record Config(Optional<BlockCondition> blockCondition, Optional<BiEntityCondition> biEntityCondition, Optional<BiEntityAction> biEntityAction) {
   }
}
