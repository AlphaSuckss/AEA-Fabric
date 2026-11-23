package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BlockAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;

public final class ModifyBouncinessPower extends PowerType<ModifyBouncinessPower.Config> {
   @Override
   public MapCodec<ModifyBouncinessPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("block_condition", BlockCondition.CODEC).forGetter(ModifyBouncinessPower.Config::blockCondition),
               LoggedOptionalField.of("block_action", BlockAction.CODEC).forGetter(ModifyBouncinessPower.Config::blockAction),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ModifyBouncinessPower.Config::entityAction),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyBouncinessPower.Config::modifiers),
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyBouncinessPower.Config::modifier),
               Codec.BOOL.optionalFieldOf("damage", false).forGetter(ModifyBouncinessPower.Config::damage),
               Codec.BOOL.optionalFieldOf("preventable", true).forGetter(ModifyBouncinessPower.Config::preventable)
            )
            .apply(i, ModifyBouncinessPower.Config::new)
      );
   }

   public record Config(
      Optional<BlockCondition> blockCondition,
      Optional<BlockAction> blockAction,
      Optional<EntityAction> entityAction,
      Optional<List<AttributeModifier>> modifiers,
      Optional<AttributeModifier> modifier,
      boolean damage,
      boolean preventable
   ) {
   }
}
