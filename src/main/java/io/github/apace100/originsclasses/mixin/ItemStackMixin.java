package io.github.apace100.originsclasses.mixin;

import net.minecraft.class_1799;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2561.class_2562;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_1799.class)
public abstract class ItemStackMixin {
   @Shadow
   public abstract boolean method_7985();

   @Shadow
   @Nullable
   public abstract class_2487 method_7969();

   @Inject(
      method = "method_7964()Lnet/minecraft/class_2561;",
      at = @At(value = "INVOKE", shift = Shift.BEFORE, target = "Lnet/minecraft/class_1799;method_7909()Lnet/minecraft/class_1792;"),
      cancellable = true
   )
   private void getExtendedName(CallbackInfoReturnable<class_2561> cir) {
      if (this.method_7985() && this.method_7969().method_10545("OriginalName")) {
         // AEA: a malformed name (e.g. edited NBT) parsed to null and crashed every caller of getName
         class_2561 name;
         try {
            name = class_2562.method_10877(this.method_7969().method_10558("OriginalName"));
         } catch (RuntimeException e) {
            name = null;
         }
         if (name != null) {
            cir.setReturnValue(name);
         }
      }
   }
}
