package dev.alphasucks.aea.port.compat.accessory.backend;

import com.google.common.collect.Multimap;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.AccessoryProvider;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import dev.alphasucks.aea.port.compat.accessory.power.PreventAccessoryEquipPower;
import dev.alphasucks.aea.port.compat.accessory.power.PreventAccessoryUnequipPower;
import dev.alphasucks.aea.port.item.ConjuredItems;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.events.AccessoryChangeCallback;
import io.wispforest.accessories.api.events.CanEquipCallback;
import io.wispforest.accessories.api.events.CanUnequipCallback;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.accessories.api.slot.SlotReference;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_1799;
import net.minecraft.class_1937;

public final class AccessoriesBackend implements AccessoryProvider {
   public static final String ID = "accessories";

   @Override
   public String id() {
      return "accessories";
   }

   @Override
   public boolean isPresent() {
      return true;
   }

   private static AccessoriesCapability capability(class_1309 entity) {
      return AccessoriesCapability.get(entity);
   }

   @Override
   public List<AccessorySlotRef> equipped(class_1309 entity) {
      AccessoriesCapability cap = capability(entity);
      if (cap == null) {
         return List.of();
      }

      List<AccessorySlotRef> out = new ArrayList();

      for (SlotEntryReference entry : cap.getAllEquipped()) {
         out.add(new AccessoriesBackend.Ref(entry.reference()));
      }

      return out;
   }

   @Override
   public List<AccessorySlotRef> slots(class_1309 entity) {
      AccessoriesCapability cap = capability(entity);
      if (cap == null) {
         return List.of();
      }

      List<AccessorySlotRef> out = new ArrayList();
      cap.getContainers().forEach((name, container) -> {
         for (int i = 0; i < container.getSize(); i++) {
            out.add(new AccessoriesBackend.Ref(SlotReference.of(entity, name, i)));
         }
      });
      return out;
   }

   @Override
   public class_1799 equip(class_1309 entity, List<AccessorySlot> filter, class_1799 stack) {
      for (AccessorySlotRef ref : this.slots(entity)) {
         if (AccessorySlot.matchesAny(filter, ref) && ref.getStack().method_7960()) {
            ref.setStack(stack.method_7972());
            return class_1799.field_8037;
         }
      }

      return stack;
   }

   @Override
   public boolean isAccessory(class_1799 stack, class_1937 level) {
      return AccessoriesAPI.isValidAccessory(stack, level);
   }

   @Override
   public void applySlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
      AccessoriesCapability cap = capability(entity);
      if (cap != null) {
         cap.addTransientSlotModifiers(modifiers);
      }
   }

   @Override
   public void removeSlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
      AccessoriesCapability cap = capability(entity);
      if (cap != null) {
         cap.removeSlotModifiers(modifiers);
      }
   }

   public void registerEvents() {
      AccessoryChangeCallback.EVENT.register((AccessoryChangeCallback)(prevStack, currentStack, reference, stateChange) -> {
         class_1309 entity = reference.entity();
         if (entity != null && !entity.method_37908().method_8608()) {
            AccessoriesBackend.Ref ref = new AccessoriesBackend.Ref(reference);
            if (prevStack != null && !prevStack.method_7960()) {
               Accessories.fireChange(entity, ref, prevStack, false);
            }

            if (currentStack != null && !currentStack.method_7960()) {
               Accessories.fireChange(entity, ref, currentStack, true);
            }
         }
      });
      CanEquipCallback.EVENT
         .register(
            (CanEquipCallback)(stack, reference) -> {
               class_1309 entity = reference.entity();
               return entity != null && PreventAccessoryEquipPower.isPrevented(entity, new AccessoriesBackend.Ref(reference), stack)
                  ? TriState.FALSE
                  : TriState.DEFAULT;
            }
         );
      CanUnequipCallback.EVENT
         .register(
            (CanUnequipCallback)(stack, reference) -> {
               if (ConjuredItems.isLocked(stack)) {
                  return TriState.FALSE;
               }

               class_1309 entity = reference.entity();
               return entity != null && PreventAccessoryUnequipPower.isPrevented(entity, new AccessoriesBackend.Ref(reference), stack)
                  ? TriState.FALSE
                  : TriState.DEFAULT;
            }
         );
   }

   private record Ref(SlotReference ref) implements AccessorySlotRef {
      @Override
      public String provider() {
         return "accessories";
      }

      @Override
      public String group() {
         return "";
      }

      @Override
      public String type() {
         return this.ref.slotName();
      }

      @Override
      public int index() {
         return this.ref.slot();
      }

      @Override
      public class_1799 getStack() {
         class_1799 s = this.ref.getStack();
         return s == null ? class_1799.field_8037 : s;
      }

      @Override
      public void setStack(class_1799 stack) {
         this.ref.setStack(stack);
      }
   }
}
