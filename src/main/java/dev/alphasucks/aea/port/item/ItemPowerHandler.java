package dev.alphasucks.aea.port.item;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.PowerContainerAttachment;
import dev.alphasucks.aea.port.access.ItemPowerSlots;
import dev.alphasucks.aea.port.data.EquipmentSlot;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import io.github.apace100.apoli.component.PowerHolderComponent;
import java.util.HashSet;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

/**
 * Grants the powers in Overgrown-format item NBT ({@link ItemPowers}) while the item is worn or held, with the
 * source {@code apoli:item/<slot>}; resource/cooldown values are written back to the item when it is taken off.
 * AEA: Apoli 2.9.2's own item powers ({@code Power} entries, source {@code apoli:<slot>}) stay Apoli's job.
 */
public final class ItemPowerHandler {
   private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
   private static final class_2960[] SOURCES = new class_2960[SLOTS.length];
   private static boolean registered;

   private ItemPowerHandler() {
   }

   /** AEA: Overgrown wires these two events in its main initialiser. */
   public static void register() {
      if (registered) {
         return;
      }
      registered = true;
      ServerEntityEvents.EQUIPMENT_CHANGE.register((living, slot, previous, next) -> onEquipmentChange(living, slot));
      ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
         if (entity instanceof class_1309 living) {
            onLoad(living);
         }
      });
   }

   public static class_1799[] newSlotArray() {
      return new class_1799[SLOTS.length];
   }

   public static void onLoad(class_1309 entity) {
      if (!entity.method_37908().method_8608() && entity instanceof ItemPowerSlots slots) {
         // AEA: chunk loads fire this for every mob; skip the common case of no powers anywhere
         PowerContainer existing = PowerContainer.of(entity);
         if ((existing == null || existing.isEmpty()) && !wearsItemPowers(entity)) {
            return;
         }
         class_1799[] lastStacks = slots.apoli$itemPowerStacks();

         for (int i = 0; i < SLOTS.length; i++) {
            reconcileSlot(entity, i, lastStacks);
         }
      }
   }

   private static boolean wearsItemPowers(class_1309 entity) {
      for (EquipmentSlot slot : SLOTS) {
         if (ItemPowers.read(entity.method_6118(slot.vanilla())) != null) {
            return true;
         }
      }
      return false;
   }

   public static void onEquipmentChange(class_1309 entity, class_1304 vanillaSlot) {
      if (!entity.method_37908().method_8608() && entity instanceof ItemPowerSlots slots) {
         for (int i = 0; i < SLOTS.length; i++) {
            if (SLOTS[i].vanilla() == vanillaSlot) {
               reconcileSlot(entity, i, slots.apoli$itemPowerStacks());
               return;
            }
         }
      }
   }

   private static void reconcileSlot(class_1309 entity, int index, class_1799[] lastStacks) {
      EquipmentSlot slot = SLOTS[index];
      class_1799 stack = entity.method_6118(slot.vanilla());
      class_1799 previous = lastStacks[index];
      lastStacks[index] = stack;
      Set<class_2960> desired = ItemPowers.powerIdsForSlot(stack, slot);
      PowerContainer container = desired.isEmpty() ? PowerContainer.of(entity) : PowerContainerAttachment.getOrCreate(entity);
      if (container != null && (!container.isEmpty() || !desired.isEmpty())) {
         class_2960 source = SOURCES[index];
         Set<class_2960> current = currentFromSource(container, source);

         for (class_2960 p : current) {
            if (!desired.contains(p)) {
               OptionalInt value = container.getAuxInt(p);
               if (value.isPresent() && previous != null) {
                  ItemPowers.saveValue(previous, p, slot, value.getAsInt());
               }

               container.removePower(p, source);
            }
         }

         for (class_2960 p : desired) {
            if (!current.contains(p)) {
               boolean hadAux = container.getAuxInt(p).isPresent();
               container.addPower(p, source);
               if (!hadAux && container instanceof PowerContainerImpl impl) {
                  OptionalInt saved = ItemPowers.readValue(stack, p, slot);
                  if (saved.isPresent()) {
                     impl.setAuxInt(p, saved.getAsInt());
                  }
               }
            }
         }
      }
   }

   private static Set<class_2960> currentFromSource(PowerContainer container, class_2960 source) {
      Set<class_2960> out = null;
      // AEA: on Apoli 2.9.2 a multiple power's sub-powers carry the same source; listing them here would remove
      // them on the next reconcile (they are never "desired"), leaving the parent power without its parts.
      if (container instanceof PowerContainerImpl impl) {
         PowerHolderComponent component = impl.component();
         for (io.github.apace100.apoli.power.PowerType<?> type : component.getPowerTypes(false)) {
            if (component.getSources(type).contains(source)) {
               if (out == null) {
                  out = new HashSet();
               }

               out.add(type.getIdentifier());
            }
         }
         return out == null ? Set.of() : out;
      }

      for (class_2960 p : container.allPowers()) {
         if (container.sourcesOf(p).contains(source)) {
            if (out == null) {
               out = new HashSet();
            }

            out.add(p);
         }
      }

      return out == null ? Set.of() : out;
   }

   static {
      for (int i = 0; i < SLOTS.length; i++) {
         SOURCES[i] = Apoli.id("item/" + SLOTS[i].method_15434());
      }
   }
}
