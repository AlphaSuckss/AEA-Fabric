package io.github.apace100.originsclasses.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.calio.Calio;
import io.github.apace100.originsclasses.power.ClassPowerTypes;
import io.github.apace100.originsclasses.power.CraftAmountPower;
import java.util.Optional;
import net.minecraft.class_1304;
import net.minecraft.class_1322;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1714;
import net.minecraft.class_1731;
import net.minecraft.class_1738;
import net.minecraft.class_1766;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1811;
import net.minecraft.class_1819;
import net.minecraft.class_1820;
import net.minecraft.class_1829;
import net.minecraft.class_1831;
import net.minecraft.class_1937;
import net.minecraft.class_3222;
import net.minecraft.class_3955;
import net.minecraft.class_4174;
import net.minecraft.class_4317;
import net.minecraft.class_5134;
import net.minecraft.class_8566;
import net.minecraft.class_1322.class_1323;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(class_1714.class)
public class CraftingScreenHandlerMixin {
   @Unique
   private static Optional<class_3955> classes$CachedRecipe;

   @Inject(method = "method_17399(Lnet/minecraft/class_1703;Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_8566;Lnet/minecraft/class_1731;)V", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
   private static void cacheRecipe(
      class_1703 handler,
      class_1937 world,
      class_1657 player,
      class_8566 craftingInventory,
      class_1731 resultInventory,
      CallbackInfo ci,
      @Local Optional<?> optional
   ) {
      classes$CachedRecipe = (Optional<class_3955>)(Optional)optional;
   }

   @Inject(
      method = "method_17399(Lnet/minecraft/class_1703;Lnet/minecraft/class_1937;Lnet/minecraft/class_1657;Lnet/minecraft/class_8566;Lnet/minecraft/class_1731;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1731;method_5447(ILnet/minecraft/class_1799;)V")
   )
   private static void modifyCraftingResult(
      class_1703 handler,
      class_1937 world,
      class_1657 player,
      class_8566 craftingInventory,
      class_1731 resultInventory,
      CallbackInfo ci,
      @Local(ordinal = 0) class_1799 itemStack
   ) {
      if (itemStack.method_7960()) {
         return;
      }

      if (itemStack.method_7909().method_19263() && ClassPowerTypes.BETTER_CRAFTED_FOOD.isActive(player)) {
         class_4174 food = itemStack.method_7909().method_19264();
         int foodBonus = (int)Math.ceil(food.method_19230() / 3.0F);
         if (foodBonus < 1) {
            foodBonus = 1;
         }

         itemStack.method_7948().method_10569("FoodBonus", foodBonus);
      }

      if (ClassPowerTypes.QUALITY_EQUIPMENT.isActive(player) && isEquipment(itemStack)) {
         boolean recipeContainsEquipment = false;

         for (int i = 0; i < craftingInventory.method_5439() && !recipeContainsEquipment; i++) {
            if (isEquipment(craftingInventory.method_5438(i))) {
               recipeContainsEquipment = true;
            }
         }

         if (classes$CachedRecipe != null && classes$CachedRecipe.isPresent() && classes$CachedRecipe.get() instanceof class_4317) {
            recipeContainsEquipment = false;
         }

         if (!recipeContainsEquipment) {
            addQualityAttribute(itemStack);
         }
      }

      int baseValue = itemStack.method_7947();
      int newValue = (int)PowerHolderComponent.modify(player, CraftAmountPower.class, baseValue, p -> p.doesApply(itemStack));
      if (newValue != baseValue) {
         itemStack.method_7939(newValue < 0 ? 0 : Math.min(newValue, itemStack.method_7914()));
      }
   }

   private static void addQualityAttribute(class_1799 stack) {
      class_1792 item = stack.method_7909();
      if (item instanceof class_1738) {
         class_1304 slot = ((class_1738)item).method_7685();
         stack.method_7916(class_5134.field_23725, new class_1322("Blacksmith quality", 0.25, class_1323.field_6328), slot);
         Calio.setEntityAttributesAdditional(stack, true);
      } else if (item instanceof class_1829 || item instanceof class_1811) {
         stack.method_7916(class_5134.field_23721, new class_1322("Blacksmith quality", 0.5, class_1323.field_6328), class_1304.field_6173);
         Calio.setEntityAttributesAdditional(stack, true);
      } else if (item instanceof class_1766 || item instanceof class_1820) {
         stack.method_7948().method_10548("MiningSpeedMultiplier", 1.05F);
      } else if (item instanceof class_1819) {
         stack.method_7916(class_5134.field_23718, new class_1322("Blacksmith quality", 0.1, class_1323.field_6328), class_1304.field_6171);
         Calio.setEntityAttributesAdditional(stack, true);
      }
   }

   private static boolean isEquipment(class_1799 stack) {
      if (stack != null && !stack.method_7960()) {
         class_1792 item = stack.method_7909();
         if (item instanceof class_1738) {
            return true;
         } else if (item instanceof class_1831) {
            return true;
         } else {
            return item instanceof class_1811 ? true : item instanceof class_1819;
         }
      } else {
         return false;
      }
   }
}
