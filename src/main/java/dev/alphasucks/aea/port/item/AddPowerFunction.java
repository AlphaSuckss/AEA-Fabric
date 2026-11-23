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
 * Overgrown's {@code apoli:add_power} loot function: adds the power to the stack for every listed slot.
 * AEA: reached through Apoli 2.9.2's registered type (see {@link ApoliLootFunctions}); Overgrown's Serializer
 * class became {@link #fromJson}.
 */
public class AddPowerFunction extends class_120 {
   final class_2960 power;
   final List<EquipmentSlot> slots;
   final boolean hidden;
   final boolean negative;

   AddPowerFunction(class_5341[] conditions, class_2960 power, List<EquipmentSlot> slots, boolean hidden, boolean negative) {
      super(conditions);
      this.power = power;
      this.slots = slots;
      this.hidden = hidden;
      this.negative = negative;
   }

   public class_5339 method_29321() {
      return ApoliLootFunctions.ADD_POWER;
   }

   // AEA: intermediary-name compile: spell out BiFunction#apply (vanilla's own is a bridge to method_521)
   @Override
   public class_1799 apply(class_1799 stack, class_47 context) {
      return this.method_521(stack, context);
   }

   @Override
   protected class_1799 method_522(class_1799 stack, class_47 context) {
      for (EquipmentSlot slot : this.slots) {
         ItemPowers.add(stack, this.power, slot, this.hidden, this.negative);
      }

      return stack;
   }

   public static AddPowerFunction fromJson(JsonObject json, class_5341[] conditions) {
      class_2960 power = new class_2960(class_3518.method_15265(json, "power"));
      List<EquipmentSlot> slots = new ArrayList();

      for (JsonElement el : class_3518.method_15261(json, "slot")) {
         EquipmentSlot slot = ItemPowers.slotByName(el.getAsString());
         if (slot != null) {
            slots.add(slot);
         }
      }

      boolean hidden = class_3518.method_15258(json, "hidden", false);
      boolean negative = class_3518.method_15258(json, "negative", false);
      return new AddPowerFunction(conditions, power, List.copyOf(slots), hidden, negative);
   }
}
