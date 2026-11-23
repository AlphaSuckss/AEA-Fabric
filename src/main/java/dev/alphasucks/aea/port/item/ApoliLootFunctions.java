package dev.alphasucks.aea.port.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.util.AddPowerLootFunction;
import io.github.apace100.apoli.util.RemovePowerLootFunction;
import net.minecraft.class_5339;

/**
 * AEA: Apoli 2.9.2 already registers {@code apoli:add_power} and {@code apoli:remove_power} (registering them again
 * would crash on a duplicate key). Overgrown's versions are parsed through Apoli's types instead: a JSON whose
 * {@code slot} is an array (or, for remove_power, missing) is Overgrown's format and builds the functions below;
 * a single {@code slot} string stays Apoli's own function. See {@code mixin.item.LootFunctionSerializerItemPowerMixin}.
 */
public final class ApoliLootFunctions {
   public static final class_5339 ADD_POWER = AddPowerLootFunction.TYPE;
   public static final class_5339 REMOVE_POWER = RemovePowerLootFunction.TYPE;

   private ApoliLootFunctions() {
   }

   /** Overgrown's add_power: {@code {"power": id, "slot": [slot names], "hidden"?: bool, "negative"?: bool}}. */
   public static boolean isOvergrownAddPower(JsonObject json) {
      JsonElement slot = json.get("slot");
      return slot != null && slot.isJsonArray();
   }

   /** Overgrown's remove_power: {@code {"power": id, "slot"?: [slot names]}}; no slot means every slot. */
   public static boolean isOvergrownRemovePower(JsonObject json) {
      JsonElement slot = json.get("slot");
      return slot == null || slot.isJsonArray();
   }
}
