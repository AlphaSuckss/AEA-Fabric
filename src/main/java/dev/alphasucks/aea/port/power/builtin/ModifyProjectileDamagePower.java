package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;

public final class ModifyProjectileDamagePower extends PowerType<ModifyProjectileDamagePower.Config> {
   @Override
   public MapCodec<ModifyProjectileDamagePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("damage_condition", DamageCondition.CODEC).forGetter(ModifyProjectileDamagePower.Config::damageCondition),
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyProjectileDamagePower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyProjectileDamagePower.Config::modifiers),
               LoggedOptionalField.strict("target_condition", EntityCondition.CODEC).forGetter(ModifyProjectileDamagePower.Config::targetCondition),
               LoggedOptionalField.of("self_action", EntityAction.CODEC).forGetter(ModifyProjectileDamagePower.Config::selfAction),
               LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(ModifyProjectileDamagePower.Config::targetAction)
            )
            .apply(i, ModifyProjectileDamagePower.Config::new)
      );
   }

   public record Config(
      Optional<DamageCondition> damageCondition,
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      Optional<EntityCondition> targetCondition,
      Optional<EntityAction> selfAction,
      Optional<EntityAction> targetAction
   ) {
   }
}
