package dev.alphasucks.aea.port.mixin.loot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.alphasucks.aea.port.access.KeyedLootTable;
import dev.alphasucks.aea.port.power.builtin.LootTableReplacement;
import dev.alphasucks.aea.port.power.builtin.ReplaceLootTablePower;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_47;
import net.minecraft.class_52;
import net.minecraft.class_60;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_52.class)
public abstract class LootTableReplaceMixin implements KeyedLootTable {
   private static final String GET_RANDOM_ITEMS_RAW = "getRandomItemsRaw(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V";
   @Unique
   @Nullable
   private class_2960 apoli$key;

   @Nullable
   @Override
   public class_2960 apoli$key() {
      return this.apoli$key;
   }

   @Override
   public void apoli$setKey(class_2960 key) {
      this.apoli$key = key;
   }

   @Inject(method = "method_328(Lnet/minecraft/class_47;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
   private void apoli$replaceTable(class_47 context, Consumer<class_1799> consumer, CallbackInfo ci) {
      if (this.apoli$key != null && ReplaceLootTablePower.HELD.isHeld()) {
         class_52 self = (class_52)(Object)this;
         if (!LootTableReplacement.isStacked(self)) {
            List<class_2960> ids = new ArrayList(2);
            ReplaceLootTablePower.replacementsFor(context, this.apoli$key.toString(), ids);
            if (!ids.isEmpty()) {
               class_60 registries = context.method_299().method_8503().method_3857();
               List<class_52> tables = new ArrayList(ids.size());

               for (int i = 0; i < ids.size(); i++) {
                  class_52 table = registries.getLootTable((class_2960)ids.get(i));
                  if (table == class_52.field_948) {
                     ReplaceLootTablePower.warnMissing((class_2960)ids.get(i));
                  } else {
                     tables.add(table);
                  }
               }

               if (!tables.isEmpty()) {
                  LootTableReplacement.begin(self, tables);

                  try {
                     LootTableReplacement.peek().method_328(context, consumer);
                  } finally {
                     LootTableReplacement.end();
                  }

                  ci.cancel();
               }
            }
         }
      }
   }

   @ModifyExpressionValue(
      method = "method_328(Lnet/minecraft/class_47;Ljava/util/function/Consumer;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_47;method_298(Lnet/minecraft/class_47$class_8487;)Z")
   )
   private boolean apoli$enterReplacement(boolean pushed) {
      if (pushed) {
         LootTableReplacement.enter();
      }

      return pushed;
   }

   @Inject(
      method = "method_328(Lnet/minecraft/class_47;Ljava/util/function/Consumer;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_47;method_295(Lnet/minecraft/class_47$class_8487;)V")
   )
   private void apoli$exitReplacement(class_47 context, Consumer<class_1799> consumer, CallbackInfo ci) {
      LootTableReplacement.exit();
   }
}
