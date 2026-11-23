package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
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
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class ActionOnKillPower extends PowerType<ActionOnKillPower.Config> {
   @Override
   public MapCodec<ActionOnKillPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnKillPower.Config::bientityAction),
               LoggedOptionalField.of("self_action", EntityAction.CODEC).forGetter(ActionOnKillPower.Config::selfAction),
               LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(ActionOnKillPower.Config::targetAction),
               LoggedOptionalField.of("attacker_action", EntityAction.CODEC).forGetter(ActionOnKillPower.Config::attackerAction),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnKillPower.Config::entityAction),
               HitSide.CODEC.optionalFieldOf("entity_action_target", HitSide.SELF).forGetter(ActionOnKillPower.Config::entityActionTarget),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnKillPower.Config::bientityCondition),
               LoggedOptionalField.strict("target_condition", EntityCondition.CODEC).forGetter(ActionOnKillPower.Config::targetCondition),
               LoggedOptionalField.strict("attacker_condition", EntityCondition.CODEC).forGetter(ActionOnKillPower.Config::attackerCondition),
               LoggedOptionalField.strict("damage_condition", DamageCondition.CODEC).forGetter(ActionOnKillPower.Config::damageCondition),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(1.0)).forGetter(ActionOnKillPower.Config::cooldown),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnKillPower.Config::hudRender)
            )
            .apply(i, ActionOnKillPower.Config::new)
      );
   }

   public void onAdded(class_2960 powerId, ActionOnKillPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!impl.getAuxInt(powerId).isPresent()) {
            impl.setAuxInt(powerId, 0);
         }
      }
   }

   public void onRemoved(class_2960 powerId, ActionOnKillPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl) {
         if (!holder.hasPower(powerId)) {
            impl.removeAux(powerId);
         }
      }
   }

   public boolean isActive(class_2960 powerId, ActionOnKillPower.Config cfg, EntityCtx ctx) {
      return true;
   }

   public void onKill(
      class_2960 powerId, ActionOnKillPower.Config cfg, class_1309 killer, class_1309 victim, class_1282 source, class_3218 level, PowerContainerImpl impl
   ) {
      long now = level.method_8510();
      if (HitActionHandler.ready(impl, powerId, now)) {
         BiEntityCtx biCtx = new BiEntityCtx(killer, victim, level);
         EntityCtx killerCtx = new EntityCtx(killer, level);
         EntityCtx victimCtx = new EntityCtx(victim, level);
         if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(biCtx)) {
            if (!cfg.targetCondition().isPresent() || ((EntityCondition)cfg.targetCondition().get()).test(victimCtx)) {
               if (!cfg.attackerCondition().isPresent() || ((EntityCondition)cfg.attackerCondition().get()).test(killerCtx)) {
                  if (!cfg.damageCondition().isPresent() || ((DamageCondition)cfg.damageCondition().get()).test(new DamageCtx(source, victim, level, 0.0F))) {
                     cfg.bientityAction().ifPresent(a -> a.run(biCtx));
                     cfg.selfAction().ifPresent(a -> a.run(killerCtx));
                     cfg.attackerAction().ifPresent(a -> a.run(killerCtx));
                     cfg.targetAction().ifPresent(a -> a.run(victimCtx));
                     EntityCtx entityCtx = cfg.entityActionTarget() == HitSide.TARGET ? victimCtx : killerCtx;
                     cfg.entityAction().ifPresent(a -> a.run(entityCtx));
                     impl.setAuxInt(powerId, HitActionHandler.expiry(now, PowerResources.cooldownTicks(cfg.cooldown(), impl)));
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean isCooldown() {
      return true;
   }

   public OptionalInt readResource(class_2960 powerId, ActionOnKillPower.Config cfg, PowerContainer holder) {
      return PowerResources.readDeadline(holder, powerId);
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnKillPower.Config cfg, PowerContainer holder, int value) {
      return PowerResources.writeDeadline(holder, powerId, value, PowerResources.cooldownTicks(cfg.cooldown(), holder));
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnKillPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown(), holder), 0) : 0);
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
