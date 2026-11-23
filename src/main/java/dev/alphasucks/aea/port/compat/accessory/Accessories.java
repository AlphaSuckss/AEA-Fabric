package dev.alphasucks.aea.port.compat.accessory;

import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_1799;
import net.minecraft.class_1937;

public final class Accessories {
   private static final List<AccessoryProvider> PROVIDERS = new ArrayList();
   private static Accessories.ChangeListener changeListener = (e, r, s, eq) -> {};

   private Accessories() {
   }

   public static void register(AccessoryProvider provider) {
      if (provider != null && provider.isPresent()) {
         PROVIDERS.add(provider);
      }
   }

   public static boolean anyPresent() {
      return !PROVIDERS.isEmpty();
   }

   public static List<AccessoryProvider> providers() {
      return PROVIDERS;
   }

   public static void setChangeListener(Accessories.ChangeListener listener) {
      changeListener = listener != null ? listener : (e, r, s, eq) -> {};
   }

   public static void fireChange(class_1309 entity, AccessorySlotRef ref, class_1799 stack, boolean equipping) {
      changeListener.onChange(entity, ref, stack, equipping);
   }

   public static List<AccessorySlotRef> equipped(class_1309 entity) {
      if (PROVIDERS.isEmpty()) {
         return List.of();
      }

      List<AccessorySlotRef> out = new ArrayList();

      for (AccessoryProvider p : PROVIDERS) {
         out.addAll(p.equipped(entity));
      }

      return out;
   }

   public static List<AccessorySlotRef> equipped(class_1309 entity, List<AccessorySlot> filter) {
      List<AccessorySlotRef> out = new ArrayList();

      for (AccessorySlotRef ref : equipped(entity)) {
         if (AccessorySlot.matchesAny(filter, ref)) {
            out.add(ref);
         }
      }

      return out;
   }

   public static List<AccessorySlotRef> slots(class_1309 entity) {
      if (PROVIDERS.isEmpty()) {
         return List.of();
      }

      List<AccessorySlotRef> out = new ArrayList();

      for (AccessoryProvider p : PROVIDERS) {
         out.addAll(p.slots(entity));
      }

      return out;
   }

   public static List<AccessorySlotRef> slots(class_1309 entity, List<AccessorySlot> filter) {
      List<AccessorySlotRef> out = new ArrayList();

      for (AccessorySlotRef ref : slots(entity)) {
         if (AccessorySlot.matchesAny(filter, ref)) {
            out.add(ref);
         }
      }

      return out;
   }

   public static boolean isAccessory(class_1799 stack, class_1937 level) {
      for (AccessoryProvider p : PROVIDERS) {
         if (p.isAccessory(stack, level)) {
            return true;
         }
      }

      return false;
   }

   public static void applySlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
      for (AccessoryProvider p : PROVIDERS) {
         p.applySlotModifiers(entity, modifiers);
      }
   }

   public static void removeSlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
      for (AccessoryProvider p : PROVIDERS) {
         p.removeSlotModifiers(entity, modifiers);
      }
   }

   @FunctionalInterface
   public interface ChangeListener {
      void onChange(class_1309 var1, AccessorySlotRef var2, class_1799 var3, boolean var4);
   }
}
