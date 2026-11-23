package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.EquipmentSlot;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.item.ConjuredItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_3222;

public final class ConjureEquipmentAction implements ActionType<EntityCtx, ConjureEquipmentAction.Cfg> {
   private static final Codec<EquipmentSlot> SLOT_CODEC = Codec.STRING.comapFlatMap(ConjureEquipmentAction::slotByName, EquipmentSlot::method_15434);
   private static final MapCodec<ConjureEquipmentAction.Cfg> INNER = RecordCodecBuilder.mapCodec(
      i -> i.group(
            SLOT_CODEC.optionalFieldOf("slot").forGetter(ConjureEquipmentAction.Cfg::slot),
            AccessorySlot.LIST.optionalFieldOf("accessory_slot", List.of()).forGetter(ConjureEquipmentAction.Cfg::accessorySlots),
            ItemStackData.CODEC.fieldOf("item").forGetter(ConjureEquipmentAction.Cfg::item),
            Codec.BOOL.optionalFieldOf("lock", false).forGetter(ConjureEquipmentAction.Cfg::lock)
         )
         .apply(i, ConjureEquipmentAction.Cfg::new)
   );

   @Override
   public MapCodec<ConjureEquipmentAction.Cfg> codec() {
      return INNER.flatXmap(ConjureEquipmentAction::require, DataResult::success);
   }

   public void run(ConjureEquipmentAction.Cfg cfg, EntityCtx ctx) {
      class_1309 entity = ctx.living();
      if (entity != null && !entity.method_37908().method_8608()) {
         if (cfg.slot.isPresent()) {
            class_1799 stack = cfg.item.stack().method_7972();
            ConjuredItems.mark(stack, cfg.lock);
            entity.method_5673(((EquipmentSlot)cfg.slot.get()).vanilla(), stack);
         }

         if (!cfg.accessorySlots.isEmpty()) {
            AccessorySlotRef ref = firstFreeSlot(entity, cfg.accessorySlots);
            if (ref != null) {
               class_1799 stack = cfg.item.stack().method_7972();
               ConjuredItems.mark(stack, cfg.lock);
               ref.setStack(stack);
            }
         }

         if (entity instanceof class_3222 player) {
            player.field_7498.method_7623();
         }
      }
   }

   private static DataResult<ConjureEquipmentAction.Cfg> require(ConjureEquipmentAction.Cfg cfg) {
      return cfg.slot.isEmpty() && cfg.accessorySlots.isEmpty()
         ? DataResult.error(() -> "apoli:conjure_equipment needs 'slot' (mainhand/offhand/head/chest/legs/feet) or 'accessory_slot'")
         : DataResult.success(cfg);
   }

   private static AccessorySlotRef firstFreeSlot(class_1309 entity, List<AccessorySlot> filter) {
      List<AccessorySlotRef> refs = Accessories.slots(entity, filter);
      if (refs.isEmpty()) {
         return null;
      }

      for (int i = 0; i < refs.size(); i++) {
         AccessorySlotRef ref = (AccessorySlotRef)refs.get(i);
         if (ref.getStack().method_7960()) {
            return ref;
         }
      }

      return (AccessorySlotRef)refs.get(0);
   }

   private static DataResult<EquipmentSlot> slotByName(String raw) {
      String name = raw.startsWith("slot.") ? raw.substring(5) : raw;

      for (EquipmentSlot slot : EquipmentSlot.values()) {
         if (slot.method_15434().equals(name) || slot.name().equals(name)) {
            return DataResult.success(slot);
         }
      }

      return DataResult.error(() -> "Unknown equipment slot '" + raw + "' (expected mainhand/offhand/head/chest/legs/feet, optionally prefixed with 'slot.')");
   }

   public record Cfg(Optional<EquipmentSlot> slot, List<AccessorySlot> accessorySlots, ItemStackData item, boolean lock) {
   }
}
