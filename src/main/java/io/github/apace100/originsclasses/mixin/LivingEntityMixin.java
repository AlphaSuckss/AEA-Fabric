package io.github.apace100.originsclasses.mixin;

import io.github.apace100.originsclasses.power.ClassPowerTypes;
import java.util.Random;
import net.minecraft.class_1282;
import net.minecraft.class_1293;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1429;
import net.minecraft.class_173;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_52;
import net.minecraft.class_8567;
import net.minecraft.class_8567.class_8568;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;

@Mixin(class_1309.class)
public abstract class LivingEntityMixin extends class_1297 {
   public LivingEntityMixin(class_1299<?> type, class_1937 world) {
      super(type, world);
   }

   @Inject(
      method = "method_16077(Lnet/minecraft/class_1282;Z)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_52;method_51880(Lnet/minecraft/class_8567;JLjava/util/function/Consumer;)V"
      )
   )
   private void dropAdditionalRancherLoot(
      class_1282 damageSource,
      boolean causedByPlayer,
      CallbackInfo ci,
      @Local class_52 lootTable,
      @Local class_8568 builder
   ) {
      if (causedByPlayer && (Object)this instanceof class_1429 && ClassPowerTypes.MORE_ANIMAL_LOOT.isActive(damageSource.method_5529()) && new Random().nextInt(10) < 3
         )
       {
         lootTable.method_51882(builder.method_51875(class_173.field_1173), ((class_1309)(Object)this)::method_5775);
      }
   }

   @Inject(method = "method_6092(Lnet/minecraft/class_1293;)Z", at = @At("RETURN"))
   private void addStatusEffect(class_1293 effect, CallbackInfoReturnable<Boolean> ci) {
      if ((Boolean)ci.getReturnValue() && !effect.method_5591() && ClassPowerTypes.TAMED_POTION_DIFFUSAL.isActive(this)) {
         this.method_37908()
            .method_8390(class_1321.class, this.method_5829().method_1012(8.0, 2.0, 8.0).method_1012(-8.0, -2.0, -8.0), e -> e.method_35057() == (Object)this)
            .forEach(e -> e.method_6092(new class_1293(effect))); // AEA: copy - sharing one instance made every holder tick its duration down
      }
   }
}
