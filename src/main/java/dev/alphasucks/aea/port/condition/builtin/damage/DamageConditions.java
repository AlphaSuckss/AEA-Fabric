package dev.alphasucks.aea.port.condition.builtin.damage;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import net.minecraft.class_8103;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class DamageConditions {
   private DamageConditions() {
   }

   public static void register() {
      ConditionTypes.DAMAGE
         .register(
            Apoli.id("attack_charge"),
            new AttackChargeDamageCondition(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("spam_attack")).addTypeAlias(Apoli.id("attack_cooldown")).build()
         );
      ConditionTypes.DAMAGE.register(Apoli.id("body_part"), new BodyPartDamageCondition());
      ConditionTypes.DAMAGE.register(Apoli.id("critical"), new CriticalDamageCondition());
      ConditionTypes.DAMAGE.register(Apoli.id("source"), new SourceDamageCondition());
      ConditionTypes.DAMAGE.register(Apoli.id("type"), new TypeDamageCondition());
   }
}
