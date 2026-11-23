package dev.alphasucks.aea.port.compat.accessory.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1799;

public final class ModifyAccessoryAction implements ActionType<EntityCtx, ModifyAccessoryAction.Cfg> {
   @Override
   public MapCodec<ModifyAccessoryAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               AccessorySlot.LIST.optionalFieldOf("slots", List.of()).forGetter(ModifyAccessoryAction.Cfg::slots),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(ModifyAccessoryAction.Cfg::itemCondition),
               LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(ModifyAccessoryAction.Cfg::itemAction),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(ModifyAccessoryAction.Cfg::entityAction),
               Codec.INT.optionalFieldOf("limit", 0).forGetter(ModifyAccessoryAction.Cfg::limit),
               Codec.BOOL.optionalFieldOf("unequip", false).forGetter(ModifyAccessoryAction.Cfg::unequip)
            )
            .apply(i, ModifyAccessoryAction.Cfg::new)
      );
   }

   public void run(ModifyAccessoryAction.Cfg cfg, EntityCtx ctx) {
      class_1309 e = ctx.living();
      if (e != null) {
         int processed = 0;

         for (AccessorySlotRef ref : Accessories.equipped(e, cfg.slots())) {
            class_1799 stack = ref.getStack();
            ItemCtx itemCtx = new ItemCtx(stack, ctx.level(), e);
            if (!cfg.itemCondition().isPresent() || ((ItemCondition)cfg.itemCondition().get()).test(itemCtx)) {
               cfg.entityAction().ifPresent(a -> a.run(ctx));
               cfg.itemAction().ifPresent(a -> a.run(itemCtx));
               if (cfg.unequip()) {
                  ref.setStack(class_1799.field_8037);
               }

               if (cfg.limit() > 0) {
                  if (++processed >= cfg.limit()) {
                     break;
                  }
               }
            }
         }
      }
   }

   public record Cfg(
      List<AccessorySlot> slots,
      Optional<ItemCondition> itemCondition,
      Optional<ItemAction> itemAction,
      Optional<EntityAction> entityAction,
      int limit,
      boolean unequip
   ) {
   }
}
