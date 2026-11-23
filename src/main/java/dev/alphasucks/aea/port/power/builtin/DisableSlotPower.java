package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.ItemSlot;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class DisableSlotPower extends PowerType<DisableSlotPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("disable_slot");
   public static final int INVENTORY_SIZE = 41;

   @Override
   public MapCodec<DisableSlotPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               ItemSlot.CODEC.optionalFieldOf("slot").forGetter(c -> Optional.empty()),
               ItemSlot.CODEC.listOf().optionalFieldOf("slots").forGetter(c -> Optional.of(c.slots())),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(DisableSlotPower.Config::itemCondition)
            )
            .apply(i, DisableSlotPower::build)
      );
   }

   private static DisableSlotPower.Config build(Optional<ItemSlot> slot, Optional<List<ItemSlot>> slots, Optional<ItemCondition> itemCondition) {
      Set<ItemSlot> merged = new LinkedHashSet();
      slot.ifPresent(merged::add);
      slots.ifPresent(merged::addAll);
      Set<ItemSlot> supported = new LinkedHashSet();
      boolean mainHand = false;

      for (ItemSlot s : merged) {
         if (s.index() == 98) {
            mainHand = true;
         } else {
            int index = s.toContainerIndex();
            if (index >= 0 && index < 41) {
               supported.add(s);
            } else {
               Apoli.LOGGER
                  .warn(
                     "[Apoli] apoli:disable_slot ignores '{}' — it only covers the player's own inventory (hotbar.*, inventory.*, armor.*, weapon.*).",
                     s.name()
                  );
            }
         }
      }

      return new DisableSlotPower.Config(List.copyOf(supported), itemCondition, mainHand);
   }

   public void onAdded(class_2960 powerId, DisableSlotPower.Config cfg, PowerContainer holder, class_2960 source) {
      vacate(holder.owner(), cfg);
   }

   public void onUnsuppressed(class_2960 powerId, DisableSlotPower.Config cfg, PowerContainer holder) {
      vacate(holder.owner(), cfg);
   }

   private static void vacate(@Nullable class_1309 holder, DisableSlotPower.Config cfg) {
      if (holder instanceof class_1657 player && !player.method_37908().method_8608()) {
         class_1661 inventory = player.method_31548();
         int selected = inventory.field_7545;
         if (cfg.mainHand()) {
            relocate(player, inventory, cfg, selected);
         }

         List<ItemSlot> slots = cfg.slots();

         for (int i = 0; i < slots.size(); i++) {
            relocate(player, inventory, cfg, ((ItemSlot)slots.get(i)).toContainerIndex());
         }
      }
   }

   private static void relocate(class_1657 player, class_1661 inventory, DisableSlotPower.Config cfg, int index) {
      if (index >= 0 && index < 41) {
         class_1799 stack = inventory.method_5438(index);
         if (!stack.method_7960() && cfg.blocks(stack, player)) {
            inventory.method_5447(index, class_1799.field_8037);
            if (!giveBack(player, stack) && !stack.method_7960()) {
               player.method_7329(stack, false, true);
            }
         }
      }
   }

   public static boolean has(@Nullable class_1297 entity) {
      return PowerLookup.hasActive(entity, CANONICAL);
   }

   public static boolean disabled(class_1657 player, int containerIndex, class_1799 stack) {
      int selected = player.method_31548().field_7545;
      boolean[] hit = new boolean[]{false};
      PowerLookup.forEach(player, CANONICAL, DisableSlotPower.Config.class, cfg -> {
         if (!hit[0]) {
            if (cfg.covers(containerIndex, selected)) {
               if (stack.method_7960() || cfg.blocks(stack, player)) {
                  hit[0] = true;
               }
            }
         }
      });
      return hit[0];
   }

   public static boolean giveBack(class_1657 player, class_1799 stack) {
      class_1661 inventory = player.method_31548();

      for (int index = 0; index < 36 && !stack.method_7960(); index++) {
         class_1799 existing = inventory.method_5438(index);
         if (!existing.method_7960() && class_1799.method_31577(existing, stack) && !disabled(player, index, stack)) {
            int room = Math.min(stack.method_7914(), inventory.method_5444()) - existing.method_7947();
            if (room > 0) {
               int moved = Math.min(room, stack.method_7947());
               existing.method_7933(moved);
               stack.method_7934(moved);
            }
         }
      }

      for (int index = 0; index < 36 && !stack.method_7960(); index++) {
         if (inventory.method_5438(index).method_7960() && !disabled(player, index, stack)) {
            inventory.method_5447(index, stack.method_51164());
         }
      }

      return stack.method_7960();
   }

   public record Config(List<ItemSlot> slots, Optional<ItemCondition> itemCondition, boolean mainHand) {
      public boolean blocks(class_1799 stack, class_1657 holder) {
         return this.itemCondition.isEmpty() ? true : ((ItemCondition)this.itemCondition.get()).test(new ItemCtx(stack, holder.method_37908(), holder));
      }

      public boolean covers(int containerIndex, int selected) {
         if (this.mainHand && containerIndex == selected) {
            return true;
         }

         for (int i = 0; i < this.slots.size(); i++) {
            if (((ItemSlot)this.slots.get(i)).toContainerIndex() == containerIndex) {
               return true;
            }
         }

         return false;
      }
   }
}
