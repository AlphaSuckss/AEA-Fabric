package dev.alphasucks.aea.port.action.builtin.entity;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.builtin.bientity.StorePowerAction.Self;
import dev.alphasucks.aea.port.action.builtin.entity.RelativeAction.Target;
import dev.alphasucks.aea.port.action.builtin.meta.SendActionMeta.Entity;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.accessory.action.ModifyAccessoryAction;
import dev.alphasucks.aea.port.compat.hardcorerevival.action.KnockOutAction;
import dev.alphasucks.aea.port.compat.hardcorerevival.action.ReviveAction;
import dev.alphasucks.aea.port.compat.walkers.action.ShapeActions.ChangeShapeAbilityCooldown;
import dev.alphasucks.aea.port.compat.walkers.action.ShapeActions.ShapeAction;
import dev.alphasucks.aea.port.compat.walkers.action.ShapeActions.SwitchShape;
import dev.alphasucks.aea.port.compat.walkers.action.ShapeActions.UseShapeAbility;
import java.util.Map;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class EntityActions {
   private EntityActions() {
   }

   public static void register() {
      ActionTypes.ENTITY.register(Apoli.id("run_function"), new RunFunctionAction());
      ActionTypes.ENTITY
         .register(
            Apoli.id("team"),
            new TeamAction(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("modify_team"), Map.of("operation", "modify"))
               .addTypeAlias(Apoli.id("join_team"), Map.of("operation", "join"))
               .addTypeAlias(Apoli.id("leave_team"), Map.of("operation", "leave"))
               .build()
         );
      ActionTypes.ENTITY.register(Apoli.id("conjure_equipment"), new ConjureEquipmentAction());
      ActionTypes.ENTITY.register(Apoli.id("release_grab"), new ReleaseGrabAction());
      ActionTypes.ENTITY.register(Apoli.id("scale"), new ScaleAction(), AliasingOptions.builder().renameField("scale_type", "scale_types").build());
      ActionTypes.ENTITY.register(Apoli.id("run_stored_power"), new RunStoredPowerAction());
      ActionTypes.ENTITY.register(Apoli.id("store_power"), new Self());
      ActionTypes.ENTITY.register(Apoli.id("store_data"), new StoreDataAction());
      ActionTypes.ENTITY.register(Apoli.id("set_perspective"), new SetPerspectiveAction());
      ActionTypes.ENTITY
         .register(Apoli.id("action_on_entity_set"), new ActionOnEntitySetAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("action_on_set")).build());
      ActionTypes.ENTITY.register(Apoli.id("teleport"), new TeleportAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("teleport_to")).build());
      ActionTypes.ENTITY.register(Apoli.id("save_location"), new SaveLocationAction());
      ActionTypes.ENTITY.register(Apoli.id("teleport_to_location"), new TeleportToLocationAction());
      ActionTypes.ENTITY.register(Apoli.id("teleport_to_spawn"), new TeleportToSpawnAction());
      ActionTypes.ENTITY
         .register(
            Apoli.id("force_key_pressed"),
            new ForceKeyPressedAction(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("press_key")).addTypeAlias(Apoli.id("force_key")).build()
         );
      ActionTypes.ENTITY.register(Apoli.id("freeze"), new FreezeAction());
      ActionTypes.ENTITY.register(Apoli.id("grant_all_powers"), new GrantAllPowersAction());
      ActionTypes.ENTITY.register(Apoli.id("revoke_all_powers"), new RevokeAllPowersAction());
      ActionTypes.ENTITY.register(Apoli.id("remove_power"), new RemovePowerAction());
      ActionTypes.ENTITY
         .register(
            Apoli.id("suppress_power"),
            new SuppressPowerAction(),
            AliasingOptions.builder().renameField("powers", "power").renameField("sources", "source").build()
         );
      ActionTypes.ENTITY.register(Apoli.id("unfreeze"), new UnfreezeAction());
      ActionTypes.ENTITY
         .register(
            Apoli.id("unsuppress_power"),
            new UnsuppressPowerAction(),
            AliasingOptions.builder().renameField("powers", "power").renameField("sources", "source").build()
         );
      ActionTypes.ENTITY
         .register(
            Apoli.id("modify_stat"),
            new ModifyStatAction(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("change_stat")).addTypeAlias(Apoli.id("set_stat")).build()
         );
      ActionTypes.ENTITY
         .register(Apoli.id("text"), new TextAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("text_bar")).addTypeAlias(Apoli.id("show_text")).build());
      ActionTypes.ENTITY
         .register(
            Apoli.id("modify_entity_data"),
            new ModifyEntityDataAction(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("set_entity_data"))
               .addTypeAlias(Apoli.id("change_entity_data"))
               .addTypeAlias(Apoli.id("set_no_gravity"), Map.of("data", "no_gravity"))
               .addTypeAlias(Apoli.id("set_gravity"), Map.of("data", "no_gravity"))
               .build()
         );
      ActionTypes.ENTITY.register(Apoli.id("advancement"), new AdvancementAction(false));
      ActionTypes.ENTITY.register(Apoli.id("relative_action"), new RelativeAction(Target.PASSENGER));
      ActionTypes.ENTITY
         .register(
            Apoli.id("inventory_action"),
            new InventoryAction(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("modify_inventory"))
               .addTypeAlias(Apoli.id("replace_inventory"), Map.of("operation", "replace"))
               .addTypeAlias(Apoli.id("drop_inventory"), Map.of("operation", "drop"))
               .build()
         );
      ActionTypes.ENTITY
         .register(Apoli.id("print"), new PrintAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("log")).addTypeAlias(Apoli.id("logger")).build());
      ActionTypes.ENTITY
         .register(
            Apoli.id("change_slot"),
            new ChangeSlotAction(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("swap_slot"), Map.of("operation", "swap"))
               .addTypeAlias(Apoli.id("move_slot"), Map.of("operation", "move"))
               .build()
         );
      ActionTypes.ENTITY
         .register(
            Apoli.id("change_selected_slot"), new ChangeSelectedSlotAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("set_selected_slot")).build()
         );
      ActionTypes.ENTITY.register(Apoli.id("start_raid"), new StartRaidAction());
      ActionTypes.ENTITY.register(Apoli.id("summon_clone"), new SummonCloneAction());
      ActionTypes.ENTITY.register(Apoli.id("summon_minion"), new SummonMinionAction());
      ActionTypes.ENTITY
         .register(
            Apoli.id("set_summon_max_life"),
            new SetSummonMaxLifeAction(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("set_summon_max_life_ticks")).build()
         );
      ActionTypes.ENTITY
         .register(Apoli.id("disguise_as"), new DisguiseAsAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("disguise_as_player")).build());
      ActionTypes.ENTITY.register(Apoli.id("remove_disguise"), new RemoveDisguiseAction());
      ActionTypes.ENTITY.register(Apoli.id("tick_rate"), new TickRateAction());
      ActionTypes.ENTITY.register(Apoli.id("random_teleport"), new RandomTeleportAction());
      ActionTypes.ENTITY.register(Apoli.id("attach_rope"), new AttachRopeAction());
      ActionTypes.ENTITY.register(Apoli.id("detach_rope"), new DetachRopeAction());
      ActionTypes.ENTITY.register(Apoli.id("rope_pull"), new RopePullAction());
      ActionTypes.ENTITY.register(Apoli.id("reset_skills"), new ResetSkillsAction());
      ActionTypes.ENTITY.register(Apoli.id("radial_menu"), new RadialMenuAction(), AliasingOptions.builder().addTypeAlias("sync:radial_menu").build());
      ActionTypes.ENTITY.register(Apoli.id("add_skill_points"), new AddSkillPointsAction());
      ActionTypes.ENTITY.register(Apoli.id("grant_skill_tree"), new GrantSkillTreeAction());
      ActionTypes.ENTITY.register(Apoli.id("revoke_skill_tree"), new RevokeSkillTreeAction());
      if (ModCompat.anyAccessory()) {
         ActionTypes.ENTITY
            .register(Apoli.id("modify_accessory"), new ModifyAccessoryAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("modify_trinket")).build());
      }
      if (ModCompat.HARDCORE_REVIVAL) {
         ActionTypes.ENTITY.register(Apoli.id("knock_out"), new KnockOutAction());
         ActionTypes.ENTITY.register(Apoli.id("revive"), new ReviveAction());
      }
      ActionTypes.ENTITY.register(Apoli.id("script"), new ScriptAction());
      ActionTypes.ENTITY
         .register(
            Apoli.id("self_bientity_action"),
            new SelfBiEntityAction(),
            AliasingOptions.builder()
               .addTypeAlias(Apoli.id("self_bientity"))
               .addTypeAlias(Apoli.id("bientity_action"))
               .addTypeAlias("shappoli:self_bientity_action")
               .addTypeAlias("shappoli:self_bientity")
               .build()
         );
      ActionTypes.ENTITY.register(Apoli.id("send_action"), new Entity(), AliasingOptions.builder().addTypeAlias("shappoli:send_action").build());
      if (ModCompat.WALKERS) {
         ActionTypes.ENTITY
            .register(
               Apoli.id("change_shape_ability_cooldown"),
               new ChangeShapeAbilityCooldown(),
               AliasingOptions.builder().addTypeAlias("shappoli:change_shape_ability_cooldown").build()
            );
         ActionTypes.ENTITY
            .register(
               Apoli.id("shape_action"),
               new ShapeAction(),
               AliasingOptions.builder()
                  .addTypeAlias(Apoli.id("action_on_shape"))
                  .addTypeAlias("shappoli:shape_action")
                  .addTypeAlias("shappoli:action_on_shape")
                  .build()
            );
         ActionTypes.ENTITY
            .register(
               Apoli.id("switch_shape"),
               new SwitchShape(),
               AliasingOptions.builder()
                  .addTypeAlias(Apoli.id("change_shape"))
                  .addTypeAlias(Apoli.id("morph"))
                  .addTypeAlias("shappoli:switch_shape")
                  .addTypeAlias("shappoli:change_shape")
                  .addTypeAlias("shappoli:morph")
                  .build()
            );
         ActionTypes.ENTITY
            .register(Apoli.id("use_shape_ability"), new UseShapeAbility(), AliasingOptions.builder().addTypeAlias("shappoli:use_shape_ability").build());
      }
   }
}
