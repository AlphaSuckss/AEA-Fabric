package dev.alphasucks.aea.port.compat.accessory.backend;

import com.google.common.collect.Multimap;
import dev.emi.trinkets.TrinketPlayerScreenHandler;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.alphasucks.aea.port.compat.accessory.Accessories;
import dev.alphasucks.aea.port.compat.accessory.AccessoryProvider;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlot;
import dev.alphasucks.aea.port.compat.accessory.AccessorySlotRef;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;

public final class TrinketsBackend implements AccessoryProvider {
   public static final String ID = "trinkets";

   @Override
   public String id() {
      return "trinkets";
   }

   @Override
   public boolean isPresent() {
      return true;
   }

   @Override
   public List<AccessorySlotRef> slots(class_1309 entity) {
      return (List<AccessorySlotRef>)TrinketsApi.getTrinketComponent(entity).map(comp -> {
         List<AccessorySlotRef> out = new ArrayList();
         comp.getInventory().forEach((group, byName) -> byName.forEach((name, inv) -> {
            for (int i = 0; i < inv.method_5439(); i++) {
               out.add(new TrinketsBackend.Ref(new SlotReference(inv, i)));
            }
         }));
         return out;
      }).orElseGet(List::of);
   }

   @Override
   public List<AccessorySlotRef> equipped(class_1309 entity) {
      List<AccessorySlotRef> out = new ArrayList();

      for (AccessorySlotRef ref : this.slots(entity)) {
         if (!ref.getStack().method_7960()) {
            out.add(ref);
         }
      }

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
      return TrinketsApi.getTrinket(stack.method_7909()) != TrinketsApi.getDefaultTrinket();
   }

   @Override
   public void applySlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
      TrinketsApi.getTrinketComponent(entity).ifPresent(component -> component.addTemporaryModifiers(modifiers));
      refreshSlots(entity);
   }

   @Override
   public void removeSlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
      TrinketsApi.getTrinketComponent(entity).ifPresent(component -> component.removeModifiers(modifiers));
      refreshSlots(entity);
   }

   private static void refreshSlots(class_1309 entity) {
      if (entity instanceof class_1657 player && player.field_7498 instanceof TrinketPlayerScreenHandler handler) {
         handler.trinkets$updateTrinketSlots(false);
      }
   }

   public static AccessorySlotRef slotRef(SlotReference reference) {
      return new TrinketsBackend.Ref(reference);
   }

   public static void handleChange(TrinketInventory inventory, int slot, class_1799 oldStack, class_1799 newStack) {
      TrinketComponent comp = inventory.getComponent();
      if (comp != null) {
         class_1309 entity = comp.getEntity();
         if (entity != null && !entity.method_37908().method_8608()) {
            TrinketsBackend.Ref ref = new TrinketsBackend.Ref(new SlotReference(inventory, slot));
            if (!oldStack.method_7960()) {
               Accessories.fireChange(entity, ref, oldStack, false);
            }

            if (!newStack.method_7960()) {
               Accessories.fireChange(entity, ref, newStack, true);
            }
         }
      }
   }

   private record Ref(SlotReference ref) implements AccessorySlotRef {
      @Override
      public String provider() {
         return "trinkets";
      }

      @Override
      public String group() {
         return this.ref.inventory().getSlotType().getGroup();
      }

      @Override
      public String type() {
         return this.ref.inventory().getSlotType().getName();
      }

      @Override
      public int index() {
         return this.ref.index();
      }

      @Override
      public class_1799 getStack() {
         return this.ref.inventory().method_5438(this.ref.index());
      }

      @Override
      public void setStack(class_1799 stack) {
         this.ref.inventory().method_5447(this.ref.index(), stack);
      }
   }
}
