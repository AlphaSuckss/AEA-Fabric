package dev.alphasucks.aea.port.mixin.loot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.access.KeyedLootTable;
import net.minecraft.class_52;
import net.minecraft.class_60;
import net.minecraft.class_8488;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_60.class)
public abstract class LootDataManagerKeyMixin {
   @ModifyReturnValue(method = "getElement(Lnet/minecraft/class_8488;)Ljava/lang/Object;", at = @At("RETURN"))
   private Object apoli$keyLootTable(Object element, @Local(argsOnly = true) class_8488<?> id) {
      if (element instanceof class_52 table && table != class_52.field_948) {
         KeyedLootTable keyed = (KeyedLootTable)table;
         if (keyed.apoli$key() == null) {
            keyed.apoli$setKey(id.comp_1475());
         }
      }

      return element;
   }
}
