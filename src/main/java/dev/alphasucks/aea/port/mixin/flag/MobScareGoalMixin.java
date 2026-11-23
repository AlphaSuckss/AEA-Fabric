package dev.alphasucks.aea.port.mixin.flag;

import dev.alphasucks.aea.port.entity.ScareMobsGoal;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1314;
import net.minecraft.class_1355;
import net.minecraft.class_1937;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1308.class)
public abstract class MobScareGoalMixin {
   @Shadow
   protected class_1355 field_6201;

   @Inject(method = "<init>(Lnet/minecraft/class_1299;Lnet/minecraft/class_1937;)V", at = @At("TAIL"))
   private void apoli$addScareGoal(class_1299<? extends class_1308> type, class_1937 level, CallbackInfo ci) {
      if (!level.method_8608()) {
         if ((Object)this instanceof class_1314 pathfinder) {
            this.field_6201.method_6277(0, new ScareMobsGoal(pathfinder));
         }
      }
   }
}
