package dev.alphasucks.aea.port.action.builtin.item;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.builtin.meta.SendActionMeta.Item;
import dev.alphasucks.aea.port.alias.AliasingOptions;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class ItemActions {
   private ItemActions() {
   }

   public static void register() {
      ActionTypes.ITEM.register(Apoli.id("holder_action"), new HolderActionItemAction(), AliasingOptions.builder().addTypeAlias(Apoli.id("holder")).build());
      ActionTypes.ITEM.register(Apoli.id("store_data"), new StoreDataItemAction());
      ActionTypes.ITEM.register(Apoli.id("cooldown"), new CooldownItemAction());
      ActionTypes.ITEM.register(Apoli.id("script"), new ScriptItemAction());
      ActionTypes.ITEM.register(Apoli.id("send_action"), new Item(), AliasingOptions.builder().addTypeAlias("shappoli:send_action").build());
   }
}
