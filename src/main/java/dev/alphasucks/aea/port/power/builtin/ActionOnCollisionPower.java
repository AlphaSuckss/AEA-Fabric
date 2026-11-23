package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.HudRender;
import dev.alphasucks.aea.port.dev.DevParticles;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.HudRendered;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class ActionOnCollisionPower extends PowerType<ActionOnCollisionPower.Config> {
   @Override
   public MapCodec<ActionOnCollisionPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(ActionOnCollisionPower.Config::bientityAction),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ActionOnCollisionPower.Config::entityAction),
               LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(ActionOnCollisionPower.Config::targetAction),
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ActionOnCollisionPower.Config::bientityCondition),
               LoggedOptionalField.strict("target_condition", EntityCondition.CODEC).forGetter(ActionOnCollisionPower.Config::targetCondition),
               Codec.FLOAT.optionalFieldOf("radius", 0.0F).forGetter(ActionOnCollisionPower.Config::radius),
               Expression.INT_OR_EXPR.optionalFieldOf("cooldown", Expression.constant(0.0)).forGetter(ActionOnCollisionPower.Config::cooldown),
               Codec.BOOL.optionalFieldOf("include_riding", false).forGetter(ActionOnCollisionPower.Config::includeRiding),
               HudRender.CODEC.optionalFieldOf("hud_render", HudRender.DONT_RENDER).forGetter(ActionOnCollisionPower.Config::hudRender)
            )
            .apply(i, ActionOnCollisionPower.Config::new)
      );
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   public void onRemoved(class_2960 powerId, ActionOnCollisionPower.Config cfg, PowerContainer holder, class_2960 source) {
      if (holder instanceof PowerContainerImpl impl && !holder.hasPower(powerId)) {
         impl.removeAux(powerId);
      }
   }

   public void tick(class_2960 powerId, ActionOnCollisionPower.Config cfg, PowerContainer holder) {
      if (holder instanceof PowerContainerImpl impl) {
         class_1297 owner = holder.rawOwner();
         if (owner != null && !owner.method_31481()) {
            if (owner.method_37908() instanceof class_3218 level) {
               long var18 = level.method_8510();
               if (HitActionHandler.ready(impl, powerId, var18)) {
                  Power loaded = ApoliPowers.get(powerId);
                  EntityCtx selfCtx = new EntityCtx(owner, level);
                  if (loaded == null || !loaded.condition().isPresent() || ((EntityCondition)loaded.condition().get()).test(selfCtx)) {
                     class_238 box = cfg.radius() > 0.0F ? owner.method_5829().method_1014(cfg.radius()) : owner.method_5829();
                     if (DevParticles.due(level)) {
                        DevParticles.outlineBox(level, box);
                     }

                     List<class_1297> hits = level.method_8333(owner, box, otherx -> !otherx.method_7325() && otherx.method_5805());
                     if (!hits.isEmpty()) {
                        for (int i = 0; i < hits.size(); i++) {
                           class_1297 other = (class_1297)hits.get(i);
                           if (cfg.includeRiding() || !other.method_5794(owner) && !other.method_5626(owner) && !owner.method_5626(other)) {
                              BiEntityCtx biCtx = new BiEntityCtx(owner, other, level);
                              if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(biCtx)) {
                                 EntityCtx targetCtx = new EntityCtx(other, level);
                                 if (!cfg.targetCondition().isPresent() || ((EntityCondition)cfg.targetCondition().get()).test(targetCtx)) {
                                    if (cfg.bientityAction().isPresent()) {
                                       ((BiEntityAction)cfg.bientityAction().get()).run(biCtx);
                                    }

                                    if (cfg.entityAction().isPresent()) {
                                       ((EntityAction)cfg.entityAction().get()).run(selfCtx);
                                    }

                                    if (cfg.targetAction().isPresent()) {
                                       ((EntityAction)cfg.targetAction().get()).run(targetCtx);
                                    }

                                    int ticks = PowerResources.cooldownTicks(cfg.cooldown(), impl);
                                    if (ticks > 0) {
                                       impl.setAuxInt(powerId, HitActionHandler.expiry(var18, ticks));
                                       return;
                                    }
                                 }
                              }
                           }
                        }
                     }
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

   public OptionalInt readResource(class_2960 powerId, ActionOnCollisionPower.Config cfg, PowerContainer holder) {
      return PowerResources.readDeadline(holder, powerId);
   }

   public OptionalInt writeResource(class_2960 powerId, ActionOnCollisionPower.Config cfg, PowerContainer holder, int value) {
      return PowerResources.writeDeadline(holder, powerId, value, PowerResources.cooldownTicks(cfg.cooldown(), holder));
   }

   public OptionalInt resourceBound(class_2960 powerId, ActionOnCollisionPower.Config cfg, PowerContainer holder, boolean max) {
      return OptionalInt.of(max ? Math.max(PowerResources.cooldownTicks(cfg.cooldown(), holder), 0) : 0);
   }

   public record Config(
      Optional<BiEntityAction> bientityAction,
      Optional<EntityAction> entityAction,
      Optional<EntityAction> targetAction,
      Optional<BiEntityCondition> bientityCondition,
      Optional<EntityCondition> targetCondition,
      float radius,
      Expression cooldown,
      boolean includeRiding,
      HudRender hudRender
   ) implements HudRendered {
   }
}
