package dev.alphasucks.aea.port.mixin.sound;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.port.sound.ReplaceableSound;
import dev.alphasucks.aea.port.sound.SoundVariants;
import java.util.List;
import net.minecraft.class_1102;
import net.minecraft.class_1111;
import net.minecraft.class_1146;
import net.minecraft.class_1148;
import net.minecraft.class_2960;
import net.minecraft.class_3419;
import net.minecraft.class_5819;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_1102.class)
public abstract class AbstractSoundInstanceMixin implements ReplaceableSound {
   @Mutable
   @Shadow
   @Final
   protected class_2960 field_5448;
   @Shadow
   protected float field_5442;
   @Shadow
   protected float field_5441;
   @Shadow
   @Final
   protected class_5819 field_38800;
   @Unique
   private int apoli$variant;

   @Inject(method = "<init>(Lnet/minecraft/class_2960;Lnet/minecraft/class_3419;Lnet/minecraft/class_5819;)V", at = @At("TAIL"))
   private void apoli$unwrapVariant(class_2960 location, class_3419 source, class_5819 random, CallbackInfo ci) {
      if (SoundVariants.isVariant(location)) {
         class_2960 base = SoundVariants.base(location);
         int index = SoundVariants.index(location);
         if (base != null && index >= 0) {
            this.field_5448 = base;
            this.apoli$variant = index + 1;
         }
      }
   }

   @ModifyExpressionValue(
      method = "method_4783",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1146;method_4887(Lnet/minecraft/class_5819;)Lnet/minecraft/class_1111;")
   )
   private class_1111 apoli$pickVariant(class_1111 original, @Local class_1146 events) {
      if (this.apoli$variant == 0) {
         return original;
      }

      List<class_1148<class_1111>> entries = ((WeighedSoundEventsAccessor)events).apoli$getList();
      int index = this.apoli$variant - 1;
      return index < entries.size() ? (class_1111)((class_1148)entries.get(index)).method_4893(this.field_38800) : original;
   }

   @Override
   public void apoli$replaceWith(class_2960 location, float volume, float pitch) {
      if (!location.equals(this.field_5448)) {
         this.apoli$variant = 0;
      }

      this.field_5448 = location;
      this.field_5442 = volume;
      this.field_5441 = pitch;
   }

   @Override
   public float apoli$baseVolume() {
      return this.field_5442;
   }

   @Override
   public float apoli$basePitch() {
      return this.field_5441;
   }
}
