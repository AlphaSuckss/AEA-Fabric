package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;

public final class ModifyBlockStuckSpeedPower extends PowerType<ModifyBlockStuckSpeedPower.Config> {
   @Override
   public MapCodec<ModifyBlockStuckSpeedPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("block_condition", BlockCondition.CODEC).forGetter(ModifyBlockStuckSpeedPower.Config::blockCondition),
               Vector.SCALAR_OR_VECTOR.optionalFieldOf("multiplier", Vector.uniform(1.0F)).forGetter(ModifyBlockStuckSpeedPower.Config::multiplier)
            )
            .apply(i, ModifyBlockStuckSpeedPower.Config::new)
      );
   }

   public record Config(Optional<BlockCondition> blockCondition, Vector multiplier) {
   }
}
