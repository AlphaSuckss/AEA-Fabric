package dev.alphasucks.aea.port.power.builtin;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.AttributeModifier;
import dev.alphasucks.aea.port.data.AttributeModifierHelper;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1282;
import net.minecraft.class_1309;
import net.minecraft.class_1676;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_8103;
import org.jetbrains.annotations.Nullable;

public final class ModifyProjectileDamageHandler {
   private static final class_2960 CANONICAL = Apoli.id("modify_projectile_damage");
   private static class_1676 activeProjectile;

   private ModifyProjectileDamageHandler() {
   }

   public static float modifyAmount(@Nullable class_1309 shooter, class_1309 target, class_1282 source, float amount, class_1937 level) {
      return apply(shooter, target, source, amount, level, true);
   }

   public static float previewAmount(@Nullable class_1309 shooter, class_1309 target, class_1282 source, float amount, class_1937 level) {
      return apply(shooter, target, source, amount, level, false);
   }

   private static float apply(@Nullable class_1309 shooter, class_1309 target, class_1282 source, float amount, class_1937 level, boolean runActions) {
      if (shooter == null && source.method_5526() instanceof class_1676 p && p.method_24921() instanceof class_1309 owner) {
         shooter = owner;
      }

      if (shooter == null) {
         return amount;
      }

      if (!isProjectile(source) && !fromActiveProjectile(shooter)) {
         return amount;
      }

      PowerContainer container = PowerContainer.of(shooter);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(CANONICAL);
         boolean transferred = AttributeModifyTransferPower.PROJECTILE_DAMAGE.appliesTo(container);
         if (powers.isEmpty() && !transferred) {
            return amount;
         }

         DamageCtx damageCtx = new DamageCtx(source, target, level, amount);
         List<ModifyProjectileDamagePower.Config> matches = new ArrayList();
         int i = 0;

         for (int n = powers.size(); i < n; i++) {
            class_2960 powerId = (class_2960)powers.get(i);
            Power power = ApoliPowers.get(powerId);
            if (power != null
               && !container.isSuppressed(powerId)
               && power.config() instanceof ModifyProjectileDamagePower.Config cfg
               && (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(new EntityCtx(shooter, level)))
               && (!cfg.damageCondition().isPresent() || ((DamageCondition)cfg.damageCondition().get()).test(damageCtx))
               && (!cfg.targetCondition().isPresent() || ((EntityCondition)cfg.targetCondition().get()).test(new EntityCtx(target, level)))) {
               matches.add(cfg);
            }
         }

         List<AttributeModifier> allMods = new ArrayList();

         for (ModifyProjectileDamagePower.Config cfg : matches) {
            allMods.addAll(AttributeModifierHelper.flatten(cfg.modifier(), cfg.modifiers()));
         }

         if (transferred) {
            allMods = AttributeModifyTransferPower.PROJECTILE_DAMAGE.merge(shooter, container, allMods);
         }

         if (matches.isEmpty() && allMods.isEmpty()) {
            return amount;
         }

         float modified = (float)AttributeModifierHelper.apply(amount, allMods, shooter, container, shooter, target);
         if (runActions) {
            class_1309 finalShooter = shooter;

            for (ModifyProjectileDamagePower.Config cfg : matches) {
               cfg.selfAction().ifPresent(a -> a.run(new EntityCtx(finalShooter, level)));
               cfg.targetAction().ifPresent(a -> a.run(new EntityCtx(target, level)));
            }
         }

         return Math.max(0.0F, modified);
      } else {
         return amount;
      }
   }

   private static boolean isProjectile(class_1282 source) {
      return source.method_48789(class_8103.field_42247) ? true : source.method_5526() instanceof class_1676;
   }

   public static void beginProjectileContext(class_1676 projectile) {
      activeProjectile = projectile;
   }

   public static void endProjectileContext() {
      activeProjectile = null;
   }

   private static boolean fromActiveProjectile(class_1309 shooter) {
      return activeProjectile != null && activeProjectile.method_24921() == shooter;
   }
}
