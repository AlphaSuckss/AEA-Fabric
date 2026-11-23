package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.InventoryType;
import dev.alphasucks.aea.port.data.ItemSlot;
import dev.alphasucks.aea.port.power.builtin.InventoryPower;
import java.util.Optional;
import net.minecraft.class_1277;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3542;
import net.minecraft.class_5630;

public final class ChangeSlotAction implements ActionType<EntityCtx, ChangeSlotAction.Cfg> {
   @Override
   public MapCodec<ChangeSlotAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ItemSlot.CODEC.fieldOf("slot_a").forGetter(ChangeSlotAction.Cfg::slotA),
               ItemSlot.CODEC.fieldOf("slot_b").forGetter(ChangeSlotAction.Cfg::slotB),
               ChangeSlotAction.Operation.CODEC.optionalFieldOf("operation", ChangeSlotAction.Operation.SWAP).forGetter(ChangeSlotAction.Cfg::operation),
               InventoryType.CODEC.optionalFieldOf("inventory_type", InventoryType.INVENTORY).forGetter(ChangeSlotAction.Cfg::inventoryType),
               IdCodecs.ID.optionalFieldOf("power").forGetter(ChangeSlotAction.Cfg::power)
            )
            .apply(i, ChangeSlotAction.Cfg::new)
      );
   }

   public void run(ChangeSlotAction.Cfg cfg, EntityCtx ctx) {
      class_1297 entity = ctx.entity();
      if (entity != null && !entity.method_37908().method_8608()) {
         if (cfg.inventoryType() != InventoryType.POWER) {
            runOnEntity(cfg, entity);
         } else if (!cfg.power().isEmpty() && entity instanceof class_1309 living) {
            runOnPower(cfg, living, (class_2960)cfg.power().get());
         }
      }
   }

   private static class_5630 accessFor(class_1297 entity, ItemSlot slot) {
      if (entity instanceof class_1309 living) {
         class_1304 equipment = switch (slot.index()) {
            case 98 -> class_1304.field_6173;
            case 99 -> class_1304.field_6171;
            case 100 -> class_1304.field_6166;
            case 101 -> class_1304.field_6172;
            case 102 -> class_1304.field_6174;
            case 103 -> class_1304.field_6169;
            default -> null;
         };
         if (equipment != null) {
            return class_5630.method_32330(living, equipment);
         }
      }

      return entity.method_32318(slot.index());
   }

   private static void runOnEntity(ChangeSlotAction.Cfg cfg, class_1297 entity) {
      class_5630 refA = accessFor(entity, cfg.slotA());
      class_5630 refB = accessFor(entity, cfg.slotB());
      if (refA != class_5630.field_27860 && refB != class_5630.field_27860) {
         class_1799 stackA = refA.method_32327().method_7972();
         class_1799 stackB = refB.method_32327().method_7972();
         switch (cfg.operation()) {
            case SWAP:
               if (!refA.method_32332(stackB)) {
                  return;
               }

               if (!refB.method_32332(stackA)) {
                  refA.method_32332(stackA);
                  return;
               }
               break;
            case MOVE:
               if (!refB.method_32332(stackA)) {
                  return;
               }

               refA.method_32332(class_1799.field_8037);
         }

         if (entity instanceof class_3222 serverPlayer) {
            serverPlayer.field_7512.method_7623();
         }
      }
   }

   private static void runOnPower(ChangeSlotAction.Cfg cfg, class_1309 entity, class_2960 powerId) {
      class_1277 container = InventoryPower.openContainer(entity, powerId);
      if (container != null) {
         int size = container.method_5439();
         int slotA = cfg.slotA().toContainerIndex();
         int slotB = cfg.slotB().toContainerIndex();
         if (slotA >= 0 && slotA < size && slotB >= 0 && slotB < size) {
            class_1799 stackA = container.method_5438(slotA).method_7972();
            class_1799 stackB = container.method_5438(slotB).method_7972();
            switch (cfg.operation()) {
               case SWAP:
                  container.method_5447(slotA, stackB);
                  container.method_5447(slotB, stackA);
                  break;
               case MOVE:
                  container.method_5447(slotA, class_1799.field_8037);
                  container.method_5447(slotB, stackA);
            }

            InventoryPower.saveContainer(entity, powerId, container);
         }
      }
   }

   public record Cfg(ItemSlot slotA, ItemSlot slotB, ChangeSlotAction.Operation operation, InventoryType inventoryType, Optional<class_2960> power) {
   }

   public enum Operation implements class_3542 {
      SWAP("swap"),
      MOVE("move");

      public static final Codec<ChangeSlotAction.Operation> CODEC = class_3542.method_28140(ChangeSlotAction.Operation::values);
      private final String name;

      Operation(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
