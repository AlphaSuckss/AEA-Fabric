package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import net.minecraft.class_1657;
import net.minecraft.class_1874;
import net.minecraft.class_2338;
import net.minecraft.class_2591;
import net.minecraft.class_2609;
import net.minecraft.class_2624;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_2609.class)
public abstract class AbstractFurnaceBlockEntityMixin extends class_2624 {
   private static class_1657 playerTakingStacks;

   protected AbstractFurnaceBlockEntityMixin(class_2591<?> blockEntityType, class_2338 blockPos, class_2680 blockState) {
      super(blockEntityType, blockPos, blockState);
   }

   @Inject(method = "method_17763(Lnet/minecraft/class_3222;)V", at = @At("HEAD"))
   private void savePlayerForLater(class_3222 player, CallbackInfo ci) {
      // AEA: always overwrite (and clear at TAIL) - the stale static made every later furnace/smoker XP drop,
      // including from broken furnaces, use whoever last took from a smoker
      playerTakingStacks = this.method_11017() == class_2591.field_16414 ? player : null;
   }

   @Inject(method = "method_17763(Lnet/minecraft/class_3222;)V", at = @At("TAIL"))
   private void clearSavedPlayer(class_3222 player, CallbackInfo ci) {
      playerTakingStacks = null;
   }

   @ModifyExpressionValue(method = "method_17761(Ljava/util/List;Lnet/minecraft/class_3218;Lnet/minecraft/class_243;Lit/unimi/dsi/fastutil/objects/Object2IntMap$Entry;Lnet/minecraft/class_1860;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1874;method_8171()F"))
   private static float modifyExperienceGain(float regularXp) {
      return playerTakingStacks != null && ClassPowerTypes.MORE_SMOKER_XP.isActive(playerTakingStacks) ? regularXp * 2.0F : regularXp;
   }
}
