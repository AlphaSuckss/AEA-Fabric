package dev.alphasucks.aea.port.condition.builtin.entity;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.accessory.condition.entity.AccessoryEquippedCountCondition;
import dev.alphasucks.aea.port.compat.accessory.condition.entity.AccessorySlotCountCondition;
import dev.alphasucks.aea.port.compat.hardcorerevival.condition.KnockedOutCondition;
import dev.alphasucks.aea.port.compat.walkers.condition.ShapeConditions.CanUseShapeAbility;
import dev.alphasucks.aea.port.compat.walkers.condition.ShapeConditions.HasShapeAbility;
import dev.alphasucks.aea.port.compat.walkers.condition.ShapeConditions.Shape;
import dev.alphasucks.aea.port.compat.walkers.condition.ShapeConditions.ShapeAbilityCooldown;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.builtin.entity.HungerCondition.Kind;
import dev.alphasucks.aea.port.condition.builtin.entity.XpCondition.Unit;
import dev.alphasucks.aea.port.condition.builtin.meta.SendConditionMeta.Entity;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class EntityConditions {
   private EntityConditions() {
   }

   public static void register() {
      ConditionTypes.ENTITY
         .register(
            Apoli.id("attack_charge"),
            new AttackChargeCondition(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("spam_attack")).addTypeAlias(Apoli.id("attack_cooldown")).build()
         );
      ConditionTypes.ENTITY.register(Apoli.id("tick_rate"), new TickRateCondition());
      ConditionTypes.ENTITY.register(Apoli.id("scale"), new ScaleCondition());
      ConditionTypes.ENTITY.register(Apoli.id("has_location"), new HasLocationCondition());
      ConditionTypes.ENTITY.register(Apoli.id("player_model_type"), new PlayerModelTypeCondition());
      ConditionTypes.ENTITY
         .register(
            Apoli.id("key_pressed"),
            new KeyPressedCondition(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("key_held")).addTypeAlias(Apoli.id("held_key")).build()
         );
      ConditionTypes.ENTITY
         .register(Apoli.id("entity_set_size"), new EntitySetSizeCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("set_size")).build());
      ConditionTypes.ENTITY.register(Apoli.id("grabbed"), new GrabbedCondition());
      ConditionTypes.ENTITY
         .register(Apoli.id("in_entity_set"), new EntitySetMembershipCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("in_set")).build());
      ConditionTypes.ENTITY.register(Apoli.id("in_team"), new InTeamCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("team")).build());
      ConditionTypes.ENTITY.register(Apoli.id("owner"), new OwnerCondition());
      ConditionTypes.ENTITY.register(Apoli.id("exists"), SimpleFlagConditions.exists(), AliasingOptions.builder().addTypeAlias(Apoli.id("online")).build());
      ConditionTypes.ENTITY.register(Apoli.id("in_snow"), SimpleFlagConditions.inSnow());
      ConditionTypes.ENTITY.register(Apoli.id("in_thunderstorm"), SimpleFlagConditions.inThunder());
      ConditionTypes.ENTITY.register(Apoli.id("damage_would_kill"), new DamageWouldKillCondition());
      ConditionTypes.ENTITY.register(Apoli.id("hunger"), new HungerCondition(Kind.FOOD));
      ConditionTypes.ENTITY.register(Apoli.id("xp"), new XpCondition(Unit.LEVELS));
      ConditionTypes.ENTITY.register(Apoli.id("perspective"), new PerspectiveCondition());
      ConditionTypes.ENTITY.register(Apoli.id("entity_in_radius"), new EntityInRadiusCondition());
      ConditionTypes.ENTITY.register(Apoli.id("stored_power"), new StoredPowerCondition());
      ConditionTypes.ENTITY
         .register(
            Apoli.id("stat"),
            new StatCondition(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("stats"))
               .addTypeAlias(Apoli.id("status"))
               .addTypeAlias(Apoli.id("statistic"))
               .renameField("status", "stat")
               .build()
         );
      ConditionTypes.ENTITY.register(Apoli.id("distance_from_coordinates"), new DistanceFromCoordinatesCondition());
      ConditionTypes.ENTITY.register(Apoli.id("attached_to_rope"), new AttachedToRopeCondition());
      ConditionTypes.ENTITY.register(Apoli.id("disguised"), new DisguisedCondition());
      if (ModCompat.anyAccessory()) {
         ConditionTypes.ENTITY
            .register(
               Apoli.id("accessory_equipped_count"),
               new AccessoryEquippedCountCondition(),
               AliasingOptions.builder().addTypeAlias(Apoli.id("equipped_trinket_count")).build()
            );
         ConditionTypes.ENTITY
            .register(
               Apoli.id("accessory_slot_count"),
               new AccessorySlotCountCondition(),
               AliasingOptions.builder().addTypeAlias(Apoli.id("trinket_slot_count")).build()
            );
      }
      if (ModCompat.HARDCORE_REVIVAL) {
         ConditionTypes.ENTITY.register(Apoli.id("knocked_out"), new KnockedOutCondition());
      }
      ConditionTypes.ENTITY.register(Apoli.id("has_command_tag"), new HasCommandTagCondition());
      ConditionTypes.ENTITY.register(Apoli.id("difficulty"), new DifficultyCondition());
      ConditionTypes.ENTITY
         .register(
            Apoli.id("sound_playing"),
            new SoundPlayingCondition(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("playing_sound")).addTypeAlias(Apoli.id("hearing_sound")).build()
         );
      ConditionTypes.ENTITY.register(Apoli.id("script"), new ScriptCondition());
      ConditionTypes.ENTITY.register(Apoli.id("send_condition"), new Entity(), AliasingOptions.builder().addTypeAlias("shappoli:send_condition").build());
      if (ModCompat.WALKERS) {
         ConditionTypes.ENTITY.register(Apoli.id("can_use_shape_ability"), new CanUseShapeAbility());
         ConditionTypes.ENTITY.register(Apoli.id("has_shape_ability"), new HasShapeAbility());
         ConditionTypes.ENTITY.register(Apoli.id("shape_ability_cooldown"), new ShapeAbilityCooldown());
         ConditionTypes.ENTITY
            .register(
               Apoli.id("shape_condition"),
               new Shape(),
               AliasingOptions.builder().addTypeAlias(Apoli.id("shape")).addTypeAlias("shappoli:shape_condition").addTypeAlias("shappoli:shape").build()
            );
      }
   }
}
