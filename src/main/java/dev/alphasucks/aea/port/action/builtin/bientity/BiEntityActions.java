package dev.alphasucks.aea.port.action.builtin.bientity;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.builtin.bientity.StorePowerAction.BiEntity;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.walkers.action.ShapeActions.SwitchShapeBiEntity;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class BiEntityActions {
   private BiEntityActions() {
   }

   public static void register() {
      ActionTypes.BI_ENTITY
         .register(
            Apoli.id("modify_resource"),
            new ModifyResourceBiEntityAction(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("change_resource")).addTypeAlias("origins:change_resource").build()
         );
      ActionTypes.BI_ENTITY.register(Apoli.id("aggro_at"), new AggroAtAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("punch"), new PunchBiEntityAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("teleport_to"), new TeleportToBiEntityAction(false));
      ActionTypes.BI_ENTITY.register(Apoli.id("swap"), new TeleportToBiEntityAction(true));
      ActionTypes.BI_ENTITY.register(Apoli.id("explode"), new ExplodeBiEntityAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("grab"), new GrabAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("breed"), new BreedAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("both"), new BothAction());
      ActionTypes.BI_ENTITY
         .register(Apoli.id("add_to_entity_set"), new AddToEntitySetAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("add_to_set")).build());
      ActionTypes.BI_ENTITY
         .register(
            Apoli.id("remove_from_entity_set"), new RemoveFromEntitySetAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("remove_from_set")).build()
         );
      ActionTypes.BI_ENTITY.register(Apoli.id("disguise"), new DisguiseBiEntityAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("transfer"), new TransferAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("transfer_effects"), new TransferEffectsAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("execute_command"), new ExecuteCommandBiEntityAction());
      ActionTypes.BI_ENTITY.register(Apoli.id("store_power"), new BiEntity());
      ActionTypes.BI_ENTITY.register(Apoli.id("attach_rope"), new dev.alphasucks.aea.port.action.builtin.entity.AttachRopeAction.BiEntity());
      ActionTypes.BI_ENTITY
         .register(Apoli.id("raycast"), new RaycastBiEntityAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("raycast_between")).build());
      ActionTypes.BI_ENTITY.register(Apoli.id("script"), new ScriptBiEntityAction());
      ActionTypes.BI_ENTITY
         .register(
            Apoli.id("send_action"),
            new dev.alphasucks.aea.port.action.builtin.meta.SendActionMeta.BiEntity(),
            AliasingOptions.builder().addTypeAlias("shappoli:send_action").build()
         );
      if (ModCompat.WALKERS) {
         ActionTypes.BI_ENTITY
            .register(
               Apoli.id("switch_shape"),
               new SwitchShapeBiEntity(),
               AliasingOptions.builder().addTypeAlias(Apoli.id("change_shape")).addTypeAlias(Apoli.id("morph")).addTypeAlias("shappoli:switch_shape").build()
            );
      }
   }
}
