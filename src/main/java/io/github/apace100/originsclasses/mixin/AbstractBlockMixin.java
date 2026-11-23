package io.github.apace100.originsclasses.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.originsclasses.ducks.SneakingStateSavingManager;
import io.github.apace100.originsclasses.networking.ModPacketsS2C;
import io.github.apace100.originsclasses.power.MultiMinePower;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import net.minecraft.class_4970;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_4970.class)
public class AbstractBlockMixin {
   @Inject(method = "method_9594(Lnet/minecraft/class_2680;Lnet/minecraft/class_1657;Lnet/minecraft/class_1922;Lnet/minecraft/class_2338;)F", at = @At("RETURN"), cancellable = true)
   private void modifyMultiMinedBlockBreakingDelta(class_2680 state, class_1657 player, class_1922 world, class_2338 pos, CallbackInfoReturnable<Float> cir) {
      boolean processMultimine = false;
      if (player instanceof class_3222) {
         SneakingStateSavingManager sneakingState = (SneakingStateSavingManager)((class_3222)player).field_13974;
         processMultimine = !sneakingState.wasSneakingWhenBlockBreakingStarted();
      } else {
         processMultimine = ModPacketsS2C.isMultiMining;
      }

      if (processMultimine) {
         class_1799 tool = player.method_6118(class_1304.field_6173);
         int toolDurability = 128;
         if (!tool.method_7960() && tool.method_7963()) {
            toolDurability = tool.method_7936() - tool.method_7919();
         }

         int finalToolDurability = toolDurability;
         ((PowerHolderComponent)PowerHolderComponent.KEY.get(player)).getPowers(MultiMinePower.class).forEach(mmp -> {
            if (mmp.isBlockStateAffected(state)) {
               int affectBlockCount = mmp.getAffectedBlocks(state, pos).size();
               if (affectBlockCount > 0) {
                  int multiplier = Math.min(affectBlockCount, finalToolDurability - 1);
                  // AEA: clamp - with 1 durability left (or a non-damageable tool) this divided by 0 or a
                  // negative number, making logs break instantly or never
                  multiplier = Math.max(1, (int)Math.ceil(multiplier * 0.75F));
                  cir.setReturnValue(cir.getReturnValueF() / multiplier);
               }
            }
         });
      }
   }
}
