package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class ModifyDamagePower extends PowerType<ModifyDamagePower.Config> {
   @Override
   public MapCodec<ModifyDamagePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ModifyDamagePower.Config::bientityAction),
               LoggedOptionalField.of("self_action", EntityAction.CODEC).forGetter(ModifyDamagePower.Config::selfAction),
               LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(ModifyDamagePower.Config::targetAction),
               LoggedOptionalField.of("attacker_action", EntityAction.CODEC).forGetter(ModifyDamagePower.Config::attackerAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ModifyDamagePower.Config::bientityCondition),
               LoggedOptionalField.strict("target_condition", EntityCondition.CODEC).forGetter(ModifyDamagePower.Config::targetCondition),
               LoggedOptionalField.strict("apply_armor_condition", EntityCondition.CODEC).forGetter(ModifyDamagePower.Config::applyArmorCondition),
               LoggedOptionalField.strict("damage_armor_condition", EntityCondition.CODEC).forGetter(ModifyDamagePower.Config::damageArmorCondition),
               LoggedOptionalField.strict("damage_condition", DamageCondition.CODEC).forGetter(ModifyDamagePower.Config::damageCondition),
               AttributeModifier.CODEC.optionalFieldOf("modifier").forGetter(ModifyDamagePower.Config::modifier),
               AttributeModifier.LIST_OR_SINGLE.optionalFieldOf("modifiers").forGetter(ModifyDamagePower.Config::modifiers),
               Codec.BOOL.optionalFieldOf("target_used", false).forGetter(ModifyDamagePower.Config::targetUsed)
            )
            .apply(i, ModifyDamagePower.Config::new)
      );
   }

   public record Config(
      Optional<BiEntityAction> bientityAction,
      Optional<EntityAction> selfAction,
      Optional<EntityAction> targetAction,
      Optional<EntityAction> attackerAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<EntityCondition> targetCondition,
      Optional<EntityCondition> applyArmorCondition,
      Optional<EntityCondition> damageArmorCondition,
      Optional<DamageCondition> damageCondition,
      Optional<AttributeModifier> modifier,
      Optional<List<AttributeModifier>> modifiers,
      boolean targetUsed
   ) {
      public List<AttributeModifier> allModifiers() {
         if (this.modifier.isEmpty() && this.modifiers.isEmpty()) {
            return List.of();
         }

         if (this.modifier.isPresent() && this.modifiers.isEmpty()) {
            return List.of((AttributeModifier)this.modifier.get());
         }

         if (this.modifier.isEmpty()) {
            return (List<AttributeModifier>)this.modifiers.get();
         }

         List<AttributeModifier> combined = new ArrayList(((List)this.modifiers.get()).size() + 1);
         combined.add((AttributeModifier)this.modifier.get());
         combined.addAll((Collection)this.modifiers.get());
         return combined;
      }
   }
}
