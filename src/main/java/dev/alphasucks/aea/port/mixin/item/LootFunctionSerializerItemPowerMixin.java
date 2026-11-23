package dev.alphasucks.aea.port.mixin.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.alphasucks.aea.port.item.AddPowerFunction;
import dev.alphasucks.aea.port.item.ApoliLootFunctions;
import dev.alphasucks.aea.port.item.RemovePowerFunction;
import io.github.apace100.apoli.util.AddPowerLootFunction;
import io.github.apace100.apoli.util.RemovePowerLootFunction;
import net.minecraft.class_120;
import net.minecraft.class_5341;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * AEA (replaces Overgrown's ApoliLootFunctions.register()): {@code apoli:add_power}/{@code apoli:remove_power}
 * belong to Apoli 2.9.2, so Overgrown-format JSON (slot list, optional slot for remove) is routed to the ported
 * functions when Apoli's serializer is asked to read it; Apoli's own format is untouched.
 */
@Mixin(class_120.class_123.class)
public abstract class LootFunctionSerializerItemPowerMixin {
   @WrapOperation(
      method = "method_528(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/class_120;",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_120$class_123;method_530(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;[Lnet/minecraft/class_5341;)Lnet/minecraft/class_120;"
      )
   )
   private class_120 aea$overgrownItemPowerFunctions(
      class_120.class_123<?> serializer, JsonObject json, JsonDeserializationContext context, class_5341[] conditions, Operation<class_120> original
   ) {
      if (serializer instanceof AddPowerLootFunction.Serializer && ApoliLootFunctions.isOvergrownAddPower(json)) {
         return AddPowerFunction.fromJson(json, conditions);
      }
      if (serializer instanceof RemovePowerLootFunction.Serializer && ApoliLootFunctions.isOvergrownRemovePower(json)) {
         return RemovePowerFunction.fromJson(json, conditions);
      }
      return original.call(serializer, json, context, conditions);
   }
}
