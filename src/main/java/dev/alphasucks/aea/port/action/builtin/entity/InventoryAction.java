package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.InventoryType;
import dev.alphasucks.aea.port.data.ItemSlot;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.data.ProcessMode;
import dev.alphasucks.aea.port.power.builtin.InventoryPower;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1263;
import net.minecraft.class_1277;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3542;

public final class InventoryAction implements ActionType<EntityCtx, InventoryAction.Cfg> {
   @Override
   public MapCodec<InventoryAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               InventoryAction.Operation.CODEC.optionalFieldOf("operation", InventoryAction.Operation.MODIFY).forGetter(InventoryAction.Cfg::operation),
               InventoryType.CODEC.optionalFieldOf("inventory_type", InventoryType.INVENTORY).forGetter(InventoryAction.Cfg::inventoryType),
               LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(InventoryAction.Cfg::entityAction),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(InventoryAction.Cfg::itemCondition),
               ItemSlot.CODEC.optionalFieldOf("slot").forGetter(InventoryAction.Cfg::slot),
               Codec.list(ItemSlot.CODEC).optionalFieldOf("slots").forGetter(InventoryAction.Cfg::slots),
               IdCodecs.ID.optionalFieldOf("power").forGetter(InventoryAction.Cfg::power),
               ProcessMode.CODEC.optionalFieldOf("process_mode", ProcessMode.STACKS).forGetter(InventoryAction.Cfg::processMode),
               Codec.INT.optionalFieldOf("limit", 0).forGetter(InventoryAction.Cfg::limit),
               LoggedOptionalField.of("item_action", ItemAction.CODEC).forGetter(InventoryAction.Cfg::itemAction),
               ItemStackData.CODEC.optionalFieldOf("stack").forGetter(InventoryAction.Cfg::stack),
               Codec.BOOL.optionalFieldOf("merge_nbt", false).forGetter(InventoryAction.Cfg::mergeNbt),
               Codec.BOOL.optionalFieldOf("throw_randomly", false).forGetter(InventoryAction.Cfg::throwRandomly),
               Codec.BOOL.optionalFieldOf("retain_ownership", true).forGetter(InventoryAction.Cfg::retainOwnership),
               Codec.INT.optionalFieldOf("amount").forGetter(InventoryAction.Cfg::amount)
            )
            .apply(i, InventoryAction.Cfg::new)
      );
   }

   public void run(InventoryAction.Cfg cfg, EntityCtx ctx) {
      class_1309 entity = ctx.living();
      if (entity != null) {
         cfg.entityAction.ifPresent(a -> a.run(ctx));
         if (cfg.inventoryType == InventoryType.POWER) {
            if (!cfg.power.isEmpty()) {
               class_2960 powerId = (class_2960)cfg.power.get();
               class_1277 container = InventoryPower.openContainer(entity, powerId);
               if (container != null) {
                  this.apply(cfg, container, entity, ctx);
                  InventoryPower.saveContainer(entity, powerId, container);
               }
            }
         } else if (entity instanceof class_1657 player) {
            this.apply(cfg, player.method_31548(), entity, ctx);
         }
      }
   }

   private void apply(InventoryAction.Cfg cfg, class_1263 container, class_1309 entity, EntityCtx ctx) {
      ProcessMode mode = cfg.operation == InventoryAction.Operation.MODIFY ? cfg.processMode : ProcessMode.STACKS;
      InventoryHelper.walk(
         container, entity, cfg.itemCondition, cfg.slot, cfg.slots, mode, cfg.limit, cfg.operation == InventoryAction.Operation.REPLACE, (idx, stack) -> {
            switch (cfg.operation) {
               case MODIFY:
                  cfg.itemAction.ifPresent(a -> a.run(new ItemCtx(stack, ctx.level(), entity)));
                  break;
               case REPLACE:
                  if (cfg.stack.isEmpty()) {
                     return;
                  }

                  class_1799 replacement = ((ItemStackData)cfg.stack.get()).stack().method_7972();
                  if (cfg.mergeNbt && stack.method_7985() && replacement.method_7909() != class_1802.field_8162) {
                     class_2487 merged = stack.method_7969().method_10553();
                     if (replacement.method_7985()) {
                        merged.method_10543(replacement.method_7969());
                     }

                     replacement.method_7980(merged);
                  }

                  container.method_5447(idx, replacement);
                  cfg.itemAction.ifPresent(a -> a.run(new ItemCtx(replacement, ctx.level(), entity)));
                  break;
               case DROP:
                  int drop = Math.min((Integer)cfg.amount.orElse(stack.method_7947()), stack.method_7947());
                  class_1799 split = stack.method_7971(drop);
                  cfg.itemAction.ifPresent(a -> a.run(new ItemCtx(split, ctx.level(), entity)));
                  dropStack(entity, split, cfg.throwRandomly, cfg.retainOwnership);
                  container.method_5447(idx, stack);
            }
         }
      );
   }

   private static void dropStack(class_1309 entity, class_1799 stack, boolean throwRandomly, boolean retainOwnership) {
      if (entity instanceof class_1657 player) {
         player.method_7329(stack, throwRandomly, retainOwnership);
      } else {
         entity.method_5775(stack);
      }
   }

   public record Cfg(
      InventoryAction.Operation operation,
      InventoryType inventoryType,
      Optional<EntityAction> entityAction,
      Optional<ItemCondition> itemCondition,
      Optional<ItemSlot> slot,
      Optional<List<ItemSlot>> slots,
      Optional<class_2960> power,
      ProcessMode processMode,
      int limit,
      Optional<ItemAction> itemAction,
      Optional<ItemStackData> stack,
      boolean mergeNbt,
      boolean throwRandomly,
      boolean retainOwnership,
      Optional<Integer> amount
   ) {
   }

   public enum Operation implements class_3542 {
      MODIFY("modify"),
      REPLACE("replace"),
      DROP("drop");

      public static final Codec<InventoryAction.Operation> CODEC = class_3542.method_28140(InventoryAction.Operation::values);
      private final String name;

      Operation(String n) {
         this.name = n;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
