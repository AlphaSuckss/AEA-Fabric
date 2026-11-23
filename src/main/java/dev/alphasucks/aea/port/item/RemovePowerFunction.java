package dev.alphasucks.aea.port.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.alphasucks.aea.port.data.EquipmentSlot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_120;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_3518;
import net.minecraft.class_47;
import net.minecraft.class_5339;
import net.minecraft.class_5341;

/**
 * Overgrown's {@code apoli:remove_power} loot function: removes the power from the listed slots (all slots when
 * none are listed). AEA: reached through Apoli 2.9.2's registered type (see {@link ApoliLootFunctions}); also
 * removes Apoli 2.9.2-format entries of the power.
 */
public class RemovePowerFunction extends class_120 {
   final class_2960 power;
   final List<EquipmentSlot> slots;

   RemovePowerFunction(class_5341[] conditions, class_2960 power, List<EquipmentSlot> slots) {
      super(conditions);
      this.power = power;
      this.slots = slots;
   }

   public class_5339 method_29321() {
      return ApoliLootFunctions.REMOVE_POWER;
   }

   // AEA: intermediary-name compile: spell out BiFunction#apply (vanilla's own is a bridge to method_521)
   @Override
   public class_1799 apply(class_1799 stack, class_47 context) {
      return this.method_521(stack, context);
   }

   @Override
   protected class_1799 method_522(class_1799 stack, class_47 context) {
      if (this.slots.isEmpty()) {
         ItemPowers.remove(stack, this.power, null);
      } else {
         for (EquipmentSlot slot : this.slots) {
            ItemPowers.remove(stack, this.power, slot);
         }
      }

      return stack;
   }

   public static RemovePowerFunction fromJson(JsonObject json, class_5341[] conditions) {
      class_2960 power = new class_2960(class_3518.method_15265(json, "power"));
      List<EquipmentSlot> slots = new ArrayList();
      if (json.has("slot")) {
         for (JsonElement el : class_3518.method_15261(json, "slot")) {
            EquipmentSlot slot = ItemPowers.slotByName(el.getAsString());
            if (slot != null) {
               slots.add(slot);
            }
         }
      }

      return new RemovePowerFunction(conditions, power, List.copyOf(slots));
   }
}
