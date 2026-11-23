package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HitSide;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_2960;

public final class ActionOnHitPower extends PowerType<ActionOnHitPower.Config> {
   @Override
   public MapCodec<ActionOnHitPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnHitPower.Config::bientityAction),
               LoggedOptionalField.of("self_action", EntityAction.CODEC).forGetter(ActionOnHitPower.Config::selfAction),
               LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(ActionOnHitPower.Config::targetAction),
               LoggedOptionalField.of("attacker_action", EntityAction.CODEC).forGetter(ActionOnHitPower.Config::attackerAction),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnHitPower.Config::entityAction),
               HitSide.CODEC.optionalFieldOf("entity_action_target", HitSide.SELF).forGetter(ActionOnHitPower.Config::entityActionTarget),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnHitPower.Config::bientityCondition),
               LoggedOptionalField.strict("target_condition", EntityCondition.CODEC).forGetter(ActionOnHitPower.Config::targetCondition),
               LoggedOptionalField.strict("attacker_condition", EntityCondition.CODEC).forGetter(ActionOnHitPower.Config::attackerCondition),
               LoggedOptionalField.strict("damage_condition", DamageCondition.CODEC).forGetter(ActionOnHitPower.Config::damageCondition),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(1.0)).forGetter(ActionOnHitPower.Config::cooldown),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnHitPower.Config::hudRender)
            )
            .apply(i, ActionOnHitPower.Config::new)
      );
   }

   public void onAdded(class_2960 powerId, ActionOnHitPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!impl.getAuxInt(powerId).isPresent()) {
            impl.setAuxInt(powerId, 0);
         }
      }
   }

   public void onRemoved(class_2960 powerId, ActionOnHitPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!holder.hasPower(powerId)) {
            impl.removeAux(powerId);
         }
      }
   }

   public boolean isActive(class_2960 powerId, ActionOnHitPower.Config cfg, EntityCtx ctx) {
      return true;
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, ActionOnHitPower.Config cfg, PowerContainer holder) {
      return PowerResources.readDeadline(holder, powerId);
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnHitPower.Config cfg, PowerContainer holder, int value) {
      return PowerResources.writeDeadline(holder, powerId, value, PowerResources.cooldownTicks(cfg.cooldown, holder));
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnHitPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown, holder), 0) : 0);
   }

   public record Config(
      Optional<BiEntityAction> bientityAction,
      Optional<EntityAction> selfAction,
      Optional<EntityAction> targetAction,
      Optional<EntityAction> attackerAction,
      Optional<EntityAction> entityAction,
      HitSide entityActionTarget,
      Optional<BiEntityCondition> bientityCondition,
      Optional<EntityCondition> targetCondition,
      Optional<EntityCondition> attackerCondition,
      Optional<DamageCondition> damageCondition,
      Expression cooldown,
      HudRender hudRender
   ) implements HudRendered {
   }
}
