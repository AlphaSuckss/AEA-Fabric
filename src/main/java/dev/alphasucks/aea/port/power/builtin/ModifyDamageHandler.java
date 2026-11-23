package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.data.AttributeModifierHelper.Owned;
import dev.alphasucks.aea.port.dev.DevMode;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.builtin.AttributeModifyTransferPower.Target;
import dev.alphasucks.aea.port.power.builtin.ModifyDamagePower.Config;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ModifyDamageHandler {
   private static final int DEV_REPORT_TICKS = 10;

   private ModifyDamageHandler() {
   }

   public static float modifyAmount(@Nullable class_1309 attacker, class_1309 target, class_1282 source, float amount, class_1937 level) {
      DamageCtx damageCtx = new DamageCtx(source, target, level, amount);
      List<ModifyDamageHandler.Match> matches = new ArrayList();
      collect(matches, target, attacker, target, damageCtx, true);
      if (attacker != null) {
         collect(matches, attacker, attacker, target, damageCtx, false);
      }

      List<Owned> transfers = transfers(attacker, target);
      if (matches.isEmpty() && transfers == null) {
         return amount;
      }

      float modified = applyModifiers(amount, matches, transfers);
      if (DevMode.any()) {
         reportToDevMode(attacker, target, source, amount, modified, matches);
      }

      for (ModifyDamageHandler.Match m : matches) {
         runActions(m, attacker, target, level);
      }

      return modified;
   }

   public static float previewAmount(@Nullable class_1309 attacker, class_1309 target, class_1282 source, float amount, class_1937 level) {
      DamageCtx damageCtx = new DamageCtx(source, target, level, amount);
      List<ModifyDamageHandler.Match> matches = new ArrayList();
      collect(matches, target, attacker, target, damageCtx, true);
      if (attacker != null) {
         collect(matches, attacker, attacker, target, damageCtx, false);
      }

      List<Owned> transfers = transfers(attacker, target);
      return matches.isEmpty() && transfers == null ? amount : applyModifiers(amount, matches, transfers);
   }

   public static boolean shouldApplyArmor(class_1309 target, @Nullable class_1309 attacker, class_1282 source, float amount, class_1937 level) {
      DamageCtx damageCtx = new DamageCtx(source, target, level, amount);
      List<ModifyDamageHandler.Match> matches = new ArrayList();
      collect(matches, target, attacker, target, damageCtx, true);

      for (ModifyDamageHandler.Match m : matches) {
         Config cfg = m.cfg;
         if (!cfg.applyArmorCondition().isEmpty() && !((EntityCondition)cfg.applyArmorCondition().get()).test(new EntityCtx(target, level))) {
            return false;
         }
      }

      return true;
   }

   public static boolean shouldDamageArmor(class_1309 target, @Nullable class_1309 attacker, class_1282 source, float amount, class_1937 level) {
      DamageCtx damageCtx = new DamageCtx(source, target, level, amount);
      List<ModifyDamageHandler.Match> matches = new ArrayList();
      collect(matches, target, attacker, target, damageCtx, true);

      for (ModifyDamageHandler.Match m : matches) {
         Config cfg = m.cfg;
         if (!cfg.damageArmorCondition().isEmpty() && !((EntityCondition)cfg.damageArmorCondition().get()).test(new EntityCtx(target, level))) {
            return false;
         }
      }

      return true;
   }

   private static void collect(
      List<ModifyDamageHandler.Match> out, class_1309 holder, @Nullable class_1309 attacker, class_1309 target, DamageCtx damageCtx, boolean targetUsedSide
   ) {
      PowerContainer container = PowerContainer.of(holder);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(ApoliIds.MODIFY_DAMAGE);
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            Power power = ApoliPowers.get(powerId);
            if (power != null && !container.isSuppressed(powerId) && power.config() instanceof Config cfg && cfg.targetUsed() == targetUsedSide) {
               class_1937 level = damageCtx.level();
               if (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(new EntityCtx(holder, level))) {
                  class_1309 bientityActor = targetUsedSide ? attacker : holder;
                  class_1309 bientityTarget = targetUsedSide ? holder : target;
                  if ((
                        !cfg.bientityCondition().isPresent()
                           || bientityActor != null
                              && ((BiEntityCondition)cfg.bientityCondition().get()).test(new BiEntityCtx(bientityActor, bientityTarget, level))
                     )
                     && (!cfg.targetCondition().isPresent() || ((EntityCondition)cfg.targetCondition().get()).test(new EntityCtx(bientityTarget, level)))
                     && (!cfg.damageCondition().isPresent() || ((DamageCondition)cfg.damageCondition().get()).test(damageCtx))) {
                     out.add(new ModifyDamageHandler.Match(powerId, cfg, holder, container, bientityActor, bientityTarget, targetUsedSide));
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static List<Owned> transfers(@Nullable class_1309 attacker, class_1309 target) {
      List<Owned> out = addTransfers(null, target, AttributeModifyTransferPower.DAMAGE_TAKEN, attacker, target);
      if (attacker != null) {
         out = addTransfers(out, attacker, AttributeModifyTransferPower.DAMAGE_DEALT, attacker, target);
      }

      return out;
   }

   @Nullable
   private static List<Owned> addTransfers(@Nullable List<Owned> out, class_1309 holder, Target transfer, @Nullable class_1309 attacker, class_1309 target) {
      if (!transfer.inUse()) {
         return out;
      }

      PowerContainer container = PowerContainer.of(holder);
      if (!transfer.appliesTo(container)) {
         return out;
      }

      List<AttributeModifier> mods = transfer.merge(holder, container, List.of());
      int i = 0;

      for (int n = mods.size(); i < n; i++) {
         if (out == null) {
            out = new ArrayList(4);
         }

         out.add(new Owned((AttributeModifier)mods.get(i), holder, container, attacker, target));
      }

      return out;
   }

   private static float applyModifiers(float amount, List<ModifyDamageHandler.Match> matches, @Nullable List<Owned> transfers) {
      List<Owned> owned = transfers;
      int i = 0;

      for (int n = matches.size(); i < n; i++) {
         ModifyDamageHandler.Match m = (ModifyDamageHandler.Match)matches.get(i);
         List<AttributeModifier> mods = m.cfg.allModifiers();
         int j = 0;

         for (int k = mods.size(); j < k; j++) {
            if (owned == null) {
               owned = new ArrayList(4);
            }

            owned.add(new Owned((AttributeModifier)mods.get(j), m.holder, m.container, m.bientityActor, m.bientityTarget));
         }
      }

      return owned == null ? amount : Math.max(0.0F, (float)AttributeModifierHelper.applyOwned(amount, owned));
   }

   private static void runActions(ModifyDamageHandler.Match m, @Nullable class_1309 attacker, class_1309 target, class_1937 level) {
      Config cfg = m.cfg;
      if (cfg.bientityAction().isPresent() && m.bientityActor != null) {
         ((BiEntityAction)cfg.bientityAction().get()).run(new BiEntityCtx(m.bientityActor, m.bientityTarget, level));
      }

      if (cfg.selfAction().isPresent()) {
         class_1309 holder = m.targetUsedSide ? target : (attacker != null ? attacker : target);
         ((EntityAction)cfg.selfAction().get()).run(new EntityCtx(holder, level));
      }

      if (!m.targetUsedSide && cfg.targetAction().isPresent()) {
         ((EntityAction)cfg.targetAction().get()).run(new EntityCtx(target, level));
      }

      if (m.targetUsedSide && cfg.attackerAction().isPresent() && attacker != null) {
         ((EntityAction)cfg.attackerAction().get()).run(new EntityCtx(attacker, level));
      }
   }

   private static void reportToDevMode(
      @Nullable class_1309 attacker, class_1309 target, class_1282 source, float before, float after, List<ModifyDamageHandler.Match> matches
   ) {
      StringBuilder line = new StringBuilder("damage ").append(number(before)).append(" → ").append(number(after));
      source.method_48793().method_40230().ifPresent(type -> line.append(" (").append(type.method_29177()).append(')'));
      appendSide(line, " · dealt: ", matches, false);
      appendSide(line, " · taken: ", matches, true);
      DevMode.reportEvery(target, new ModifyDamageHandler.DevTopic(attacker == null ? null : attacker.method_5667()), 10, line.toString());
   }

   private static void appendSide(StringBuilder line, String label, List<ModifyDamageHandler.Match> matches, boolean takenSide) {
      boolean first = true;
      int i = 0;

      for (int n = matches.size(); i < n; i++) {
         ModifyDamageHandler.Match m = (ModifyDamageHandler.Match)matches.get(i);
         if (m.targetUsedSide == takenSide) {
            line.append(first ? label : ", ").append(m.powerId);
            if (!takenSide) {
               line.append(" (").append(m.holder.method_5477().getString()).append(')');
            }

            first = false;
         }
      }
   }

   private static String number(float value) {
      if (!Float.isFinite(value)) {
         return Float.toString(value);
      } else {
         return value == Math.rint(value) && Math.abs(value) < 1.0E15F
            ? Long.toString((long)value)
            : BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
      }
   }

   private record DevTopic(@Nullable UUID attacker) {
   }

   private record Match(
      class_2960 powerId,
      Config cfg,
      class_1309 holder,
      PowerContainer container,
      @Nullable class_1309 bientityActor,
      class_1309 bientityTarget,
      boolean targetUsedSide
   ) {
   }
}
