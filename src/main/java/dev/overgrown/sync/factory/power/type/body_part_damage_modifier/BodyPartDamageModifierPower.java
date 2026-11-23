package dev.overgrown.sync.factory.power.type.body_part_damage_modifier;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.BodyPartModifierEntry;
import dev.overgrown.sync.factory.power.type.body_part_damage_modifier.utils.HitLocationTracker;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import java.util.List;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_3222;
import net.minecraft.class_3545;
import net.minecraft.class_8111;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BodyPartDamageModifierPower extends Power {
   private static final Logger LOGGER = LoggerFactory.getLogger("sync/BodyPartDmgMod");
   private final List<BodyPartModifierEntry> entries;
   private final ConditionFactory<class_3545<class_1282, Float>>.Instance damageCondition;
   private final boolean requireHitData;
   private final boolean showHitLocation;

   public BodyPartDamageModifierPower(
      PowerType<?> type,
      class_1309 entity,
      List<BodyPartModifierEntry> entries,
      ConditionFactory<class_3545<class_1282, Float>>.Instance damageCondition,
      boolean requireHitData,
      boolean showHitLocation
   ) {
      super(type, entity);
      this.entries = entries;
      this.damageCondition = damageCondition;
      this.requireHitData = requireHitData;
      this.showHitLocation = showHitLocation;
   }

   public float apply(float amount, class_1282 source) {
      return this.apply(amount, source, HitLocationTracker.getAndClear(this.entity));
   }

   /** @param trackedHit the hit location read (and cleared) once for this damage event, or null */
   public float apply(float amount, class_1282 source, class_243 trackedHit) {
      if (!this.isActive()) {
         return amount;
      }

      if (this.damageCondition != null && !this.damageCondition.test(new class_3545<>(source, amount))) {
         return amount;
      }

      class_243 norm = this.resolveHitLocation(source, trackedHit);
      if (norm == null) {
         return amount;
      }

      double xNorm = norm.field_1352;
      double yNorm = norm.field_1351;
      double zNorm = norm.field_1350;
      double result = amount;
      String matchedZone = null;
      class_1297 attacker = source != null ? source.method_5529() : null;

      for (BodyPartModifierEntry entry : this.entries) {
         if (entry.getRegion().contains(xNorm, yNorm, zNorm)) {
            result = ModifierUtil.applyModifiers(this.entity, entry.getModifiers(), result);
            if (entry.getBientityAction() != null && attacker != null) {
               entry.getBientityAction().accept(new class_3545<>(attacker, this.entity));
            }

            if (this.showHitLocation && matchedZone == null) {
               matchedZone = classifyZone(xNorm, yNorm, zNorm);
            }
         }
      }

      if (this.showHitLocation) {
         String zone = matchedZone != null ? matchedZone : classifyZone(xNorm, yNorm, zNorm);
         String msg = String.format("[BodyPart] Zone=%s x=%.2f y=%.2f z=%.2f | dmg %.1f -> %.1f", new Object[]{zone, xNorm, yNorm, zNorm, amount, result});
         LOGGER.info(msg);
         if (this.entity instanceof class_3222 spe) {
            spe.method_7353(class_2561.method_43470(msg), false);
         }

         if (attacker instanceof class_3222 attackerPlayer && attackerPlayer != this.entity) {
            attackerPlayer.method_7353(class_2561.method_43470(msg), false);
         }
      }

      return (float)Math.max(0.0, result);
   }

   private class_243 resolveHitLocation(class_1282 source, class_243 tracked) {
      if (tracked != null) {
         return tracked;
      }

      if (this.requireHitData) {
         return null;
      }

      if (source == null) {
         return this.randomBodyZone();
      }

      if (!source.method_49708(class_8111.field_42345) && !source.method_49708(class_8111.field_42339) && !source.method_49708(class_8111.field_42355)) {
         if (source.method_49708(class_8111.field_42342) || source.method_49708(class_8111.field_42343)) {
            return new class_243(0.0, 0.69, 0.0);
         } else {
            return !source.method_49708(class_8111.field_42346)
                  && !source.method_49708(class_8111.field_42357)
                  && !source.method_49708(class_8111.field_42356)
                  && !source.method_49708(class_8111.field_42358)
               ? this.randomBodyZone()
               : new class_243(0.0, 0.94, 0.0);
         }
      } else {
         return this.entity.method_6051().method_43056() ? new class_243(0.0, 0.09, 0.0) : new class_243(0.0, 0.34, 0.0);
      }
   }

   private class_243 randomBodyZone() {
      int zone = this.entity.method_6051().method_43048(8);

      return switch (zone) {
         case 0 -> new class_243(0.0, 0.94, 0.0);
         case 1 -> new class_243(0.0, 0.69, 0.0);
         case 2 -> new class_243(0.9, 0.74, 0.0);
         case 3 -> new class_243(-0.9, 0.74, 0.0);
         case 4, 5 -> new class_243(0.0, 0.34, 0.0);
         case 6, 7 -> new class_243(0.0, 0.09, 0.0);
         default -> new class_243(0.0, 0.69, 0.0);
      };
   }

   private static String classifyZone(double x, double y, double z) {
      if (y >= 0.88) {
         return "Head";
      }

      if (y >= 0.5) {
         if (y >= 0.6 && x > 0.8) {
            return "Left Arm";
         } else {
            return y >= 0.6 && x < -0.8 ? "Right Arm" : "Torso";
         }
      } else {
         return y >= 0.18 ? "Legs" : "Feet";
      }
   }

   public static PowerFactory<BodyPartDamageModifierPower> getFactory() {
      PowerFactory<BodyPartDamageModifierPower> factory = new PowerFactory<>(
         Sync.identifier("body_part_damage_modifier"),
         new SerializableData()
            .add("modifiers", BodyPartModifierEntry.LIST_TYPE)
            .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
            .add("require_hit_data", SerializableDataTypes.BOOLEAN, false)
            .add("show_hit_location", SerializableDataTypes.BOOLEAN, false),
         data -> (type, player) -> new BodyPartDamageModifierPower(
            type, player, data.get("modifiers"), data.get("damage_condition"), data.getBoolean("require_hit_data"), data.getBoolean("show_hit_location")
         )
      );
      factory.allowCondition();
      return factory;
   }
}
