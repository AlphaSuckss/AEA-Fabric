package dev.alphasucks.aea.port.mixin.power;

import dev.alphasucks.aea.port.access.ItemPowerSlots;
import dev.alphasucks.aea.port.item.ItemPowerHandler;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(class_1309.class)
public abstract class LivingEntityItemPowerMixin implements ItemPowerSlots {
   @Unique
   private class_1799[] apoli$itemPowerStacks;

   @Override
   public class_1799[] apoli$itemPowerStacks() {
      class_1799[] stacks = this.apoli$itemPowerStacks;
      if (stacks == null) {
         stacks = ItemPowerHandler.newSlotArray();
         this.apoli$itemPowerStacks = stacks;
      }

      return stacks;
   }
}
