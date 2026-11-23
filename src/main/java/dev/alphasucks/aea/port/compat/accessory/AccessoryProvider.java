package dev.alphasucks.aea.port.compat.accessory;

import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.class_1309;
import net.minecraft.class_1322;
import net.minecraft.class_1799;
import net.minecraft.class_1937;

public interface AccessoryProvider {
   String id();

   boolean isPresent();

   List<AccessorySlotRef> equipped(class_1309 var1);

   List<AccessorySlotRef> slots(class_1309 var1);

   class_1799 equip(class_1309 var1, List<AccessorySlot> var2, class_1799 var3);

   default boolean isAccessory(class_1799 stack, class_1937 level) {
      return false;
   }

   default void applySlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
   }

   default void removeSlotModifiers(class_1309 entity, Multimap<String, class_1322> modifiers) {
   }
}
