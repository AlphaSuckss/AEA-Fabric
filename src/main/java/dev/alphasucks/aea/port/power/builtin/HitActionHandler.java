package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.HitSide;
import dev.alphasucks.aea.port.data.expr.ExprDamageContext;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.builtin.ActionOnHitPower.Config;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class HitActionHandler {
   private HitActionHandler() {
   }

   public static void fire(@Nullable class_1309 attacker, class_1309 target, class_1282 source, float amount, class_1937 level, boolean damageLanded) {
      if (damageLanded && !level.method_8608()) {
         DamageCtx damageCtx = new DamageCtx(source, target, level, amount);
         long now = level.method_8510();
         if (attacker != null) {
            fireOnHit(attacker, target, amount, level, damageCtx, now);
         }

         fireWhenHit(attacker, target, amount, level, damageCtx, now);
      }
   }

   private static void fireOnHit(class_1309 attacker, class_1309 target, float amount, class_1937 level, DamageCtx damageCtx, long now) {
      PowerContainer container = PowerContainer.of(attacker);
      if (container instanceof PowerContainerImpl impl) {
         List<class_2960> powers = container.powersOfType(ApoliIds.ACTION_ON_HIT);
         if (!powers.isEmpty()) {
            EntityCtx attackerCtx = null;
            EntityCtx targetCtx = null;
            BiEntityCtx biCtx = null;

            for (int i = 0; i < powers.size(); i++) {
               class_2960 powerId = (class_2960)powers.get(i);
               if (!container.isSuppressed(powerId)) {
                  Power power = ApoliPowers.get(powerId);
                  if (power != null && power.config() instanceof Config cfg && ready(impl, powerId, now)) {
                     if (attackerCtx == null) {
                        attackerCtx = new EntityCtx(attacker, level);
                     }

                     if (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(attackerCtx)) {
                        if (biCtx == null) {
                           biCtx = new BiEntityCtx(attacker, target, level);
                        }

                        if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(biCtx)) {
                           if (cfg.targetCondition().isPresent()) {
                              if (targetCtx == null) {
                                 targetCtx = new EntityCtx(target, level);
                              }

                              if (!((EntityCondition)cfg.targetCondition().get()).test(targetCtx)) {
                                 continue;
                              }
                           }

                           if ((!cfg.attackerCondition().isPresent() || ((EntityCondition)cfg.attackerCondition().get()).test(attackerCtx))
                              && (!cfg.damageCondition().isPresent() || ((DamageCondition)cfg.damageCondition().get()).test(damageCtx))) {
                              if (targetCtx == null) {
                                 targetCtx = new EntityCtx(target, level);
                              }

                              double prevDamage = ExprDamageContext.set(amount);

                              try {
                                 run(cfg.bientityAction(), biCtx);
                                 run(cfg.selfAction(), attackerCtx);
                                 run(cfg.attackerAction(), attackerCtx);
                                 run(cfg.targetAction(), targetCtx);
                                 run(cfg.entityAction(), cfg.entityActionTarget() == HitSide.TARGET ? targetCtx : attackerCtx);
                              } finally {
                                 ExprDamageContext.restore(prevDamage);
                              }

                              impl.setAuxInt(powerId, expiry(now, PowerResources.cooldownTicks(cfg.cooldown(), impl)));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void fireWhenHit(@Nullable class_1309 attacker, class_1309 target, float amount, class_1937 level, DamageCtx damageCtx, long now) {
      PowerContainer container = PowerContainer.of(target);
      if (container instanceof PowerContainerImpl impl) {
         List<class_2960> powers = container.powersOfType(ApoliIds.ACTION_WHEN_HIT);
         if (!powers.isEmpty()) {
            EntityCtx targetCtx = null;
            EntityCtx attackerCtx = null;
            BiEntityCtx biCtx = null;

            for (int i = 0; i < powers.size(); i++) {
               class_2960 powerId = (class_2960)powers.get(i);
               if (!container.isSuppressed(powerId)) {
                  Power power = ApoliPowers.get(powerId);
                  if (power != null && power.config() instanceof dev.alphasucks.aea.port.power.builtin.ActionWhenHitPower.Config cfg && ready(impl, powerId, now)) {
                     if (targetCtx == null) {
                        targetCtx = new EntityCtx(target, level);
                     }

                     if (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(targetCtx)) {
                        if (cfg.bientityCondition().isPresent()) {
                           if (attacker == null) {
                              continue;
                           }

                           if (biCtx == null) {
                              biCtx = new BiEntityCtx(attacker, target, level);
                           }

                           if (!((BiEntityCondition)cfg.bientityCondition().get()).test(biCtx)) {
                              continue;
                           }
                        }

                        if (cfg.attackerCondition().isPresent()) {
                           if (attacker == null) {
                              continue;
                           }

                           if (attackerCtx == null) {
                              attackerCtx = new EntityCtx(attacker, level);
                           }

                           if (!((EntityCondition)cfg.attackerCondition().get()).test(attackerCtx)) {
                              continue;
                           }
                        }

                        if ((!cfg.targetCondition().isPresent() || ((EntityCondition)cfg.targetCondition().get()).test(targetCtx))
                           && (!cfg.damageCondition().isPresent() || ((DamageCondition)cfg.damageCondition().get()).test(damageCtx))) {
                           double prevDamage = ExprDamageContext.set(amount);

                           try {
                              if (attacker != null) {
                                 if (biCtx == null) {
                                    biCtx = new BiEntityCtx(attacker, target, level);
                                 }

                                 if (attackerCtx == null) {
                                    attackerCtx = new EntityCtx(attacker, level);
                                 }

                                 run(cfg.bientityAction(), biCtx);
                                 run(cfg.attackerAction(), attackerCtx);
                              }

                              run(cfg.selfAction(), targetCtx);
                              run(cfg.targetAction(), targetCtx);
                              if (cfg.entityActionTarget() == HitSide.ATTACKER) {
                                 if (attackerCtx != null) {
                                    run(cfg.entityAction(), attackerCtx);
                                 }
                              } else {
                                 run(cfg.entityAction(), targetCtx);
                              }
                           } finally {
                              ExprDamageContext.restore(prevDamage);
                           }

                           impl.setAuxInt(powerId, expiry(now, PowerResources.cooldownTicks(cfg.cooldown(), impl)));
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void run(Optional<EntityAction> action, EntityCtx ctx) {
      if (action.isPresent()) {
         ((EntityAction)action.get()).run(ctx);
      }
   }

   private static void run(Optional<BiEntityAction> action, BiEntityCtx ctx) {
      if (action.isPresent()) {
         ((BiEntityAction)action.get()).run(ctx);
      }
   }

   static boolean ready(PowerContainerImpl impl, class_2960 powerId, long now) {
      OptionalInt cur = impl.getAuxInt(powerId);
      return cur.isEmpty() || cur.getAsInt() <= (int)now;
   }

   static int expiry(long now, int cooldown) {
      return (int)now + Math.max(cooldown, 0);
   }
}
