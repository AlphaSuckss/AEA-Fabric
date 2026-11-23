package dev.alphasucks.aea.port.compat.accessory.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public final class ActionOnAccessoryChangePower extends PowerType<ActionOnAccessoryChangePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("action_on_accessory_change");

   @Override
   public MapCodec<ActionOnAccessoryChangePower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.of("entity_action_on_equip", EntityAction.CODEC).forGetter(ActionOnAccessoryChangePower.Config::entityActionOnEquip),
               LoggedOptionalField.of("item_action_on_equip", ItemAction.CODEC).forGetter(ActionOnAccessoryChangePower.Config::itemActionOnEquip),
               LoggedOptionalField.of("entity_action_on_unequip", EntityAction.CODEC).forGetter(ActionOnAccessoryChangePower.Config::entityActionOnUnequip),
               LoggedOptionalField.of("item_action_on_unequip", ItemAction.CODEC).forGetter(ActionOnAccessoryChangePower.Config::itemActionOnUnequip),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ActionOnAccessoryChangePower.Config::itemCondition),
               AccessorySlot.LIST.optionalFieldOf("slots", List.of()).forGetter(ActionOnAccessoryChangePower.Config::slots)
            )
            .apply(i, ActionOnAccessoryChangePower.Config::new)
      );
   }

   public static void handle(class_1309 entity, AccessorySlotRef ref, class_1799 stack, boolean equipping) {
      if (!entity.method_37908().method_8608()) {
         ItemCtx itemCtx = new ItemCtx(stack, entity.method_37908(), entity);
         EntityCtx entityCtx = EntityCtx.of(entity, entity.method_37908());
         PowerLookup.forEach(entity, CANONICAL, ActionOnAccessoryChangePower.Config.class, cfg -> {
            if (AccessorySlot.matchesAny(cfg.slots(), ref)) {
               if (!cfg.itemCondition().isPresent() || ((ItemCondition)cfg.itemCondition().get()).test(itemCtx)) {
                  if (equipping) {
                     cfg.entityActionOnEquip().ifPresent(a -> a.run(entityCtx));
                     cfg.itemActionOnEquip().ifPresent(a -> a.run(itemCtx));
                  } else {
                     cfg.entityActionOnUnequip().ifPresent(a -> a.run(entityCtx));
                     cfg.itemActionOnUnequip().ifPresent(a -> a.run(itemCtx));
                  }
               }
            }
         });
      }
   }

   public record Config(
      Optional<EntityAction> entityActionOnEquip,
      Optional<ItemAction> itemActionOnEquip,
      Optional<EntityAction> entityActionOnUnequip,
      Optional<ItemAction> itemActionOnUnequip,
      Optional<ItemCondition> itemCondition,
      List<AccessorySlot> slots
   ) {
   }
}
