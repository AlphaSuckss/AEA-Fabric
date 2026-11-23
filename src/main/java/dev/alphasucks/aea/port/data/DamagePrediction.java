package dev.alphasucks.aea.port.data;

import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.mixin.damage.LivingEntityLastHurtAccessor;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import dev.alphasucks.aea.port.power.builtin.ModifyDamageHandler;
import dev.alphasucks.aea.port.power.builtin.ModifyProjectileDamageHandler;
import dev.alphasucks.aea.port.power.builtin.PreventDeathPower;
import dev.alphasucks.aea.port.power.builtin.PreventDeathPower.Config;
import java.util.Iterator;
import net.minecraft.class_1280;
import net.minecraft.class_1282;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1890;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_3483;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.class_8103;
import net.minecraft.class_8110;
import net.minecraft.class_6880.class_6883;
import org.jetbrains.annotations.Nullable;

public final class DamagePrediction {
   private DamagePrediction() {
   }

   public static boolean wouldKill(class_1309 target, class_1282 source, float amount) {
      float health = target.method_6032();
      if (health <= 0.0F) {
         return false;
      }

      float dealt = predict(target, source, amount);
      return dealt < health ? false : !preventsDeath(target, source, amount);
   }

   public static float predict(class_1309 target, class_1282 source, float amount) {
      class_1937 level = target.method_37908();
      if (target.method_5679(source)) {
         return 0.0F;
      }

      if (target.method_29504()) {
         return 0.0F;
      }

      if (source.method_48789(class_8103.field_42246) && target.method_6059(class_1294.field_5918)) {
         return 0.0F;
      }

      class_1309 attacker = source.method_5529() instanceof class_1309 le ? le : null;
      float f = ModifyProjectileDamageHandler.previewAmount(attacker, target, source, amount, level);
      f = ModifyDamageHandler.previewAmount(attacker, target, source, f, level);
      if (f <= 0.0F) {
         return 0.0F;
      }

      if (target.method_6061(source)) {
         return 0.0F;
      }

      if (source.method_48789(class_8103.field_42252) && target.method_5864().method_20210(class_3483.field_29826)) {
         f *= 5.0F;
      }

      if (target.field_6008 > 10 && !source.method_48789(class_8103.field_42969)) {
         float lastHurt = ((LivingEntityLastHurtAccessor)target).apoli$getLastHurt();
         if (f <= lastHurt) {
            return 0.0F;
         }

         f -= lastHurt;
      }

      f = afterArmor(target, attacker, source, f, level);
      f = afterMagic(target, source, f, level);
      return Math.max(f - target.method_6067(), 0.0F);
   }

   private static float afterArmor(class_1309 target, @Nullable class_1309 attacker, class_1282 source, float f, class_1937 level) {
      if (source.method_48789(class_8103.field_42241)) {
         return f;
      } else {
         return !ModifyDamageHandler.shouldApplyArmor(target, attacker, source, f, level)
            ? f
            : class_1280.method_5496(f, target.method_6096(), (float)target.method_26825(class_5134.field_23725));
      }
   }

   private static float afterMagic(class_1309 target, class_1282 source, float f, class_1937 level) {
      if (source.method_48789(class_8103.field_42243)) {
         return f;
      }

      if (target.method_6059(class_1294.field_5907) && !source.method_48789(class_8103.field_42244)) {
         int amplified = (target.method_6112(class_1294.field_5907).method_5578() + 1) * 5;
         f = Math.max(f * (25 - amplified) / 25.0F, 0.0F);
      }

      if (f <= 0.0F) {
         return 0.0F;
      }

      if (source.method_48789(class_8103.field_42245)) {
         return f;
      }

      int protection = class_1890.method_8219(target.method_5661(), source);
      return protection > 0 ? class_1280.method_5497(f, protection) : f;
   }

   private static boolean preventsDeath(class_1309 target, class_1282 source, float amount) {
      PowerContainer container = PowerContainer.of(target);
      if (container != null && !container.isEmpty()) {
         class_1937 level = target.method_37908();
         DamageCtx damageCtx = null;
         Iterator var6 = container.allPowers().iterator();

         while (true) {
            if (!var6.hasNext()) {
               return false;
            }

            class_2960 powerId = (class_2960)var6.next();
            Power power = ApoliPowers.get(powerId);
            if (power != null
               && !container.isSuppressed(powerId)
               && PowerTypeRegistry.get(power.typeId()) instanceof PreventDeathPower
               && power.config() instanceof Config cfg
               && (!power.condition().isPresent() || ((EntityCondition)power.condition().get()).test(new EntityCtx(target, level)))) {
               if (!cfg.damageCondition().isPresent()) {
                  break;
               }

               if (damageCtx == null) {
                  damageCtx = new DamageCtx(source, target, level, amount);
               }

               if (((DamageCondition)cfg.damageCondition().get()).test(damageCtx)) {
                  break;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public static class_1282 source(class_1937 level, class_2960 damageType, @Nullable class_1297 attacker) {
      class_6883<class_8110> holder = (class_6883<class_8110>)level.method_30349()
         .method_30530(class_7924.field_42534)
         .method_40264(class_5321.method_29179(class_7924.field_42534, damageType))
         .orElse(null);
      if (holder == null) {
         return null;
      } else {
         return attacker == null ? new class_1282(holder) : new class_1282(holder, attacker);
      }
   }
}
