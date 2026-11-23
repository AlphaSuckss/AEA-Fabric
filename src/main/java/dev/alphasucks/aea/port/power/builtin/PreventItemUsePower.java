package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public final class PreventItemUsePower extends PowerType<PreventItemUsePower.Config> {
   @Override
   public MapCodec<PreventItemUsePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(PreventItemUsePower.Config::itemCondition))
            .apply(i, PreventItemUsePower.Config::new)
      );
   }

   public record Config(Optional<ItemCondition> itemCondition) {
   }
}
