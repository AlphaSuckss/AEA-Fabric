package dev.alphasucks.aea.port.condition.builtin.item;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.accessory.condition.item.AccessoryCondition;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.builtin.meta.SendConditionMeta.Item;

/** AEA: filtered copy, registers only types Apoli 2.9.2 lacks. */
public final class ItemConditions {
   private ItemConditions() {
   }

   public static void register() {
      ConditionTypes.ITEM
         .register(Apoli.id("fireproof"), new FireproofItemCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("fire_resistant")).build());
      ConditionTypes.ITEM.register(Apoli.id("fuel"), new FuelItemCondition());
      ConditionTypes.ITEM
         .register(Apoli.id("is_damageable"), new IsDamageableItemCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("damageable")).build());
      ConditionTypes.ITEM
         .register(Apoli.id("is_equippable"), new IsEquippableItemCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("equippable")).build());
      ConditionTypes.ITEM.register(Apoli.id("nbt"), new NbtItemCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("custom_data")).build());
      ConditionTypes.ITEM.register(Apoli.id("relative_item_cooldown"), new RelativeItemCooldownCondition());
      if (ModCompat.anyAccessory()) {
         ConditionTypes.ITEM.register(Apoli.id("accessory"), new AccessoryCondition(), AliasingOptions.builder().addTypeAlias(Apoli.id("trinket")).build());
      }
      ConditionTypes.ITEM
         .register(
            Apoli.id("is_block"),
            new IsBlockItemCondition(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("block")).addTypeAlias("shappoli:is_block").addTypeAlias("shappoli:block").build()
         );
      ConditionTypes.ITEM
         .register(
            Apoli.id("holder_condition"),
            new HolderItemCondition(),
            AliasingOptions.builder().addTypeAlias(Apoli.id("holder")).addTypeAlias("shappoli:holder_condition").addTypeAlias("shappoli:holder").build()
         );
      ConditionTypes.ITEM.register(Apoli.id("script"), new ScriptItemCondition());
      ConditionTypes.ITEM.register(Apoli.id("send_condition"), new Item(), AliasingOptions.builder().addTypeAlias("shappoli:send_condition").build());
   }
}
